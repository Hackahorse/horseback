package com.hackahorse.horseback.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropsLoader {

    private static final Logger log = java.util.logging.Logger.getLogger(PropsLoader.class.getName());

    private PropsLoader() {
        throw new IllegalStateException("Utility class");
    }

    public static Properties loadAppProps() throws IOException {
        InputStream input = PropsLoader.class.getClassLoader().getResourceAsStream("application.properties");
        Properties prop = new Properties();
        if (input == null) {
            log.log(Level.SEVERE, "Can''t to find application.properties");
            return null;
        }
        prop.load(input);
        return prop;
    }
    public static Properties loadConfigProps() throws IOException {
        InputStream input = PropsLoader.class.getClassLoader().getResourceAsStream("config.properties");
        Properties prop = new Properties();
        if (input == null) {
            log.log(Level.SEVERE, "Can''t to find config.properties");
            return null;
        }
        prop.load(input);
        return prop;
    }
}
