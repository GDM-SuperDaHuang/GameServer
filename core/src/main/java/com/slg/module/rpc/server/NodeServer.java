package com.slg.module.rpc.server;

import com.slg.module.rpc.msgDECode.MsgDecode;
import com.slg.module.util.ConfigReader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

public class NodeServer {
    private int port;

    public int getPort() {
        return port;
    }

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup = new NioEventLoopGroup(6);
    private final PbMessageHandler pbMessageHandler = new PbMessageHandler();
    private ChannelFuture serverChannelFuture;
    LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);


    public NodeServer() {
        try {
            ConfigReader config = new ConfigReader("application.properties");
            port = config.getIntProperty("netty.server.port");
            int protoIdMin = config.getIntProperty("server.proto-id-min");
            int protoIdMax = config.getIntProperty("server.proto-id-max");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //读取配置
        if (Epoll.isAvailable() && System.getProperty("os.name", "").toLowerCase().contains("linux")) {
            bossGroup = new EpollEventLoopGroup(1);
        } else {
            bossGroup = new NioEventLoopGroup(1);
        }

    }

    public void start(int port) {
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
                        //日志
//                            p.addLast("log",loggingHandler);
                        p.addLast(new MsgDecode());
                        p.addLast(pbMessageHandler);
//                            System.out.println("客户端连接成功");
                    }
                });
        serverChannelFuture = b.bind();
        //zk注册
        System.out.println("=========节点服务器启动正在.....");
        serverChannelFuture.addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("===== 节点服务器启动成功，端口: " + port + " =====");
            } else {
                System.err.println("!!!!! 节点服务器启动失败 !!!!!");
                future.cause().printStackTrace();
                System.exit(1); // 启动失败直接退出
            }
        });
    }

    public void shutdown() {
        if (serverChannelFuture != null) {
            serverChannelFuture.channel().close().syncUninterruptibly();
        }
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

}