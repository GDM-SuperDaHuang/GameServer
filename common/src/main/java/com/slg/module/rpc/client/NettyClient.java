package com.slg.module.rpc.client;

import com.google.protobuf.GeneratedMessage;
import com.slg.module.connection.ServerChannelManage;
import com.slg.module.message.ByteBufferMessage;
import com.slg.module.rpc.msgDECode.MsgDecode;
import com.slg.module.util.UniqueCidGenerator;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 远程调用
 */
@Component
public class NettyClient {
    @Autowired
    private DownstreamServerHandler downstreamServerHandler;

    @Autowired
    private ServerChannelManage serverChannelManage;
    private final EventLoopGroup downstreamGroup = new NioEventLoopGroup(1);
    private final Bootstrap bootstrap;

    public NettyClient() {
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

//    //todo 注册中心配置
//    public ServerConfig getChannelKey(int protocolId) {
//        ServerConfig serverConfig;
//        if (protocolId > 2000) {
//            serverConfig = serverConfig1;
//        } else {
//            serverConfig = serverConfig2;
//        }
//        return serverConfig;
//    }

    /**
     * 连接远程节点之间；
     *
     * @param host     服务器地址
     * @param port     服务器端口
     * @param serverId 服务器标识
     * @throws InterruptedException
     */
    public Channel connectToDownstreamServer(String host, int port, int serverId) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
        if (channelFuture.isSuccess()) {
            Channel downstreamChannel = channelFuture.channel();
            serverChannelManage.put(serverId, downstreamChannel);
            downstreamChannel.closeFuture().addListener(future -> {
                serverChannelManage.remove(serverId);
            });
            return downstreamChannel;
        }
        return null;
    }

}
