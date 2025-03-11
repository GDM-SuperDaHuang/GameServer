package com.slg.module.rpc;

import com.slg.module.rpc.server.PbMessageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
@RequiredArgsConstructor
public class GatewayServer implements CommandLineRunner {
    private int gatewayPort;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventLoopGroup downstreamGroup;

    @Autowired
    private final PbMessageHandler pbMessageHandler;

    private ConcurrentHashMap<Integer,Channel> channelMap;

    public GatewayServer(int gatewayPort) {
        this.gatewayPort = gatewayPort;
        this.channelMap = new ConcurrentHashMap();
    }

    //网关--用户
    public void start(int port) throws Exception {
        String str ="";
        for(int i = 0;i<str.length();i++){
            char indexStr = str.charAt(i);
            for(int j = 0;j<str.length();j++){
                String temp = indexStr + str.charAt(i)+"";

            }
        }
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    // 指定Channel
                    .channel(NioServerSocketChannel.class)
//                    .handler(new LoggingHandler())
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //非延迟，直接发送
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    //使用指定的端口设置套接字地址
                    .localAddress(new InetSocketAddress(port))
                    //服务端可连接队列数,对应TCP/IP协议listen函数中backlog参数,缓存连接
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
//                            //心跳 20
//                            p.addLast(new IdleStateHandler(20, 0, 0));
//                            p.addLast(new ChannelDuplexHandler() {
//                                @Override
//                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//                                    IdleStateEvent event = (IdleStateEvent) evt;
//                                    if (event.state() == IdleState.READER_IDLE) {
//                                        System.out.println("超时---------------");
//                                    }
////                                    super.userEventTriggered(ctx, evt);
//                                }
//                            });

                            //日志
//                            p.addLast("log",loggingHandler);
                            p.addLast(new MsgDecode());
                            p.addLast(pbMessageHandler);
//                            System.out.println("客户端连接成功");
                        }
                    });

            ChannelFuture f = b.bind().sync();
            //zk注册
//            toRegisterZK(port);
            System.out.println("=====服务器启动成功=====");
            f.channel().closeFuture().sync();
        } finally {
            System.out.println("----------------------------服务器关闭--------------------------------------------");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            // 关闭ZooKeeper客户端
//            client.close();
        }
    }

    /**
     * 网关-->目标服务器
     * 目标服务器
     * @param host h
     * @param port p
     * @param channelKey 服务器id
     */
    public void connectToDownstreamServer(String host, int port,int channelKey) throws InterruptedException {
        Bootstrap b = new Bootstrap();
        b.group(downstreamGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(pbMessageHandler);
                    }
                });

        ChannelFuture channelFuture = b.connect(host, port).sync();
        if (channelFuture.isSuccess()) {
            Channel downstreamChannel = channelFuture.channel();
            channelMap.put(channelKey,downstreamChannel);
            downstreamChannel.closeFuture().addListener(future -> {
                channelMap.remove(channelKey);
            });
        }
    }

    @Async
    @Override
    public void run(String... args) throws Exception {
        GatewayServer gatewayServer = new GatewayServer(7898);
        gatewayServer.start();
        // 连接到下游服务器
        //todo
        gatewayServer.connectToDownstreamServer("127.0.0.1", 8999,10000000);
        gatewayServer.connectToDownstreamServer("127.0.0.1", 8082,20000000);
        // 更多下游服务器...
    }
}