package net.wchar.compare.database.ddl.service;

import net.wchar.compare.database.ddl.enums.DBType;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 比较 Service
 * @author Elijah
 */
public interface DataCompareService {
    /***
     * 获取所有表
     */
    List<String> getAllTable(DataSource dataSource, String likeRight) throws SQLException;

    /**
     * 获取建表语句
     */
    String getCreateTableSql(String tableName, DataSource dataSource) throws SQLException, IOException;

    /**
     * 获取表注释
     */
    String getTableDesc(String tableName, DataSource dataSource) throws SQLException;

    /**
     * 获取列注释
     */
    List<String> getColumnDesc(String tableName, DataSource dataSource) throws SQLException;


    /**
     * 获取列 名称、类型 、是否可为空、默认值、primary、unique
     */
    List<String> getColumnWrapper(DataSource masterDataSource, DataSource slaveDataSource, String tableName) throws SQLException, IOException;

    /**
     * 获取数据库类型
     */
    DBType getDBType(DataSource dataSource) throws SQLException;
}
