package com.molla.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Value("${MYSQLDATABASE_URL:}")
    private String mysqlDatabaseUrl;

    @Value("${MYSQLHOST:}")
    private String mysqlHost;

    @Value("${MYSQLPORT:3306}")
    private String mysqlPort;

    @Value("${MYSQLDATABASE:}")
    private String mysqlDatabase;

    @Value("${MYSQLUSER:}")
    private String mysqlUser;

    @Value("${MYSQLPASSWORD:}")
    private String mysqlPassword;

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties properties = new DataSourceProperties();
        
        // Priority 1: If Railway's MYSQLDATABASE_URL is provided, parse it
        if (mysqlDatabaseUrl != null && !mysqlDatabaseUrl.isEmpty()) {
            try {
                // Railway format: mysql://user:password@host:port/database
                URI dbUri = new URI(mysqlDatabaseUrl.replace("mysql://", "http://"));
                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String host = dbUri.getHost();
                int port = dbUri.getPort() == -1 ? 3306 : dbUri.getPort();
                String database = dbUri.getPath().replaceFirst("/", "");
                
                properties.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
                properties.setUsername(username);
                properties.setPassword(password);
                return properties;
            } catch (Exception e) {
                // If parsing fails, continue to check individual variables
            }
        }
        
        // Priority 2: Use Railway's individual MySQL variables
        if (mysqlHost != null && !mysqlHost.isEmpty() && 
            mysqlDatabase != null && !mysqlDatabase.isEmpty()) {
            properties.setUrl("jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase);
            if (mysqlUser != null && !mysqlUser.isEmpty()) {
                properties.setUsername(mysqlUser);
            }
            if (mysqlPassword != null && !mysqlPassword.isEmpty()) {
                properties.setPassword(mysqlPassword);
            }
            return properties;
        }
        
        // Priority 3: Fall back to application.properties values (SPRING_DATASOURCE_URL, etc.)
        // Spring Boot will use the values from application.properties
        
        return properties;
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}
