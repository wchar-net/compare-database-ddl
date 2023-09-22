package net.wchar.compare.database.ddl;

import lombok.extern.slf4j.Slf4j;
import net.wchar.compare.database.ddl.service.DataCompareService;
import net.wchar.compare.database.ddl.util.FileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/***
 * 测试类 从这里开始
 * @author Elijah
 */
@Slf4j
@SpringBootTest
class CompareDatabaseDDLApplicationTests {

    @Resource
    @Qualifier("oracleCompareServiceImpl")
    private DataCompareService dataCompareService;

    @Resource
    private DataSource masterDataSource;

    @Resource
    private DataSource slaveDataSource;

    private List<String> masterTables;
    private List<String> slaveTables;

    /**
     * 输出文件夹前缀
     */
    private String baseOutDir = "./compare";

    /**
     * 要比较得表前缀,为空则为所有
     */
    private String tablePrefix = "TUMP";


    @Test
    void contextLoads() throws SQLException, IOException {
        //No.1 验证
        checkDataSource();
        //No.2 比较表
        compareTables();
        //No.3 比较列信息
        compareTableColumn();
        log.info("比较完成 (●'◡'●)");
    }

    private void checkDataSource() throws SQLException {
        if (!dataCompareService.getDBType(masterDataSource).compare(dataCompareService.getDBType(slaveDataSource))) {
            throw new IllegalArgumentException("必须是相同得数据库才能比较!");
        }
    }

    private void compareTables() throws SQLException, IOException {
        masterTables = dataCompareService.getAllTable(masterDataSource, tablePrefix);
        slaveTables = dataCompareService.getAllTable(slaveDataSource, tablePrefix);
        //差集
        masterTables.removeAll(slaveTables);
        for (String masterTable : masterTables) {
            //建表语句
            String createTableSql = dataCompareService.getCreateTableSql(masterTable, masterDataSource);
            //表注释
            String tableDescSql = dataCompareService.getTableDesc(masterTable, masterDataSource);
            //列信息
            List<String> columnDescWrapperList = dataCompareService.getColumnDesc(masterTable, masterDataSource);
            log.info("表比较: {}", masterTable);
            //追加到文件
            String dirPath = baseOutDir + "/table/";
            if (!new File(dirPath).exists()) {
                new File(dirPath).mkdirs();
            }
            String filePath = dirPath + masterTable + ".sql";
            if (StringUtils.hasText(createTableSql)) {
                FileUtil.appendListToFile(Arrays.asList(createTableSql), filePath);
            }
            if (StringUtils.hasText(tableDescSql)) {
                FileUtil.appendListToFile(Arrays.asList(tableDescSql), filePath);
            }
            FileUtil.appendListToFile(columnDescWrapperList, filePath);
        }
    }

    private void compareTableColumn() throws SQLException, IOException {
        //交集
        masterTables = dataCompareService.getAllTable(masterDataSource, tablePrefix);
        slaveTables = dataCompareService.getAllTable(slaveDataSource, tablePrefix);
        List<String> intersection = masterTables.stream().filter(item -> slaveTables.contains(item)).collect(Collectors.toList());
        for (String tableName : intersection) {
            List<String> columnWrapperList = dataCompareService.getColumnWrapper(masterDataSource, slaveDataSource, tableName);
            log.info("列比较: {}", tableName);
            //追加到文件
            String dirPath = baseOutDir + "/column/";
            if (!new File(dirPath).exists()) {
                new File(dirPath).mkdirs();
            }
            String filePath = dirPath + tableName + ".sql";
            if (!CollectionUtils.isEmpty(columnWrapperList)) {
                FileUtil.appendListToFile(columnWrapperList, filePath);
            }
        }
    }


}
