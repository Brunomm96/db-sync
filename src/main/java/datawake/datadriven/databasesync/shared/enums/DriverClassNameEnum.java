package datawake.datadriven.databasesync.shared.enums;

public enum DriverClassNameEnum {
    SQLServer("com.microsoft.sqlserver.jdbc.SQLServerDriver");

    private final String label;

    DriverClassNameEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }
}
