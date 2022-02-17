package it.lockless.psidemoserver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Database configuration.
 * Note: this configuration runs the DB in-memory and it is intended to be used only for testing purpose.
 * Replace with a proper stable-storage DB to use it in a business environment.
 */

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${db.host}")
    private String dbUrl;

    @Bean
    public DataSource getDataSource() {
        log.info("Calling getDataSource");
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        dataSource.setUsername("");
        dataSource.setPassword("");
        return dataSource;
    }

}
