package net.wchar.compare.database.ddl.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class SlaveDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari.slave")
    public HikariConfig slaveHikariConfig() {
        return new HikariConfig();
    }

    @Bean
    public DataSource slaveDataSource(@Qualifier("slaveHikariConfig") HikariConfig slaveHikariConfig) {
        return new HikariDataSource(slaveHikariConfig);
    }


}
