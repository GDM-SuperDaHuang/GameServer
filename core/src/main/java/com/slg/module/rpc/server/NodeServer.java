package com.slg.module.rpc.server;

import com.slg.module.connection.ServerConfigManager;
import com.slg.module.message.Constants;
import com.slg.module.rpc.msgDECode.MsgDecode;
import com.slg.module.util.ConfigReader;
import com.slg.module.util.NacosClientUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class NodeServer {
    private int port;

    public int getPort() {
        return port;
    }
    private final Class<? extends ServerSocketChannel> channelClass;

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final PbMessageHandler pbMessageHandler = new PbMessageHandler();
    private ChannelFuture serverChannelFuture;
    LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);
    ConfigReader configReader = new ConfigReader("application.properties");
    NacosClientUtil client = NacosClientUtil.getInstance(
            configReader.getProperty("nacos.serverAddr"),  // Nacos服务器地址
            configReader.getProperty("nacos.namespace")  // 命名空间ID（如为空字符串则使用默认命名空间）
    );

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
            workerGroup = new EpollEventLoopGroup(); // 使用 Epoll
            channelClass = EpollServerSocketChannel.class;

        } else {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(); // 使用 NIO
            channelClass = NioServerSocketChannel.class;
        }

    }

    public void start(int port) {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                // 指定Channel
                .channel(channelClass)
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
//                client.waitForConnected(1000); // 等待最长10秒
                // 注册服务实例
                Map<String, String> metadata = new HashMap<>();
                String group = configReader.getProperty("nacos.service.group");
                if (group == null) {
                    group = "DEFAULT_GROUP";
                }
//                String host = configReader.getProperty("netty.server.host");
                String host = "115.190.79.27";
                String serverName = configReader.getProperty("nacos.service.name");
                String pbMin = configReader.getProperty("server.proto-id-min");
                String pbMax = configReader.getProperty("server.proto-id-max");
                String serverId = configReader.getProperty("netty.server.serverId");
                String groupId = configReader.getProperty("netty.server.group-id");//组
                metadata.put(Constants.ProtoMinId, pbMin);
                metadata.put(Constants.ProtoMaxId, pbMax);
                metadata.put(Constants.GroupId, groupId);
//                client.registerInstance(serverId, serverName, group, host, port, 1.0, metadata);
                client.registerInstance(serverId, serverName, group, host, 8001, 1.0, metadata);
                ServerConfigManager.getInstance(serverName, group, null, serverId);
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
        client.close();
    }

}