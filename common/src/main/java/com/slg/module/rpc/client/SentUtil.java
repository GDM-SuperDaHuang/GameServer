package com.slg.module.rpc.client;

import com.google.protobuf.GeneratedMessage;
import com.slg.module.connection.ServerChannelManage;
import com.slg.module.message.ByteBufferMessage;
import com.slg.module.util.UniqueCidGenerator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SentUtil {

    @Autowired
    private NettyClient client;

    @Autowired
    private ServerChannelManage serverChannelManage;
    ServerConfig serverConfig1 = new ServerConfig("127.0.0.1", 7777, 1);
    ServerConfig serverConfig2 = new ServerConfig("127.0.0.1", 8888, 2);
    public CompletableFuture<ByteBufferMessage> getPendingRequests(long cid) {
        return pendingRequests.getOrDefault(cid, null);
    }

    public void removeCompletableFutureMap(long cid) {
        pendingRequests.remove(cid);
    }
    private ConcurrentHashMap<Long, CompletableFuture<ByteBufferMessage>> pendingRequests = new ConcurrentHashMap<>();

    //远程调用
    public CompletableFuture<ByteBufferMessage> sentMsgAsync(int protocolId, GeneratedMessage.Builder<?> builder) {
        ServerConfig serverConfig = getChannelKey(protocolId);
        int serverId = serverConfig.getServerId();

        Channel channel = serverChannelManage.get(serverId);
        CompletableFuture<ByteBufferMessage> future = new CompletableFuture<>();

        if (channel == null) {
            try {
                channel = client.connectToDownstreamServer(serverConfig.getHost(), serverConfig.getPort(), serverId);
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

    public ServerConfig getChannelKey(int protocolId) {
        ServerConfig serverConfig;
        if (protocolId > 2000) {
            serverConfig = serverConfig1;
        } else {
            serverConfig = serverConfig2;
        }
        return serverConfig;
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
