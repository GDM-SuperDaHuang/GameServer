package com.slg.module.rpc.client;

import com.google.protobuf.GeneratedMessage;
import com.slg.module.message.ByteBufferServerMessage;
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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 远程调用
 */
@Component
public class SentUtil {
    ServerConfig serverConfig1 = new ServerConfig("127.0.0.1", 10101, 1);
    ServerConfig serverConfig2 = new ServerConfig("127.0.0.1", 7777, 2);

    @Autowired
    private DownstreamServerHandler downstreamServerHandler;

    private Map<Integer, Channel> serverChannelMap = new ConcurrentHashMap<>();
    private final EventLoopGroup downstreamGroup = new NioEventLoopGroup(1);
    private ConcurrentHashMap<Integer, CompletableFuture<ByteBufferServerMessage>> pendingRequests = new ConcurrentHashMap<>();

    private final Bootstrap bootstrap;

    public SentUtil() {
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


    public ServerConfig getChannelKey(int protocolId) {
        ServerConfig serverConfig;
        if (protocolId > 2000) {
            serverConfig = serverConfig1;
        } else {
            serverConfig = serverConfig2;
        }
        return serverConfig;
    }

    //发送
    public CompletableFuture<ByteBufferServerMessage> sentMsgAsync(int protocolId, GeneratedMessage.Builder<?> builder) {
        ServerConfig serverConfig = getChannelKey(protocolId);
        if (serverConfig == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("No server config for protocolId: " + protocolId));
        }

        int serverId = serverConfig.getServerId();

        Channel channel = serverChannelMap.get(serverId);
        CompletableFuture<ByteBufferServerMessage> future = new CompletableFuture<>();

        if (channel == null) {
            channel = connectToDownStreamServer(serverConfig.getHost(), serverConfig.getPort(), serverId);
        }

        if (channel == null) {
            future.completeExceptionally(new RuntimeException("Channel connection failed"));
            return future;
        }

        int nextCid = UniqueCidGenerator.getNextCid();
        pendingRequests.put(nextCid, future);
        byte[] msg = builder.buildPartial().toByteArray();
//        System.out.println("=====nextCid:=========" + nextCid);
        ByteBuf out = buildMsg(0, nextCid, 0, protocolId, 0, 3, msg);
        channel.writeAndFlush(out).addListener(writeFuture -> {
            if (!writeFuture.isSuccess()) {
                pendingRequests.remove(nextCid);
                future.completeExceptionally(writeFuture.cause());
            }
        });

        return future;
    }

    public <T extends GeneratedMessageV3> CompletableFuture<T> sentMsgAsync(
        int protocolId, 
        GeneratedMessage.Builder<?> builder, 
        Function<byte[], T> parserFunction) {
    
    CompletableFuture<ByteBufferServerMessage> rawFuture = sentMsgAsync(protocolId, builder);
    
    return rawFuture.thenApply(response -> {
        try {
            return parserFunction.apply(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    });
}

    // 处理响应的示例方法
    public <T> void handleResponse(int cid, Object response, Class<T> clazz) {
        CompletableFuture<?> rawFuture = pendingRequests.remove(cid);
        if (rawFuture != null) {
            CompletableFuture<T> future = (CompletableFuture<T>) rawFuture;
            T typedResponse = clazz.cast(response);
            future.complete(typedResponse);
        }
    }

    /**
     * 连接远程节点之间；
     *
     * @param host     服务器地址
     * @param port     服务器端口
     * @param serverId 服务器标识
     * @throws InterruptedException
     */
    public Channel connectToDownStreamServer(String host, int port, int serverId) {
        //TODO
        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.connect(host, port).sync();
        } catch (Exception e) {
            System.out.println("连接失败，远程调用失败");
            return null;
//            throw new RuntimeException(e);
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


    public CompletableFuture<ByteBufferServerMessage> getPendingRequests(int cid) {
        return pendingRequests.getOrDefault(cid, null);
    }

    public void removeCompletableFutureMap(int cid) {
        pendingRequests.remove(cid);
    }


    private ByteBuf buildMsg(long userId, int cid, int errorCode, int protocolId, int zip, int version, byte[] bodyArray) {
        int length = bodyArray.length;
        //写回
        ByteBuf out = Unpooled.buffer(24 + length);
        //消息头
        out.writeLong(userId);      // 8字节
        out.writeInt(cid);      // 4字节
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
