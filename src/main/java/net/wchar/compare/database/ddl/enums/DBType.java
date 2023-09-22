package net.wchar.compare.database.ddl.enums;

public enum DBType {
    MYSQL("MYSQL"), ORACLE("ORACLE"), SQLSERVER("SQLSERVER");

    DBType(String type) {
        this.type = type;
    }

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
