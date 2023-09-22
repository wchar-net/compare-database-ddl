package net.wchar.compare.database.ddl;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@Log4j2
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CompareDatabaseDDLApplication {
    public static void main(String[] args) {

    }
}