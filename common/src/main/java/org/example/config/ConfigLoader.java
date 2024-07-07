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