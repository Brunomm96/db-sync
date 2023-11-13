package datawake.datadriven.databasesync.backoffice.jobs;

import datawake.datadriven.databasesync.core.models.ConnectionTable;
import datawake.datadriven.databasesync.core.services.ConnectionsTablesService;
import datawake.datadriven.databasesync.shared.dtos.ParameterizedInsertQuery;
import com.zaxxer.hikari.HikariDataSource;
import datawake.datadriven.databasesync.core.events.publishers.DatabaseSyncEventPublisher;
import datawake.datadriven.databasesync.core.daos.impls.DatabaseConnectionImpl;
import datawake.datadriven.databasesync.core.models.Connection;
import datawake.datadriven.databasesync.core.models.Table;
import datawake.datadriven.databasesync.core.services.ConnectionsService;
import datawake.datadriven.databasesync.shared.utils.DatabaseConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class SyncDatabasesJob {

    private static final Logger log = LoggerFactory.getLogger(SyncDatabasesJob.class);

    private final DatabaseSyncEventPublisher databaseSyncEventPublisher;

    private final ConnectionsService connectionsService;

    private final ConnectionsTablesService connectionsTablesService;

    private final DatabaseConnectionImpl dbImpl;

    private final DataSource localDataSource;

    private final String CONNECTION_SYNC_DONE = "CONNECTION SYNC DONE: %s";

    private final String CONNECTION_SYNC_ERROR = "CONNECTION SYNC ERROR: %s \nCAUSED BY: %s";

    private final String TABLE_INSERT_ERROR = "TABLE INSERT ERROR: %s \nCAUSED BY: %s";

    private final String TABLE_DELETE_ERROR = "TABLE CLEAR ERROR: %s \nCAUSED BY: %s";

    private final String DATABASE_AND_COLUMN_CREATION_ERROR = "DATABASE AND COLUMN CREATION ERROR: %s \nCAUSED BY: %s";

    public SyncDatabasesJob(DatabaseSyncEventPublisher databaseSyncEventPublisher, ConnectionsService connectionsService, ConnectionsTablesService connectionsTablesService, DatabaseConnectionImpl dbImpl, DataSource localDataSource) {
        this.databaseSyncEventPublisher = databaseSyncEventPublisher;
        this.connectionsService = connectionsService;
        this.connectionsTablesService = connectionsTablesService;
        this.dbImpl = dbImpl;
        this.localDataSource = localDataSource;
    }

    @Scheduled(fixedRate = 1000)
    public void run() {
        runConnections(PageRequest.of(0, 5));
    }

    private void runConnections(Pageable pageable) {
        Page<Connection> connectionsPage = connectionsService.getAll(pageable);

        if (connectionsPage.isEmpty())
            return;

        List<CompletableFuture<Void>> connections = connectionsPage.getContent().stream()
                .map(this::startSync)
                .toList();

        CompletableFuture.allOf(connections.toArray(new CompletableFuture<?>[0])).join();

        runConnections(pageable.next());
    }

    private CompletableFuture<Void> startSync(Connection connection) {
        return CompletableFuture.runAsync(() -> {
            try (HikariDataSource dataSource = DatabaseConnectionUtil.createDataSource(
                    connection.getDriverClassname(),
                    connection.getUrl(),
                    connection.getUsername(),
                    connection.getPassword()
            )) {
                List<CompletableFuture<Void>> fetches = connection.getConnectionsTables().stream()
                        .map(connectionTable -> fetchData(connectionTable, dataSource)).toList();

                CompletableFuture.allOf(fetches.toArray(new CompletableFuture<?>[0])).join();
            }
        });
    }

    private CompletableFuture<Void> fetchData(ConnectionTable connectionTable, DataSource dataSource) {
        return CompletableFuture.runAsync(() -> {
            if (!connectionTable.getIsActive() || connectionTable.getIsRunning())
                return;

            LocalDateTime lastSyncDate = connectionTable.getLastSyncDate();
            Integer timeInterval = connectionTable.getTimeInterval();

            if (lastSyncDate != null && lastSyncDate.plusSeconds(timeInterval).isAfter(LocalDateTime.now()))
                return;


            connectionTable.setIsRunning(true);
            connectionsTablesService.save(connectionTable);

            Table table = connectionTable.getTable();
            Connection connection = connectionTable.getConnection();
            String tableName = table.getName();

            String START_SYNC = String.format("SYNC STARTED FOR: %s \nON TABLE %s",
                    connection.getName(),
                    tableName
                    );
            log.info(START_SYNC);
            databaseSyncEventPublisher.publish(START_SYNC, true);

            String customQuery = connectionTable.getCustomQuery();

            boolean hasCustomQuery = !(customQuery == null || customQuery.isBlank());
            String query = !hasCustomQuery
                    ? String.format(DatabaseConnectionUtil.SELECT_TEMPLATE, "*", tableName)
                    : customQuery;

            try (
                    java.sql.Connection dbConnection = dataSource.getConnection();
                    PreparedStatement preparedStatement = dbConnection.prepareStatement(query)
            ) {
                try (
                        java.sql.Connection localDbConnection = localDataSource.getConnection();
                        ResultSet result = preparedStatement.executeQuery()
                ) {
                    boolean setupCompleted = hasCustomQuery
                            ? setDatabaseAndColumnsWithCustomQuery(localDbConnection, result.getMetaData(), table)
                            : setDatabaseAndColumns(dbConnection, localDbConnection, table);

                    if (setupCompleted) {
//                    List<CompletableFuture<Void>> stores = new ArrayList<>();

                        if (connectionTable.getIsCleanRun())
                            clearTable(localDbConnection, tableName);

                        while (result.next()) {
                            ParameterizedInsertQuery insertQuery = DatabaseConnectionUtil.generateParameterizedInsertQuery(result, tableName);

                            try {

                                dbImpl.executeSyncUpdate(localDbConnection, insertQuery);

                            } catch (SQLException e) {
                                if (e.getErrorCode() != 2627) {
                                    String errorMessage = String.format(TABLE_INSERT_ERROR, e.getMessage(), e.getMessage());
                                    databaseSyncEventPublisher.publish(errorMessage, false);
                                }
                            }


//                    stores.add(
//                            dbImpl.executeSyncUpdate(localDbConnection, insertQuery).exceptionally(e -> {
//                                        Throwable throwable = e.getCause();
//                                        if (throwable instanceof SQLException exception) {
//                                            if (exception.getErrorCode() != 2627) {
//                                                String errorMessage = String.format(TABLE_INSERT_ERROR, e.getMessage(), exception.getMessage());
//                                                databaseSyncEventPublisher.publish(errorMessage, false);
//                                            }
//                                        }
//                                        return null;
//                                    }
//                            ));
                        }

//                CompletableFuture.allOf(stores.toArray(new CompletableFuture<?>[0])).completeAsync(() -> {
//                    String successMessage = String.format(TABLE_SYNC_DONE, tableName, connectionName);
//                    databaseSyncEventPublisher.publish(successMessage, true);
//                    return null;
//                }).join();
                    }

                    String successMessage = String.format(CONNECTION_SYNC_DONE, connection.getName());
                    databaseSyncEventPublisher.publish(successMessage, true);
                }
            } catch (SQLException e) {
                String errorMessage = String.format(CONNECTION_SYNC_ERROR, e.getMessage(), e.getCause().getMessage());
                databaseSyncEventPublisher.publish(errorMessage, false);
            } finally {
                connectionTable.setIsRunning(false);
                connectionTable.setLastSyncDate(LocalDateTime.now());
                connectionsTablesService.save(connectionTable);
            }
        });
    }

    private void clearTable(java.sql.Connection connection, String tableName) {
        String query = String.format(DatabaseConnectionUtil.DELETE_TEMPLATE, tableName);

        try {
            dbImpl.executeSyncUpdate(connection, query);
        } catch (SQLException e) {
            String errorMessage = String.format(TABLE_DELETE_ERROR, e.getMessage(), e.getMessage());
            databaseSyncEventPublisher.publish(errorMessage, false);
        }
    }

    private boolean setDatabaseAndColumns(java.sql.Connection dbConnection, java.sql.Connection localDbConnection, Table table) throws SQLException {
        String tableName = table.getName();
        try (
                ResultSet columns = dbConnection.getMetaData().getColumns(null, null, tableName, null)
        ) {
            dbImpl.createTableIfNotExists(localDbConnection, tableName);

            String dbName = localDbConnection.getMetaData().getDatabaseProductName();
            List<CompletableFuture<Void>> columnsToCreate = new ArrayList<>();

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");

                String column = DatabaseConnectionUtil.generateColumn(columns, dbName);

                columnsToCreate.add(dbImpl.createColumnIfNotExists(localDbConnection, tableName, column, columnName));
            }

            CompletableFuture.allOf(columnsToCreate.toArray(new CompletableFuture<?>[0])).join();

            dbImpl.dropColumnIfExists(localDbConnection, tableName, DatabaseConnectionUtil.DEFAULT_PLACEHOLDER_COLUMN_NAME).join();
            dbImpl.createPrimaryKeyConstraintIfNotExists(localDbConnection, table).join();
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() != 4923) {
                String errorMessage = String.format(DATABASE_AND_COLUMN_CREATION_ERROR, e.getMessage(), e.getMessage());
                databaseSyncEventPublisher.publish(errorMessage, false);
                return false;
            }
            return true;
        }
    }

    private boolean setDatabaseAndColumnsWithCustomQuery(
            java.sql.Connection targetDbConnection,
            ResultSetMetaData metaData,
            Table table
    ) throws SQLException {
        String tableName = table.getName();

        try {
            dbImpl.createTableIfNotExists(targetDbConnection, tableName).join();

            String dbName = targetDbConnection.getMetaData().getDatabaseProductName();

            List<CompletableFuture<Void>> columnsToCreate = new ArrayList<>();

            for (int columnIndex = 1; columnIndex < metaData.getColumnCount(); columnIndex++) {
                String columnName = metaData.getColumnName(columnIndex);

                String column = DatabaseConnectionUtil.generateColumnFromMetaData(metaData, columnIndex, dbName, table.getPrimaryKeys());

                columnsToCreate.add(dbImpl.createColumnIfNotExists(targetDbConnection, tableName, column, columnName));
            }

            CompletableFuture.allOf(columnsToCreate.toArray(new CompletableFuture<?>[0])).join();

            dbImpl.dropColumnIfExists(targetDbConnection, tableName, DatabaseConnectionUtil.DEFAULT_PLACEHOLDER_COLUMN_NAME).join();
            dbImpl.createPrimaryKeyConstraintIfNotExists(targetDbConnection, table).join();
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() != 4923) {
                String errorMessage = String.format(DATABASE_AND_COLUMN_CREATION_ERROR, e.getMessage(), e.getMessage());
                databaseSyncEventPublisher.publish(errorMessage, false);
                return false;
            }
            return true;
        }
    }
}
