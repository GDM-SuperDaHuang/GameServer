package com.slg.commom.connection;

import java.util.concurrent.ConcurrentHashMap;

public class BackendConnectionManager {
    private final ConcurrentHashMap<String, BackendConnection> backendConnections = new ConcurrentHashMap<>();

    public void addBackendConnection(String key, BackendConnection connection) {
        backendConnections.put(key, connection);
    }

    public BackendConnection getBackendConnection(String key) {
        return backendConnections.get(key);
    }
}
