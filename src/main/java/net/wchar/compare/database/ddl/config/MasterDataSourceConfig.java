package net.wchar.compare.database.ddl.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 主数据源
 * @author Elijah
 */
@Configuration
public class MasterDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari.master")
    public HikariConfig masterHikariConfig() {
        return new HikariConfig();
    }

    @Bean
    public DataSource masterDataSource(@Qualifier("masterHikariConfig") HikariConfig masterHikariConfig) {
        return new HikariDataSource(masterHikariConfig);
    }
}
