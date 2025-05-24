package com.slg.module.rpc.client;

public class ServerConfig {
    private String host;
    private int port;
    private int serverId;

    public ServerConfig() {
    }

    public ServerConfig(String host, int port, int serverId) {
        this.host = host;
        this.port = port;
        this.serverId = serverId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }
}
