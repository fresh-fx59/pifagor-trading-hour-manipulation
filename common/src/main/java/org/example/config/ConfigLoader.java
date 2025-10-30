package org.example.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String propertyValue = properties.getProperty(key.getProperty());
        return resolveEnvVars(propertyValue);
    }

    /**
     * Get environment variable directly with validation.
     * Throws IllegalStateException with clear message if variable is not set.
     *
     * @param varName environment variable name
     * @return environment variable value
     * @throws IllegalStateException if environment variable is not set
     */
    public static String getRequiredEnv(String varName) {
        String value = System.getenv(varName);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException(
                    String.format("Required environment variable '%s' is not set. Please configure it in your .env file or system environment.", varName)
            );
        }
        return value;
    }

    /**
     * Get environment variable with default value.
     *
     * @param varName environment variable name
     * @param defaultValue default value if not set
     * @return environment variable value or default
     */
    public static String getEnvOrDefault(String varName, String defaultValue) {
        String value = System.getenv(varName);
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    /**
     * Returns input string with environment variable references expanded e.g. ${SOME_VAR}
     * <a href="https://stackoverflow.com/a/9725352/2266229">stack overflow</a>
     * @param input property value
     * @return processed ${SOME_VAR}, null or property as is
     **/
    private static String resolveEnvVars(String input)
    {
        if (null == input)
            return null;

        Pattern p = Pattern.compile("\\$\\{(\\w+)\\}");
        Matcher m = p.matcher(input);
        StringBuilder sb = new StringBuilder();
        while(m.find()){
            String envVarName = null == m.group(1) ? m.group(2) : m.group(1);
            String envVarValue = System.getenv(envVarName);
            m.appendReplacement(sb, null == envVarValue ? "" : envVarValue);
        }
        m.appendTail(sb);
        return sb.toString();
    }
}