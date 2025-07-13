package com.slg.module.util;

import com.google.protobuf.GeneratedMessage;
import com.slg.module.config.ServerConfig;
import com.slg.module.connection.ServerConfigManager;
import com.slg.module.message.ByteBufferServerMessage;
import com.slg.module.rpc.client.NettyClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 远程调用
 */
public class SentMsgUtil {
    ConfigReader configReader = new ConfigReader("application.properties");

    //    ServerConfig serverConfig1 = new ServerConfig("127.0.0.1", 10101, 1);
//    ServerConfig serverConfig2 = new ServerConfig("127.0.0.1", 7777, 2);
    private final ConcurrentHashMap<Integer, CompletableFuture<ByteBufferServerMessage>> pendingRequests = new ConcurrentHashMap<>();
    private final NettyClient nodeClient = NettyClient.getInstance();
    private final Map<Integer, Method> methodMap = new ConcurrentHashMap<>();//缓存

    // 私有构造函数防止外部实例化
    private SentMsgUtil() {
    }

    // 静态内部类持有单例实例
    private static class SentMsgUtilHolder {
        private static final SentMsgUtil INSTANCE = new SentMsgUtil();
    }

    // 提供全局访问点
    public static SentMsgUtil getInstance() {
        return SentMsgUtilHolder.INSTANCE;
    }

    //todo 目前放回首个
    public ServerConfig getChannelKey(int protocolId) {
        ServerConfigManager serverConfigManager = ServerConfigManager.getAlreadyInstance();
        List<ServerConfig> channelKey = serverConfigManager.getChannelKey(protocolId);
        if (channelKey==null){
            return null;
        }

        ServerConfig serverConfig = channelKey.get(0);
        return serverConfig;
    }

    //发送
    public CompletableFuture<ByteBufferServerMessage> sentMsgAsync(int protocolId, GeneratedMessage.Builder<?> builder) {
        ServerConfig serverConfig = getChannelKey(protocolId);
        if (serverConfig == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("No server config for protocolId: " + protocolId));
        }

        int serverId = serverConfig.getServerId();
        Channel channel = nodeClient.serverChannelMap.get(serverId);
        CompletableFuture<ByteBufferServerMessage> future = new CompletableFuture<>();
        if (channel == null) {
            channel = nodeClient.connectToDownStreamServer(serverConfig.getHost(), serverConfig.getPort(), serverId);
        }
        if (channel == null) {
            future.completeExceptionally(new RuntimeException("Channel connection failed"));
            return future;
        }

        int nextCid = UniqueCidGenerator.getNextCid();
        pendingRequests.put(nextCid, future);
        byte[] body = builder.buildPartial().toByteArray();
//        System.out.println("=====nextCid:=========" + nextCid);


        ByteBuf out = buildMsg(channel, 0, nextCid, 0, protocolId, 0, 3, body);
        channel.writeAndFlush(out).addListener(writeFuture -> {
            if (!writeFuture.isSuccess()) {
                pendingRequests.remove(nextCid);
                future.completeExceptionally(writeFuture.cause());
            }
        });

        return future;
    }

    public <T extends GeneratedMessage> CompletableFuture<T> sentMsgAsync(
            int protocolId,
            GeneratedMessage.Builder<?> builder,
            Function<ByteBuf, T> parserFunction) {
        CompletableFuture<ByteBufferServerMessage> rawFuture = sentMsgAsync(protocolId, builder);
        return rawFuture.thenApply(response -> {
            try {
                return parserFunction.apply(response.getBody());
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse response", e);
            }
        });
    }

    public <T extends GeneratedMessage> CompletableFuture<T> sentMsgAsync(
            int protocolId,
            GeneratedMessage.Builder<?> builder,
            Class<T> responseClass) {

        CompletableFuture<ByteBufferServerMessage> rawFuture = sentMsgAsync(protocolId, builder);
        return rawFuture.thenApply(response -> {
            try {
                Method orDefault = methodMap.getOrDefault(protocolId, null);
                if (orDefault == null) {
                    orDefault = responseClass.getMethod("parseFrom", ByteBuf.class);
                    methodMap.put(protocolId, orDefault);
                }
                @SuppressWarnings("unchecked")
                T result = (T) orDefault.invoke(null, response.getBody());
                return result;
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse response", e);
            }
        });
    }


    public CompletableFuture<ByteBufferServerMessage> getPendingRequests(int cid) {
        return pendingRequests.getOrDefault(cid, null);
    }

    public void removeCompletableFutureMap(int cid) {
        pendingRequests.remove(cid);
    }

    private ByteBuf buildMsg(Channel ctx, long userId, int cid, int errorCode, int protocolId, int zip, int version, byte[] bodyArray) {
        int length = bodyArray.length;
        ByteBuf out = ctx.alloc().buffer(24 + length);
        out.writeLong(userId);
        out.writeInt(cid);
        out.writeInt(errorCode);
        out.writeInt(protocolId);
        out.writeByte(zip);
        out.writeByte(0);
        out.writeShort(length);
        out.writeBytes(bodyArray);
        return out;
    }

}
