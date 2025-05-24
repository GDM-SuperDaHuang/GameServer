package com.slg.module.config;


@ConfigManager.KVConfig(path = "fightServer.cfg")
public class FightAppConfig extends GameConfig {

    private final int serverId;

    private final int serverType;

    private final String serverName;

    public FightAppConfig (){
        this.serverId = 0;
        this.serverType = 0;
        this.serverName = "";
    }


    public int getServerId() {
        return serverId;
    }

    public int getServerType() {
        return serverType;
    }

    public String getServerName() {
        return serverName;
    }



}
