package net.wchar.compare.database.ddl.dao;

import net.wchar.compare.database.ddl.wrapper.ColumnDescWrapper;
import net.wchar.compare.database.ddl.wrapper.ColumnWrapper;
import net.wchar.compare.database.ddl.wrapper.TablePrimaryWrapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface DataBaseCompareDao {
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
    List<ColumnDescWrapper> getColumnDesc(String tableName, DataSource dataSource) throws SQLException;

    /**
     * 获取表中得 primary unique
     */
    List<TablePrimaryWrapper> getTablePrimaryList(String tableName, DataSource dataSource) throws SQLException;



    /**
     * 获取列 名称、类型 、是否可为空、默认值
     */
    List<ColumnWrapper> getColumnWrapper(DataSource dataSource, String tableName) throws SQLException;
}
