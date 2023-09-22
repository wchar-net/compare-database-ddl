package net.wchar.compare.database.ddl.dao.impl;

import net.wchar.compare.database.ddl.dao.DataBaseCompareDao;
import net.wchar.compare.database.ddl.wrapper.ColumnDescWrapper;
import net.wchar.compare.database.ddl.wrapper.ColumnWrapper;
import net.wchar.compare.database.ddl.wrapper.TablePrimaryWrapper;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service("oracleCompareDaoImpl")
public class OracleCompareDaoImpl implements DataBaseCompareDao {
    @Override
    public List<String> getAllTable(DataSource dataSource, String likeRight) throws SQLException {
        String sql;
        QueryRunner queryRunner = new QueryRunner(dataSource);
        if (StringUtils.hasText(likeRight)) {
            sql = "SELECT UPPER(T.TABLE_NAME) as tableName FROM user_tables T WHERE UPPER(T.TABLE_NAME) LIKE CONCAT(UPPER(?),'%')";
            return queryRunner.query(sql, new ColumnListHandler<>("tableName"), likeRight).stream().map(String::valueOf).collect(Collectors.toList());
        } else {
            sql = "SELECT UPPER(T.TABLE_NAME) as tableName FROM user_tables T";
            return queryRunner.query(sql, new ColumnListHandler<>("tableName")).stream().map(String::valueOf).collect(Collectors.toList());
        }
    }

    @Override
    public List<ColumnWrapper> getColumnWrapper(DataSource dataSource, String tableName) throws SQLException {
        String sql = "SELECT data_scale as dataScale, column_name as columnName, data_type as dataType, nullable, data_default as dataDefault,data_length as dataLength  FROM user_tab_columns WHERE table_name = ?";
        return new QueryRunner(dataSource).query(sql, rs -> {
            List<ColumnWrapper> list = new ArrayList<>();
            while (rs.next()) {
                list.add(
                        ColumnWrapper.builder()
                                .dataDefault(rs.getString("dataDefault"))
                                .columnName(rs.getString("columnName"))
                                .dataType(rs.getString("dataType"))
                                .dataLength(rs.getString("dataLength"))
                                .nullable(rs.getString("nullable"))
                                .dataScale(rs.getString("dataScale"))
                                .build()
                );
            }
            return list;
        }, tableName.toUpperCase());
    }
    @Override
    public List<TablePrimaryWrapper> getTablePrimaryList(String tableName, DataSource dataSource) throws SQLException {
        String sql = " SELECT  cc.column_name as columnName, c.constraint_type as primaryType,c.constraint_name as primaryName " +
                " FROM user_cons_columns cc " +
                " JOIN user_constraints c ON cc.constraint_name = c.constraint_name " +
                " WHERE (c.constraint_type = 'P'  or  c.constraint_type = 'U') " +
                "  AND cc.table_name = ? ";
        return new QueryRunner(dataSource).query(sql, resultSet -> {
            List<TablePrimaryWrapper> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(TablePrimaryWrapper.builder().columnName(resultSet.getString("columnName"))
                        .primaryName(resultSet.getString("primaryName")).primaryType(resultSet.getString("primaryType")).build());
            }
            return list;
        }, tableName.toUpperCase());
    }

    @Override
    public List<ColumnDescWrapper> getColumnDesc(String tableName, DataSource dataSource) throws SQLException {
        String sql = "SELECT column_name AS columnName, comments AS columnDesc FROM user_col_comments WHERE table_name = ?";
        return new QueryRunner(dataSource).query(sql, resultSet -> {
            List<ColumnDescWrapper> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(ColumnDescWrapper.builder().columnName(resultSet.getString("columnName"))
                        .columnDesc(resultSet.getString("columnDesc")).build());
            }
            return list;
        }, tableName.toUpperCase());
    }

    @Override
    public String getTableDesc(String tableName, DataSource dataSource) throws SQLException {
        String sql = "SELECT comments FROM user_tab_comments  WHERE table_name = ?";
        Object resultObj = new QueryRunner(dataSource).query(sql, new ScalarHandler<>(1), tableName.toUpperCase());
        if (null == resultObj) {
            return null;
        }
        String desc = resultObj.toString();
        return new StringBuilder().append("COMMENT ON TABLE ").append(tableName.toUpperCase()).append(" is ").append(desc).toString();
    }

    @Override
    public String getCreateTableSql(String tableName, DataSource dataSource) throws SQLException, IOException {
        String sql = "SELECT DBMS_METADATA.GET_DDL('TABLE', ?) AS ddl FROM DUAL";
        Clob clobResult = new QueryRunner(dataSource).query(sql, new ScalarHandler<>(1), tableName.toUpperCase());
        String createTableSql = clobToString(clobResult);
        if (!StringUtils.hasText(createTableSql)) {
            throw new IllegalArgumentException("查询到得建表sql为空! table: " + tableName.toUpperCase());
        }
        return handlerCreateTableSql(tableName, createTableSql);
    }

    private String clobToString(Clob clob) throws SQLException, IOException {
        try (Reader reader = clob.getCharacterStream()) {
            return IOUtils.toString(reader);
        }
    }

    private String handlerCreateTableSql(String tableName, String createTableSql) {
        int start = createTableSql.indexOf('(');
        int end = createTableSql.indexOf("SEGMENT");
        String subCreateTableSql = createTableSql.substring(start, end);
        String[] split = subCreateTableSql.split("\\n");
        StringBuilder sb = new StringBuilder();
        sb.append("create table ").append(tableName.toUpperCase());
        for (String line : split) {
            if (
                    line.indexOf("USING") != -1 ||
                            line.indexOf("STORAGE") != -1 ||
                            line.indexOf("PCTINCREASE") != -1 ||
                            line.indexOf("BUFFER_POOL") != -1 ||
                            line.indexOf("TABLESPACE") != -1
            ) {
            } else {
                sb.append(line).append("\n");
            }

        }
        return sb.toString();
    }
}
