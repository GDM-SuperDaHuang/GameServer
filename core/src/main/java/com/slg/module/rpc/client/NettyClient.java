package com.slg.module.rpc.client;

import com.slg.module.rpc.msgDECode.MsgDecode;
import com.slg.module.util.SentMsgUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyClient {

    private static final NettyClient INSTANCE = new NettyClient();
    private final EventLoopGroup downstreamGroup = new NioEventLoopGroup(1);
    private final DownstreamServerHandler downstreamServerHandler = new DownstreamServerHandler(SentMsgUtil.getInstance());
    public Map<Integer, Channel> serverChannelMap = new ConcurrentHashMap<>();//serverId--channel
    private final Bootstrap bootstrap;

    private NettyClient() {
        bootstrap = new Bootstrap();
        bootstrap.group(downstreamGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new MsgDecode());
                        p.addLast(downstreamServerHandler);
                    }
                });
    }

    public static NettyClient getInstance() {
        return INSTANCE;
    }

    public Channel connectToDownStreamServer(String host, int port, int serverId) {
        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.connect(host, port).sync();
        } catch (Exception e) {
            System.out.println("连接失败，远程调用失败");
            return null;
        }
        if (channelFuture.isSuccess()) {
            Channel downstreamChannel = channelFuture.channel();
            serverChannelMap.put(serverId, downstreamChannel);
            downstreamChannel.closeFuture().addListener(future -> {
                serverChannelMap.remove(serverId);
            });
            return downstreamChannel;
        }
        return null;
    }

    public void shutdown() {
        downstreamGroup.shutdownGracefully();
    }
}
