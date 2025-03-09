package com.slg.module.rpc.client;

import com.google.protobuf.GeneratedMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 远程调用
 */
@Component
public class NettyClient {
    int msgHeadMax = 16;
    int interval = 100000000;

    //todo 注册中心配置
    public ServerConfig getChannelKey(int protocolId) {
        int serverId = protocolId / interval + 1;
        //注册中心配置 todo
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setHost("127.0.0.1");
        serverConfig.setPort(7779);
        serverConfig.setChannelKey(2);
        return serverConfig;
    }

    @Autowired
    private DownstreamServerHandler downstreamServerHandler;

    private ConcurrentHashMap<Integer, Channel> channelMap = new ConcurrentHashMap<>();
    EventLoopGroup downstreamGroup = new NioEventLoopGroup(1);

    public NettyClient() {
    }

    /**
     * 连接远程服务器
     * @param host 服务器地址
     * @param port 服务器端口
     * @param channelKey 服务器标识
     * @throws InterruptedException
     */
    public void connectToDownstreamServer(String host, int port, int channelKey) throws InterruptedException {
        Bootstrap b = new Bootstrap();
//        b.group(downstreamGroup)
//                .channel(NioSocketChannel.class);
        b.group(downstreamGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline p = ch.pipeline();
//                        p.addLast(new MsgDecode());
                        p.addLast(downstreamServerHandler);
                    }
                });

        ChannelFuture channelFuture = b.connect(host, port).sync();
        if (channelFuture.isSuccess()) {
            Channel downstreamChannel = channelFuture.channel();
            channelMap.put(channelKey, downstreamChannel);
            downstreamChannel.closeFuture().addListener(future -> {
                channelMap.remove(channelKey);
            });
        }
    }


    //远程调用
    public void sentMsg(int protocolId, GeneratedMessage.Builder<?> builder) throws InterruptedException {
        ServerConfig serverConfig = getChannelKey(protocolId);
        int channelKey = serverConfig.getChannelKey();

        //选择一个连接一个服务器
        Channel channel = channelMap.get(serverConfig.getChannelKey());
        if (channel == null) {
            String host = serverConfig.getHost();
            int port = serverConfig.getPort();
            connectToDownstreamServer(host, port, channelKey);
            channel = channelMap.get(channelKey);
        }
        byte[] msg = builder.buildPartial().toByteArray();
        ByteBuf buf = Unpooled.buffer(msgHeadMax);
        //消息头
        buf.writeLong(0L);
        buf.writeInt(protocolId);
        buf.writeByte(0);
        buf.writeByte(0);
        int length = msg.length;
        buf.writeShort(length);

        //消息体
        buf.writeBytes(msg);
        ChannelFuture future = channel.writeAndFlush(buf);
    }

}
