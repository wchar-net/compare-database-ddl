package net.wchar.compare.database.ddl.service.impl;

import lombok.extern.log4j.Log4j2;
import net.wchar.compare.database.ddl.dao.DataBaseCompareDao;
import net.wchar.compare.database.ddl.enums.DBType;
import net.wchar.compare.database.ddl.exception.NotSupportDBException;
import net.wchar.compare.database.ddl.service.CompareService;
import net.wchar.compare.database.ddl.wrapper.ColumnDescWrapper;
import net.wchar.compare.database.ddl.wrapper.ColumnWrapper;
import net.wchar.compare.database.ddl.wrapper.TablePrimaryWrapper;
import org.apache.commons.dbutils.DbUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
public class CompareServiceImpl implements CompareService {


    @Override
    public List<String> getAllTable(DataSource dataSource, String likeRight) throws SQLException {
        return getDBType(dataSource).getAllTable(dataSource, likeRight);
    }

    @Override
    public String getCreateTableSql(String tableName, DataSource dataSource) throws SQLException, IOException {
        return getDBType(dataSource).getCreateTableSql(tableName, dataSource);
    }

    @Override
    public String getTableDesc(String tableName, DataSource dataSource) throws SQLException {
        return getDBType(dataSource).getTableDesc(tableName, dataSource);
    }

    @Override
    public List<String> getColumnWrapper(DataSource masterDataSource, DataSource slaveDataSource, String tableName) throws SQLException, IOException {
        List<ColumnWrapper> masterColumnWrapper = getDBType(masterDataSource).getColumnWrapper(masterDataSource, tableName);
        List<ColumnWrapper> slaveColumnWrapper = getDBType(slaveDataSource).getColumnWrapper(slaveDataSource, tableName);
        List<String> result = new ArrayList<>();
        //No.1 列名称差集
        List<ColumnWrapper> difference = differenceColumnWrapper(masterColumnWrapper, slaveColumnWrapper);
        for (ColumnWrapper columnWrapper : difference) {
            ColumnWrapper finedColumnWrapper = findColumnWrapper(columnWrapper.getColumnName(), masterColumnWrapper);
            result.add(builderColumnSql(finedColumnWrapper, tableName));
            String columnDesc = builderColumnDesc(finedColumnWrapper, tableName, masterDataSource);
            if (StringUtils.hasText(columnDesc)) {
                result.add(columnDesc);
            }
        }
        //No.2 列索引
        List<String> columnPrimaryList = builderColumnPrimary(tableName, masterDataSource, slaveDataSource);
        if (!CollectionUtils.isEmpty(columnPrimaryList)) {
            result.addAll(columnPrimaryList);
        }
        return result;
    }

    private String builderColumnDesc(ColumnWrapper finedColumnWrapper, String tableName, DataSource masterDataSource) throws SQLException {
        List<ColumnDescWrapper> columnDescWrapperList = getDBType(masterDataSource).getColumnDesc(tableName, masterDataSource);
        String columnName = finedColumnWrapper.getColumnName();
        for (ColumnDescWrapper columnDescWrapper : columnDescWrapperList) {
            if (!columnName.equalsIgnoreCase(columnDescWrapper.getColumnName())) {
                continue;
            }
            if (StringUtils.hasText(columnDescWrapper.getColumnDesc())) {
                return new StringBuilder().append("COMMENT ON COLUMN ").append(tableName).append(".").append("\"").append(columnDescWrapper.getColumnName()).append("\"")
                        .append(" IS ").append("'").append(columnDescWrapper.getColumnDesc()).append("'").toString();

            }
        }
        return null;
    }

    private String builderColumnSql(ColumnWrapper masterColumnInfoBo, String tableName) {
        StringBuilder sb = new StringBuilder()
                .append("ALTER TABLE ").append(tableName.toUpperCase())
                .append(" ADD ").append(masterColumnInfoBo.getColumnName()).append(" ");
        if (
                masterColumnInfoBo.getDataType().equalsIgnoreCase("date") ||
                        masterColumnInfoBo.getDataType().equalsIgnoreCase("clob") ||
                        masterColumnInfoBo.getDataType().equalsIgnoreCase("timestamp") ||
                        masterColumnInfoBo.getDataType().equalsIgnoreCase("blob") ||
                        masterColumnInfoBo.getDataType().equalsIgnoreCase("nclob") ||
                        masterColumnInfoBo.getDataType().equalsIgnoreCase("long")

        ) {
            sb.append(masterColumnInfoBo.getDataType());
        } else if (masterColumnInfoBo.getDataType().equalsIgnoreCase("number")) {
            if (StringUtils.hasText(masterColumnInfoBo.getDataScale())) {
                sb.append(masterColumnInfoBo.getDataType()).append("(").append(masterColumnInfoBo.getDataLength()).append(",").append(masterColumnInfoBo.getDataScale()).append(")");
            } else {
                sb.append(masterColumnInfoBo.getDataType());
            }
        } else {
            sb.append(masterColumnInfoBo.getDataType()).append("(").append(masterColumnInfoBo.getDataLength()).append(")");
        }

        if (StringUtils.hasText(masterColumnInfoBo.getDataDefault())) {
            sb.append(" default ").append(masterColumnInfoBo.getDataDefault().replace("\n", ""));
        }
        sb.append(" ").append("Y".equalsIgnoreCase(masterColumnInfoBo.getNullable()) ? " NULL" : " NOT NULL");
        return sb.toString();
    }


    private ColumnWrapper findColumnWrapper(String columnName, List<ColumnWrapper> columnWrapperList) {
        for (ColumnWrapper columnWrapper : columnWrapperList) {
            if (columnName.equalsIgnoreCase(columnWrapper.getColumnName())) {
                return columnWrapper;
            }
        }
        return null;
    }

    private List<ColumnWrapper> differenceColumnWrapper(List<ColumnWrapper> masterColumnWrapper, List<ColumnWrapper> slaveColumnWrapper) {
        List<String> masterColumnNameList = masterColumnWrapper.stream().map(ColumnWrapper::getColumnName).collect(Collectors.toList());
        masterColumnNameList.removeAll(slaveColumnWrapper.stream().map(ColumnWrapper::getColumnName).collect(Collectors.toList()));
        List<ColumnWrapper> result = new ArrayList<>();
        for (String columnName : masterColumnNameList) {
            for (ColumnWrapper columnWrapper : masterColumnWrapper) {
                if (columnName.equalsIgnoreCase(columnWrapper.getColumnName())) {
                    result.add(columnWrapper);
                }
            }
        }
        return result;
    }

    private TablePrimaryWrapper findTablePrimaryWrapper(String columnName, List<TablePrimaryWrapper> tablePrimaryWrapperList) {
        if (CollectionUtils.isEmpty(tablePrimaryWrapperList)) {
            return null;
        }
        if (!StringUtils.hasText(columnName)) {
            return null;
        }
        for (TablePrimaryWrapper primaryWrapper : tablePrimaryWrapperList) {
            if (columnName.equalsIgnoreCase(primaryWrapper.getColumnName())) {
                return primaryWrapper;
            }
        }
        return null;
    }

    private List<String> builderColumnPrimary(String tableName, DataSource masterDataSource, DataSource slaveDataSource) throws SQLException, IOException {
        List<String> result = new ArrayList<>();
        List<TablePrimaryWrapper> masterTablePrimaryList = getDBType(masterDataSource).getTablePrimaryList(tableName, masterDataSource);
        List<TablePrimaryWrapper> slaveTablePrimaryList = getDBType(masterDataSource).getTablePrimaryList(tableName, slaveDataSource);
        if (CollectionUtils.isEmpty(masterTablePrimaryList)) {
            return result;
        }
        String slaveDataCreateTableSql;
        try {
            slaveDataCreateTableSql = getDBType(slaveDataSource).getCreateTableSql(tableName, slaveDataSource);
        } catch (SQLException | IOException e) {
            //slave表不存在
            return result;
        }
        for (TablePrimaryWrapper masterPrimaryWrapper : masterTablePrimaryList) {
            if (StringUtils.hasText(masterPrimaryWrapper.getPrimaryType())
                    && StringUtils.hasText(masterPrimaryWrapper.getPrimaryName()
            )) {
                TablePrimaryWrapper slavePrimaryWrapper = findTablePrimaryWrapper(masterPrimaryWrapper.getColumnName(), slaveTablePrimaryList);
                String primarySql = null;
                if (null == slavePrimaryWrapper) {
                    //主键信息不存在
                    primarySql = getColumnPrimary(false, slaveDataCreateTableSql, tableName, masterPrimaryWrapper.getPrimaryType(), masterPrimaryWrapper.getPrimaryName(), masterPrimaryWrapper.getColumnName());
                } else {
                    //主键信息存在 只比较 主为P 副为U 且副必须没有P
                    if (!slavePrimaryWrapper.getPrimaryType().equalsIgnoreCase(masterPrimaryWrapper.getPrimaryType())) {
                        long count = slaveTablePrimaryList.stream().filter(t -> "U".equalsIgnoreCase(t.getPrimaryType())).count();
                        if ("U".equalsIgnoreCase(slavePrimaryWrapper.getPrimaryType())
                                && "P".equalsIgnoreCase(masterPrimaryWrapper.getPrimaryType())
                                && count <= 0
                        ) {
                            primarySql = getColumnPrimary(true, slaveDataCreateTableSql, tableName, masterPrimaryWrapper.getPrimaryType(), masterPrimaryWrapper.getPrimaryName(), masterPrimaryWrapper.getColumnName());
                        }
                    }
                }
                if (StringUtils.hasText(primarySql)) {
                    result.add(primarySql);
                }

            }
        }
        return result;
    }

    private String getColumnPrimary(boolean findCreateTableSqlFlag, String createTableSql, String tableName, String primaryType, String primaryName, String columnName) {
        String result = null;
        if ("p".equalsIgnoreCase(primaryType)) {
            if (findCreateTableSqlFlag) {
                if (!findCreateTableSqlIsPrimaryByColumn("primary", createTableSql, columnName)) {
                    result = new StringBuilder().append("CREATE INDEX ").append(primaryName).append(" ON ")
                            .append(tableName).append(" ( \"").append(columnName).append("\" ) ").toString();
                }
            } else {
                result = new StringBuilder().append("CREATE INDEX ").append(primaryName).append(" ON ")
                        .append(tableName).append(" ( \"").append(columnName).append("\" ) ").toString();
            }

        } else if ("u".equalsIgnoreCase(primaryType)) {
            if (findCreateTableSqlFlag) {
                if (!findCreateTableSqlIsPrimaryByColumn("unique", createTableSql, columnName)) {
                    result = new StringBuilder().append("CREATE UNIQUE INDEX ").append(primaryName).append(" ON ")
                            .append(tableName).append(" ( \"").append(columnName).append("\" ) ").toString();
                }
            } else {
                result = new StringBuilder().append("CREATE UNIQUE INDEX ").append(primaryName).append(" ON ")
                        .append(tableName).append(" ( \"").append(columnName).append("\" ) ").toString();
            }
        } else {
            //当前只支持 P = primary  和 U = unique
            log.info(
                    "当前只支持 P = primary  和 U = unique. 不支持得主键类型 = table name: {} column: {} primaryType: {}",
                    tableName, columnName, primaryType
            );
        }
        return result;
    }

    private boolean findCreateTableSqlIsPrimaryByColumn(String primaryType, String createTableSql, String columnName) {
        boolean flag;
        createTableSql = createTableSql.toUpperCase();
        int start = createTableSql.lastIndexOf(primaryType.toUpperCase());
        if (start == -1) {
            flag = false;
        } else {
            String subStr = createTableSql.substring(start, createTableSql.length());
            int end = subStr.indexOf(")");
            if (end == -1) {
                flag = false;
            } else {
                String endSubStr = subStr.substring(0, end);
                if (endSubStr.contains(columnName.toUpperCase())) {
                    flag = true;
                } else {
                    flag = false;
                }
            }
        }
        return flag;
    }

    @Override
    public List<String> getColumnDesc(String tableName, DataSource dataSource) throws SQLException {
        List<ColumnDescWrapper> columnDescWrapperList = getDBType(dataSource).getColumnDesc(tableName, dataSource);
        List<String> result = new ArrayList<>();
        for (ColumnDescWrapper columnDescWrapper : columnDescWrapperList) {
            if (StringUtils.hasText(columnDescWrapper.getColumnDesc())) {
                String comment = new StringBuilder().append("COMMENT ON COLUMN ").append(tableName).append(".").append("\"").append(columnDescWrapper.getColumnName()).append("\"")
                        .append(" IS ").append("'").append(columnDescWrapper.getColumnDesc()).append("'").toString();
                result.add(comment);
            }
        }
        return result;
    }

    private DataBaseCompareDao getDBType(DataSource dataSource) throws SQLException {
        Connection connection = dataSource.getConnection();
        DatabaseMetaData metaData = connection.getMetaData();
        DbUtils.close(connection);
        if (null == metaData) {
            throw new NotSupportDBException("不支持得数据库,获取metaData为空!");
        }
        String databaseProductName = metaData.getDatabaseProductName();
        if (!StringUtils.hasText(databaseProductName)) {
            throw new NotSupportDBException("不支持得数据库,获取databaseProductName为空!");
        }
        if (DBType.ORACLE.getType().equalsIgnoreCase(databaseProductName)) {
            return oracleCompareDaoImpl;
        } else {
            throw new NotSupportDBException("不支持得数据库,目前只能使用Oracle!");
        }

    }


    @Resource
    @Qualifier("oracleCompareDaoImpl")
    private DataBaseCompareDao oracleCompareDaoImpl;
}