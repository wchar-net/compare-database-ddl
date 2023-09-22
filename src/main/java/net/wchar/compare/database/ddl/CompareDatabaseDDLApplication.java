package net.wchar.compare.database.ddl;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动类
 * @author Elijah
 */
@Log4j2
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CompareDatabaseDDLApplication {
    public static void main(String[] args) {

    }
}