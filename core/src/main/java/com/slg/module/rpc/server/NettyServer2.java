//package com.slg.module.rpc.server;
//
//import com.slg.module.rpc.msgDECode.MsgDecode;
//import io.netty.bootstrap.ServerBootstrap;
//import io.netty.channel.*;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioServerSocketChannel;
//import io.netty.handler.logging.LogLevel;
//import io.netty.handler.logging.LoggingHandler;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//
//import java.net.InetSocketAddress;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Component
//public class NettyServer2 implements CommandLineRunner {
//    @Value("${netty.server.port}")
//    private int port;
//    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
//    EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
//
//    @Autowired
//    private PbMessageHandler pbMessageHandler;
//
//    private List<Channel> channelPool = Collections.synchronizedList(new ArrayList<>());
//    private AtomicInteger counter = new AtomicInteger(0);
//
//    public void start(int port) throws Exception {
//        try {
//            ServerBootstrap b = new ServerBootstrap();
//            b.group(bossGroup, workerGroup)
//                    .channel(NioServerSocketChannel.class)
//                    .childOption(ChannelOption.SO_KEEPALIVE, true)
//                    .childOption(ChannelOption.TCP_NODELAY, true)
//                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
//                    .option(ChannelOption.SO_REUSEADDR, true)
//                    .localAddress(new InetSocketAddress(port))
//                    .option(ChannelOption.SO_BACKLOG, 1024)
//                    .childHandler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        protected void initChannel(SocketChannel ch) throws Exception {
//                            ChannelPipeline p = ch.pipeline();
//                            p.addLast(new MsgDecode());
//                            p.addLast(new ChannelInboundHandlerAdapter() {
//                                @Override
//                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//                                    channelPool.remove(ctx.channel());
//                                    super.channelInactive(ctx);
//                                }
//                            });
//                            p.addLast(pbMessageHandler);
//                            channelPool.add(ch);
//                        }
//                    });
//
//            ChannelFuture f = b.bind().sync();
//            System.out.println("=====服务器启动成功=====");
//            f.channel().closeFuture().sync();
//        } finally {
//            System.out.println("----------------------------服务器关闭--------------------------------------------");
//            workerGroup.shutdownGracefully();
//            bossGroup.shutdownGracefully();
//        }
//    }
//
//    public Channel getChannel() {
//        if (channelPool.isEmpty()) {
//            return null;
//        }
//        int index = counter.getAndIncrement() % channelPool.size();
//        return channelPool.get(index);
//    }
//
//    @Async
//    @Override
//    public void run(String... args) throws Exception {
//        start(port);
//    }
//
//}