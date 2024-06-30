package org.example.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class ConfigLoader {
    private static final Properties properties = new Properties();
    private static final String propertyFile = "application.properties";

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(propertyFile)) {
            if (input == null)
                log.error("Sorry, unable to find {}", propertyFile);
            properties.load(input);
        } catch (IOException ex) {
            log.error("Failed to load properties", ex);
        }
    }

    public static String get(ConfigProperties key) {
        return properties.getProperty(key.getProperty());
    }
}