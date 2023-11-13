package datawake.datadriven.databasesync.shared.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import datawake.datadriven.databasesync.shared.enums.DriverClassNameEnum;
import datawake.datadriven.databasesync.shared.dtos.ParameterizedInsertQuery;
import org.hibernate.type.SqlTypes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class DatabaseConnectionUtil {
    /**
     * <h3>1. Columns</h3>
     * <h3>2. Table</h3>
     */
    public static final String SELECT_TEMPLATE = "SELECT %s FROM %s";

    /**
     * <h3>1. Table</h3>
     * <h3>2. Columns (separated by ',')</h3>
     * <h3>3. Values (separated by ',')</h3>
     */
    public static final String INSERT_TEMPLATE = "INSERT INTO %s ( %s ) VALUES ( %s )";

    /**
     * <h3>1. Table</h3>
     */
    public static final String DELETE_TEMPLATE = "DELETE FROM %s";

    /**
     * <h3>1. Table</h3>
     * <h3>2. Columns (separated by ',')</h3>
     */
    public static final String CREATE_TABLE_TEMPLATE = "CREATE TABLE %s ( %s )";

    /**
     * <h3>1. Table</h3>
     * <h3>2. Column</h3>
     */
    public static final String ADD_COLUMN_TEMPLATE = "ALTER TABLE %s ADD %s";

    /**
     * <h3>1. Table</h3>
     * <h3>2. Column</h3>
     */
    public static final String DROP_COLUMN_TEMPLATE = "ALTER TABLE %s DROP COLUMN %s";

    /**
     * <h3>1. Column</h3>
     * <h3>2. Type</h3>
     * <h3>3. Size</h3>
     * <h3>4. Additional Info</h3>
     */
    public static final String COLUMN_TEMPLATE = "%s %s ( %s ) %s";

    /**
     * <h3>1. Column</h3>
     * <h3>2. Type</h3>
     * <h3>3. Additional Info</h3>
     */
    public static final String SIMPLE_COLUMN_TEMPLATE = "%s %s %s";

    /**
     * <h3>1. Table</h3>
     * <h3>2. Constraint </h3>
     * <h3>3. Columns (separated by ',')</h3>
     */
    public static final String CONSTRAINT_PRIMARY_KEY_TEMPLATE =
            "ALTER TABLE %s ADD CONSTRAINT %s PRIMARY KEY (%s)";

    /**
     * <h3>1. Primary key</h3>
     */
    public static final String PRIMARY_KEY_TEMPLATE = "pk_%s";

    public static final int MAX_VARCHAR_SIZE = 4000;

    public static final Function<String, String> GET_MAX_VARCHAR_SIZE = (databaseName) ->
            databaseName.equals("Microsoft SQL Server")
                    ? "MAX"
                    : "4000";

    public static final String NOT_NULL = "NOT NULL";

    /**
     * Column name used just to be able to create a blank table
     */
    public static final String DEFAULT_PLACEHOLDER_COLUMN_NAME = "placeholder";

    /**
     * Column type used just to be able to create a blank table
     */
    public static final String DEFAULT_PLACEHOLDER_COLUMN_TYPE = "BIT";

    public static HikariDataSource createDataSource(DriverClassNameEnum driverClassName, String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClassName.getLabel());
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        return new HikariDataSource(config);
    }

    public static String generateInsertQuery(ResultSet result, String tableName) throws SQLException {
        ResultSetMetaData metaData = result.getMetaData();
        int columnCount = metaData.getColumnCount();

        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();

        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            String value = result.getString(columnIndex);
            String columnName = metaData.getColumnName(columnIndex);

            if (value == null) continue;

            if (columnIndex > 1) {
                fields.append(", ");
                values.append(",");
            }

            int typeIndex = metaData.getColumnType(columnIndex);
            JDBCType type = JDBCType.valueOf(typeIndex);

            String formattedValue = JDBCTypesUtil.transformIfString(type, value);

            fields.append(columnName);
            values.append(formattedValue);
        }

        return String.format(DatabaseConnectionUtil.INSERT_TEMPLATE, tableName, fields, values);
    }

    public static ParameterizedInsertQuery generateParameterizedInsertQuery(ResultSet result, String tableName) throws SQLException {
        ResultSetMetaData metaData = result.getMetaData();
        int columnCount = metaData.getColumnCount();

        StringBuilder fields = new StringBuilder();
        StringBuilder parameterMarks = new StringBuilder();

        List<Object> parameters = new ArrayList<>();

        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            Object value = result.getObject(columnIndex);
            String columnName = metaData.getColumnName(columnIndex);

            if (value == null) continue;

            if (columnIndex > 1) {
                fields.append(", ");
                parameterMarks.append(",");
            }

            fields.append(columnName);
            parameterMarks.append("?");
            parameters.add(value);
        }

        String query = String.format(DatabaseConnectionUtil.INSERT_TEMPLATE, tableName, fields, parameterMarks);

        return new ParameterizedInsertQuery(query, parameters);
    }

    public static String generateColumn(ResultSet columns, String dbName) throws SQLException {
        String columnName = columns.getString("COLUMN_NAME");
        String columnType = columns.getString("TYPE_NAME").toUpperCase();

        int precision = columns.getInt("COLUMN_SIZE");
        String size = precision > DatabaseConnectionUtil.MAX_VARCHAR_SIZE
                ? DatabaseConnectionUtil.GET_MAX_VARCHAR_SIZE.apply(dbName)
                : Integer.toString(precision);

        StringBuilder additionalInfo = new StringBuilder();

        if (Objects.equals(columns.getString("IS_NULLABLE"), "NO"))
            additionalInfo.append(DatabaseConnectionUtil.NOT_NULL);

        JDBCType type = JDBCType.valueOf(columns.getInt(5));

        return JDBCTypesUtil.ifStringType(type)
                ? String.format(DatabaseConnectionUtil.COLUMN_TEMPLATE, columnName, columnType, size, additionalInfo)
                : String.format(DatabaseConnectionUtil.SIMPLE_COLUMN_TEMPLATE, columnName, columnType, additionalInfo);
    }

    public static String generateColumnFromMetaData(ResultSetMetaData metaData, int columnIndex, String dbName, String primaryKeys) throws SQLException {
        String columnName = metaData.getColumnName(columnIndex);
        String columnType = metaData.getColumnTypeName(columnIndex).toUpperCase();

        int precision = metaData.getPrecision(columnIndex);
        String size = precision > DatabaseConnectionUtil.MAX_VARCHAR_SIZE
                ? DatabaseConnectionUtil.GET_MAX_VARCHAR_SIZE.apply(dbName)
                : Integer.toString(precision);

        StringBuilder additionalInfo = new StringBuilder();

        if (metaData.isNullable(columnIndex) == ResultSetMetaData.columnNoNulls || primaryKeys.contains(columnName))
            additionalInfo.append(DatabaseConnectionUtil.NOT_NULL);

        boolean isChar = SqlTypes.isCharacterType(metaData.getColumnType(columnIndex));

        return isChar
                ? String.format(DatabaseConnectionUtil.COLUMN_TEMPLATE, columnName, columnType, size, additionalInfo)
                : String.format(DatabaseConnectionUtil.SIMPLE_COLUMN_TEMPLATE, columnName, columnType, additionalInfo);
    }
}
