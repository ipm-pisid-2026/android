package com.maze;

public final class ApiConfig {

    private static final int DEFAULT_API_PORT = 5000;

    private ApiConfig() {
    }

    public static String buildEndpoint(String host, String path) {
        String base = buildBaseUrl(host);
        if (path.startsWith("/")) {
            return base + path;
        }
        return base + "/" + path;
    }

    private static String buildBaseUrl(String host) {
        if (host == null || host.isBlank()) {
            return "http://localhost:" + DEFAULT_API_PORT;
        }

        String normalizedHost = host.trim();
        if (normalizedHost.startsWith("http://") || normalizedHost.startsWith("https://")) {
            return normalizedHost;
        }

        if (normalizedHost.contains(":")) {
            return "http://" + normalizedHost;
        }

        return "http://" + normalizedHost + ":" + DEFAULT_API_PORT;
    }
}