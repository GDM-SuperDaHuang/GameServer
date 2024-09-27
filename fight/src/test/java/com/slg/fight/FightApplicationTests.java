package com.slg.fight;

import com.slg.commom.message.MSG;
import io.grpc.netty.shaded.io.netty.bootstrap.Bootstrap;
import io.grpc.netty.shaded.io.netty.channel.*;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.SocketChannel;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel;
import io.grpc.netty.shaded.io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.grpc.netty.shaded.io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.grpc.netty.shaded.io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.grpc.netty.shaded.io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.InetSocketAddress;

@SpringBootTest
class FightApplicationTests {

	@Test
	void contextLoads() {
	}


	private static Channel channel;

	private final int clientPort = 9000;

	private final String host = "127.0.0.1";


	public void start() throws Exception {
		// 创建客户端的 EventLoopGroup
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			// 创建 Bootstrap 实例
			Bootstrap b = new Bootstrap();
			b.group(group)
					.channel(NioSocketChannel.class)
					//使用指定的端口设置套接字地址
					.localAddress(new InetSocketAddress(clientPort))// 使用 NIO 的通道类型
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new ProtobufVarint32FrameDecoder());
							// 添加ProtoBuf解码器
							p.addLast(new ProtobufDecoder(MSG.Message.getDefaultInstance()));
							p.addLast(new ProtobufVarint32LengthFieldPrepender());
							// 添加ProtoBuf编码器
							p.addLast(new ProtobufEncoder());
							p.addLast(new NettyClientHandler());
						}
					});

			// 连接到服务器并等待连接完成
			ChannelFuture f = b.connect(host, clientPort).sync();
			channel = f.channel();
			// 等待直到连接被关闭
			f.channel().closeFuture().sync();
		} finally {
			// 优雅地关闭
			group.shutdownGracefully().sync();
		}
	}

}
