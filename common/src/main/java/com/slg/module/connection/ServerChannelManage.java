package com.slg.module.connection;

import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 目标服务器内部消息管理
 */
@Component
public class ServerChannelManage {
    //目标服务器连接管理
    private Map<Integer, Channel> serverChannelMap = new ConcurrentHashMap<>();
    public ServerChannelManage() {
    }
    public Channel get(int serverId){
        return serverChannelMap.getOrDefault(serverId, null);
    }
    public void remove(int serverId){
        serverChannelMap.remove(serverId);
    }
    public void put(int ip,Channel channel){
        serverChannelMap.put(ip,channel);
    }
}
