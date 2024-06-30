package org.example.config;

public class ConfigUponAppStart {
    public static void configApp() {
        System.setProperty("user.timezone", "UTC");
    }
}
