package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConfigClickhouseDataSource {

    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;

    static {
        config.setJdbcUrl(ConfigLoader.get(ClickhouseConfigPropertiesImpl.CLICKHOUSE_URL));
        config.setUsername(ConfigLoader.get(ClickhouseConfigPropertiesImpl.CLICKHOUSE_USER));
        config.setPassword(ConfigLoader.get(ClickhouseConfigPropertiesImpl.CLICKHOUSE_PASSWORD));
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        config.setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        ds = new HikariDataSource( config );
    }

    private ConfigClickhouseDataSource() {}

    public static Connection getClickhouseConnection() throws SQLException {
        return ds.getConnection();
    }
}
