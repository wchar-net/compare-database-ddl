package net.wchar.compare.database.ddl;

import lombok.extern.slf4j.Slf4j;
import net.wchar.compare.database.ddl.service.CompareService;
import net.wchar.compare.database.ddl.util.FileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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

    @Autowired
    private CompareService compareService;

    @Autowired
    private DataSource masterDataSource;

    @Autowired
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
        //No.1 比较表
        compareTables();
        //No.2 比较列信息
        compareTableColumn();
    }

    private void compareTableColumn() throws SQLException, IOException {
        //交集
        masterTables = compareService.getAllTable(masterDataSource, tablePrefix);
        slaveTables = compareService.getAllTable(slaveDataSource, tablePrefix);
        List<String> intersection = masterTables.stream().filter(item -> slaveTables.contains(item)).collect(Collectors.toList());
        for (String tableName : intersection) {
            List<String> columnWrapperList = compareService.getColumnWrapper(masterDataSource, slaveDataSource, tableName);
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

    private void compareTables() throws SQLException, IOException {
        masterTables = compareService.getAllTable(masterDataSource, tablePrefix);
        slaveTables = compareService.getAllTable(slaveDataSource, tablePrefix);
        //差集
        masterTables.removeAll(slaveTables);
        for (String masterTable : masterTables) {
            //建表语句
            String createTableSql = compareService.getCreateTableSql(masterTable, masterDataSource);
            //表注释
            String tableDescSql = compareService.getTableDesc(masterTable, masterDataSource);
            //列信息
            List<String> columnDescWrapperList = compareService.getColumnDesc(masterTable, masterDataSource);
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


}
