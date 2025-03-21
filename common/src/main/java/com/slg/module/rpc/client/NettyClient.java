package com.slg.module.rpc.client;

import com.google.protobuf.GeneratedMessage;
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
    ServerConfig serverConfig1 = new ServerConfig("127.0.0.1", 7777, 1);
    ServerConfig serverConfig2 = new ServerConfig("127.0.0.1", 8888, 2);
    // NettyClient.java 新增代码
    private ConcurrentHashMap<Long, CompletableFuture<ByteBufferMessage>> pendingRequests = new ConcurrentHashMap<>();
    //todo 注册中心配置
    public ServerConfig getChannelKey(int protocolId) {
        ServerConfig serverConfig;
        if (protocolId > 2000) {
            serverConfig = serverConfig1;
        } else {
            serverConfig = serverConfig2;
        }
        return serverConfig;
    }

    @Autowired
    private DownstreamServerHandler downstreamServerHandler;
    private ConcurrentHashMap<Integer, Channel> channelMap = new ConcurrentHashMap<>();
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
            channelMap.put(serverId, downstreamChannel);
            downstreamChannel.closeFuture().addListener(future -> {
                channelMap.remove(serverId);
            });
            return downstreamChannel;
        }
        return null;
    }

    //远程调用
    public CompletableFuture<ByteBufferMessage> sentMsgAsync(int protocolId, GeneratedMessage.Builder<?> builder) {
        ServerConfig serverConfig = getChannelKey(protocolId);
        int serverId = serverConfig.getServerId();
        Channel channel = channelMap.get(serverId);
        CompletableFuture<ByteBufferMessage> future = new CompletableFuture<>();

        if (channel == null) {
            try {
                channel = connectToDownstreamServer(serverConfig.getHost(), serverConfig.getPort(), serverId);
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
                return future;
            }
        }

        if (channel == null) {
            future.completeExceptionally(new RuntimeException("Channel connection failed"));
            return future;
        }

        long nextCid = UniqueCidGenerator.getNextCid();
        pendingRequests.put(nextCid, future);

        byte[] msg = builder.buildPartial().toByteArray();
        ByteBuf out = buildMsg(nextCid, 0, 0, protocolId, 0, 1, msg);
        channel.writeAndFlush(out).addListener(writeFuture -> {
            if (!writeFuture.isSuccess()) {
                pendingRequests.remove(nextCid);
                future.completeExceptionally(writeFuture.cause());
            }
        });

        return future;
    }


    private ByteBuf buildMsg(long cid, long userId, int errorCode, int protocolId, int zip, int version, byte[] bodyArray) {
        int length = bodyArray.length;
        //写回
        ByteBuf out = Unpooled.buffer(24 + length);
        //消息头
        out.writeLong(cid);      // 8字节
        out.writeInt(0);      // 4字节
        out.writeInt(errorCode);      // 4字节
        out.writeInt(protocolId);      // 4字节
        out.writeByte(zip);                       // zip压缩标志，1字节
        out.writeByte(version);                       // pb版本，1字节
        //消息体
        out.writeShort(bodyArray.length);                 // 消息体长度，2字节
        // 写入消息体
        out.writeBytes(bodyArray);
        return out;
    }


}
