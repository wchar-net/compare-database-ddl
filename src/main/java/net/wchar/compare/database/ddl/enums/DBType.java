package net.wchar.compare.database.ddl.enums;

import net.wchar.compare.database.ddl.exception.NotSupportDBException;

public enum DBType {
    MYSQL("MYSQL"), ORACLE("ORACLE"), SQLSERVER("SQLSERVER");

    DBType(String type) {
        this.type = type;
    }

    public boolean compare(DBType dbType) {
        return this.type.equalsIgnoreCase(dbType.type);
    }

    public static DBType parse(String dbType) {
        if (ORACLE.type.equalsIgnoreCase(dbType)) {
            return ORACLE;
        } else if (MYSQL.type.equalsIgnoreCase(dbType)) {
            return MYSQL;
        } else if (SQLSERVER.type.equalsIgnoreCase(dbType)) {
            return SQLSERVER;
        } else {
            throw new NotSupportDBException("不支持得数据库类型! " + dbType);
        }
    }

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
