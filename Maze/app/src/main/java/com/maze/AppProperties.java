package com.maze;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppProperties {

    private static final String FILE_NAME = "config.properties";
    private final Properties properties = new Properties();

    private AppProperties(Context context) {
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open(FILE_NAME)) {
            properties.load(inputStream);
        } catch (IOException ignored) {
            // Use built-in defaults when the file is missing.
        }
    }

    public static AppProperties load(Context context) {
        return new AppProperties(context.getApplicationContext());
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    public String resolveLoginHost() {
        String runMode = get("run.mode", "emulator");
        if ("phone".equalsIgnoreCase(runMode)) {
            return get("phone.host", "192.168.1.100");
        }
        return get("emulator.host", "10.0.2.2");
    }
}
