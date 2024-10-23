package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.enums.Profile;

import static org.example.config.PostgresConfigProperties.*;

public class ConfigPostgresDataSource {
    private static final HikariConfig config = new HikariConfig();
    private static HikariDataSource dataSource;

    public static HikariDataSource getPostgresDataSource(Profile profile) {
        if (dataSource == null) {
            switch (profile) {
                case PROD -> {
                    config.setJdbcUrl(ConfigLoader.get(POSTGRES_URL));
                    config.setUsername(ConfigLoader.get(POSTGRES_USER));
                    config.setPassword(ConfigLoader.get(POSTGRES_PASSWORD));
                    config.setMaximumPoolSize(Integer.parseInt(ConfigLoader.get(POSTGRES_POOL_SIZE))); // Adjust based on your needs
                    config.setAutoCommit(false);
                }
                case TEST -> {
                    config.setJdbcUrl("jdbc:postgresql://localhost:50650/postgres");
                    config.setUsername("postgres");
                    config.setPassword("postgres");
                    config.setMaximumPoolSize(10); // Adjust based on your needs
                    config.setAutoCommit(false);
                }
            }
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }
}
