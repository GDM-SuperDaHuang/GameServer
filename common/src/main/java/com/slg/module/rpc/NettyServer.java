package com.slg.module.rpc;

import com.slg.module.protubuf.ProtobufLengthDecoder;
import io.grpc.netty.shaded.io.netty.bootstrap.ServerBootstrap;
import io.grpc.netty.shaded.io.netty.channel.*;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.SocketChannel;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioServerSocketChannel;
import io.grpc.netty.shaded.io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.grpc.netty.shaded.io.netty.handler.logging.LogLevel;
import io.grpc.netty.shaded.io.netty.handler.logging.LoggingHandler;
import io.grpc.netty.shaded.io.netty.handler.timeout.IdleState;
import io.grpc.netty.shaded.io.netty.handler.timeout.IdleStateEvent;
import io.grpc.netty.shaded.io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
@RequiredArgsConstructor
public class NettyServer implements CommandLineRunner {
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
//    @Autowired
    private final MessageCodec MESSAGECODEC;
//    @Autowired
    private final MsgDecode MSGDECODE ;

//    public NettyServer(MsgDecode MSGDECODE) {
//        this.MSGDECODE = MSGDECODE;
//    }

    LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);

    public void start(int port) throws Exception {

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
                            p.addLast(loggingHandler);
                            //心跳 20
                            p.addLast(new IdleStateHandler(20, 0, 0));
                            p.addLast(new ChannelDuplexHandler() {
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    IdleStateEvent event = (IdleStateEvent) evt;
                                    if (event.state() == IdleState.READER_IDLE) {
                                        System.out.println("超时---------------");
                                    }
//                                    super.userEventTriggered(ctx, evt);
                                }
                            });
                            //添加长度解码器,防止包的不完整
//                            p.addLast(new ProtobufLengthDecoder());
                            p.addLast(new LengthFieldBasedFrameDecoder(118030371, 0xc  , 0x4  , 0x0, 0x0));
                            // 添加ProtoBuf解码器
//                            p.addLast(MESSAGECODEC);
                            p.addLast("DECODE",MSGDECODE);

                            p.addLast(new MyServerHandler());
                        }
                    });
            ChannelFuture f = b.bind().sync();
            //zk注册
//            toRegisterZK(port);
            System.out.println("=====服务器启动成功=====");
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            // 关闭ZooKeeper客户端
//            client.close();
        }
    }

    @Async
    @Override
    public void run(String... args) throws Exception {
        start(8888);
    }


//    private void toRegisterZK(int port) throws Exception {
//        // 在ZooKeeper中注册服务
//        String servicePath = "/services/myapp/" + port;
//        String serviceData = "Netty Server on port " + port;
//        if (client.checkExists().forPath(servicePath) == null) {
//            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(servicePath, serviceData.getBytes());
//            System.out.println("Server registered to ZooKeeper at " + servicePath);
//        } else {
//            System.out.println("Warning: Node already exists at " + servicePath + ". Overwriting data or ignoring...");
//            // 更新数据
//            client.setData().forPath(servicePath, serviceData.getBytes());
//        }
//        System.out.println("=====ZK注册成功/更新=====");
//    }


}