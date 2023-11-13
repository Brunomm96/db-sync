package datawake.datadriven.databasesync.shared.utils;

import java.sql.JDBCType;

public class JDBCTypesUtil {
    private static final String stringTemplate = "'%s'";

    public static String transformIfString(JDBCType type, String value) {
        return ifStringType(type) ? String.format(stringTemplate, value) : value;
    }

    public static boolean ifStringType(JDBCType type){
        return switch (type) {
            case CHAR, VARCHAR, LONGVARCHAR, NCHAR, NVARCHAR, LONGNVARCHAR -> true;

            default -> false;
        };
    }
}
