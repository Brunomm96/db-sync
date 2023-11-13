package datawake.datadriven.databasesync.core.daos.impls;

import datawake.datadriven.databasesync.backoffice.jobs.SyncDatabasesJob;
import datawake.datadriven.databasesync.core.models.Table;
import datawake.datadriven.databasesync.shared.dtos.ParameterizedInsertQuery;
import datawake.datadriven.databasesync.shared.utils.DatabaseConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class DatabaseConnectionImpl {

    private static final Logger log = LoggerFactory.getLogger(SyncDatabasesJob.class);
    @Async("threadPoolDatabaseSyncExecutor")
    public CompletableFuture<Boolean> tableExists(DatabaseMetaData databaseMetaData, String tableName) {
        try (
                ResultSet resultSet = databaseMetaData.getTables(null, null, tableName, null)
        ) {
            return CompletableFuture.completedFuture(resultSet.next());
        } catch (SQLException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("threadPoolDatabaseSyncExecutor")
    public CompletableFuture<Boolean> columnExists(DatabaseMetaData databaseMetaData, String tableName, String columnName) {
        try (
                ResultSet resultSet = databaseMetaData.getColumns(null, null, tableName, columnName)
        ) {
            return CompletableFuture.completedFuture(resultSet.next());
        } catch (SQLException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("threadPoolDatabaseSyncExecutor")
    public CompletableFuture<Boolean> containsPrimaryKey(DatabaseMetaData databaseMetaData, String tableName) {
        try (
                ResultSet resultSet = databaseMetaData.getPrimaryKeys(null, null, tableName)
        ) {
            return CompletableFuture.completedFuture(resultSet.next());
        } catch (SQLException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("threadPoolDatabaseSyncExecutor")
    public CompletableFuture<Void> createTableIfNotExists(java.sql.Connection dbConnection, String tableName) {
        try {
            if (tableExists(dbConnection.getMetaData(), tableName).get())
                return CompletableFuture.completedFuture(null);

            String defaultColumn = String.format(
                    "%s %s",
                    DatabaseConnectionUtil.DEFAULT_PLACEHOLDER_COLUMN_NAME,
                    DatabaseConnectionUtil.DEFAULT_PLACEHOLDER_COLUMN_TYPE
            );

            String query = String.format(DatabaseConnectionUtil.CREATE_TABLE_TEMPLATE, tableName, defaultColumn);

            try (
                    PreparedStatement preparedStatement = dbConnection.prepareStatement(query)
            ) {
                preparedStatement.executeUpdate();
            }

            return CompletableFuture.completedFuture(null);

        } catch (SQLException | ExecutionException | InterruptedException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

//    public void createTableIfNotExists(java.sql.Connection dbConnection, String tableName) {
//        try {
//            if (tableExists(dbConnection.getMetaData(), tableName).get())
//                return;
//
//            String defaultColumn = String.format(
//                    "%s %s",
//                    DatabaseConnectionUtil.DEFAULT_PLACEHOLDER_COLUMN_NAME,
//                    DatabaseConnectionUtil.DEFAULT_PLACEHOLDER_COLUMN_TYPE
//            );
//
//            String query = String.format(DatabaseConnectionUtil.CREATE_TABLE_TEMPLATE, tableName, defaultColumn);
//
//            try (
//                    PreparedStatement preparedStatement = dbConnection.prepareStatement(query)
//            ) {
//                preparedStatement.executeUpdate();
//            }
//
//        } catch (SQLException | ExecutionException | InterruptedException e) {
//            return;
//        }
//    }

    @Async("threadPoolDatabaseSyncExecutor")
    public CompletableFuture<Void> createColumnIfNotExists(java.sql.Connection dbConnection, String tableName, String column, String columnName) {
        try {
            if (columnExists(dbConnection.getMetaData(), tableName, columnName).get())
                return CompletableFuture.completedFuture(null);

            String query = String.format(DatabaseConnectionUtil.ADD_COLUMN_TEMPLATE, tableName, column);

            try (
                    PreparedStatement preparedStatement = dbConnection.prepareStatement(query)
            ) {
                preparedStatement.executeUpdate();
            }

            return CompletableFuture.completedFuture(null);
        } catch (SQLException | InterruptedException | ExecutionException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("threadPoolDatabaseSyncExecutor")
    public CompletableFuture<Void> createPrimaryKeyConstraintIfNotExists(java.sql.Connection dbConnection, Table table) {
        String tableName = table.getName();

        try {
            if (containsPrimaryKey(dbConnection.getMetaData(), tableName).get())
                return CompletableFuture.completedFuture(null);

            String constraint = String.format(DatabaseConnectionUtil.PRIMARY_KEY_TEMPLATE, tableName);
            String query = String.format(
                    DatabaseConnectionUtil.CONSTRAINT_PRIMARY_KEY_TEMPLATE,
                    tableName,
                    constraint,
                    table.getPrimaryKeys()
            );

            try (
                    PreparedStatement preparedStatement = dbConnection.prepareStatement(query)
            ) {
                preparedStatement.executeUpdate();
            }

            return CompletableFuture.completedFuture(null);
        } catch (SQLException | ExecutionException | InterruptedException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("threadPoolDatabaseSyncExecutor")
    public CompletableFuture<Void> dropColumnIfExists(java.sql.Connection dbConnection, String tableName, String columnName) {
        try {
            if (columnExists(dbConnection.getMetaData(), tableName, columnName).get()) {
                String dropQuery = String.format(DatabaseConnectionUtil.DROP_COLUMN_TEMPLATE, tableName, columnName);

                try (
                        PreparedStatement preparedStatement = dbConnection.prepareStatement(dropQuery)
                ) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException | ExecutionException | InterruptedException e) {
            return CompletableFuture.failedFuture(e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async("threadPoolDatabaseSyncExecutor")
    public CompletableFuture<Void> executeUpdate(java.sql.Connection dbConnection, String query) {
        try (
                PreparedStatement preparedStatement = dbConnection.prepareStatement(query)
        ) {

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            return CompletableFuture.failedFuture(e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async("threadPoolDatabaseSyncExecutor")
    public CompletableFuture<Void> executeUpdate(java.sql.Connection dbConnection, ParameterizedInsertQuery parameterizedQuery) {
        try (
                PreparedStatement preparedStatement = dbConnection.prepareStatement(parameterizedQuery.getQuery())
        ) {
            List<Object> parameters = parameterizedQuery.getParameters();

            int i = 1;
            for (Object parameter : parameters) {
                preparedStatement.setObject(i++, parameter);
            }

            preparedStatement.executeUpdate();

            return CompletableFuture.completedFuture(null);
        } catch (SQLException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public void executeSyncUpdate(java.sql.Connection dbConnection, ParameterizedInsertQuery parameterizedQuery) throws SQLException {
        try (
                PreparedStatement preparedStatement = dbConnection.prepareStatement(parameterizedQuery.getQuery())
        ) {
            List<Object> parameters = parameterizedQuery.getParameters();

            int i = 1;
            for (Object parameter : parameters) {
                preparedStatement.setObject(i++, parameter);
            }

            preparedStatement.executeUpdate();
        }
    }

    public void executeSyncUpdate(java.sql.Connection dbConnection, String query) throws SQLException {
        try (
                PreparedStatement preparedStatement = dbConnection.prepareStatement(query)
        ) {
            preparedStatement.executeUpdate();
        }
    }


}
