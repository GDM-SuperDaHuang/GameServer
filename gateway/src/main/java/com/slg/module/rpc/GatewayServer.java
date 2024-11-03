package com.slg.module.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
@RequiredArgsConstructor
public class GatewayServer implements CommandLineRunner {
    private int gatewayPort;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventLoopGroup downstreamGroup;

    private ConcurrentHashMap<Integer,Channel> channelMap;

    public GatewayServer(int gatewayPort) {
        this.gatewayPort = gatewayPort;
        this.channelMap = new ConcurrentHashMap();
    }

    //网关--用户
    public void start() throws Exception {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        downstreamGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                // 指定Channel
                .channel(NioServerSocketChannel.class)
//                    .handler(new LoggingHandler())
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                //非延迟，直接发送
                .childOption(ChannelOption.TCP_NODELAY, true)
                //服务端可连接队列数,对应TCP/IP协议listen函数中backlog参数,缓存连接
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new MsgDecode());
                        p.addLast(new GatewayServerHandler());
                        System.out.println("客户端连接网关成功");

                    }
                });

        // 绑定端口并启动网关服务器
        ChannelFuture f = b.bind(gatewayPort).sync();
        f.channel().closeFuture().sync();
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
                        ch.pipeline().addLast(new DownstreamServerHandler());
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