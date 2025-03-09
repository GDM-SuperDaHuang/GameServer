package com.slg.module.rpc.client;

public class ServerConfig {
    private String host;
    private int port;
    private int channelKey;

    public ServerConfig(String host, int port, int channelKey) {
        this.host = host;
        this.port = port;
        this.channelKey = channelKey;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setChannelKey(int channelKey) {
        this.channelKey = channelKey;
    }

    public ServerConfig() {
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getChannelKey() {
        return channelKey;
    }
}
