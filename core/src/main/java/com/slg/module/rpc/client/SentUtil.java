//package com.slg.module.rpc.client;
//
//import com.google.protobuf.GeneratedMessage;
//import com.google.protobuf.GeneratedMessageV3;
//import com.slg.module.message.ByteBufferServerMessage;
//import com.slg.module.message.MsgUtil;
//import com.slg.module.rpc.msgDECode.MsgDecode;
//import com.slg.module.util.BeanTool;
//import com.slg.module.util.UniqueCidGenerator;
//import io.netty.bootstrap.Bootstrap;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.CompositeByteBuf;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.*;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.Method;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.function.Function;
//
///**
// * 远程调用
// */
//@Component
//public class SentUtil {
//    ServerConfig serverConfig1 = new ServerConfig("127.0.0.1", 10101, 1);
//    ServerConfig serverConfig2 = new ServerConfig("127.0.0.1", 7777, 2);
//
//    private final DownstreamServerHandler downstreamServerHandler = new DownstreamServerHandler(BeanTool.getBean(SentUtil.class));
//
//    private Map<Integer, Channel> serverChannelMap = new ConcurrentHashMap<>();
//    private final EventLoopGroup downstreamGroup = new NioEventLoopGroup(1);
//    private ConcurrentHashMap<Integer, CompletableFuture<ByteBufferServerMessage>> pendingRequests = new ConcurrentHashMap<>();
//
//    private Map<Integer, Method> methodMap = new ConcurrentHashMap<>();
//
//    private final Bootstrap bootstrap;
//
//    public SentUtil() {
//        bootstrap = new Bootstrap();
//        bootstrap.group(downstreamGroup)
//                .channel(NioSocketChannel.class)
//                .option(ChannelOption.TCP_NODELAY, true)
//                .option(ChannelOption.SO_KEEPALIVE, true)
//                .handler(new ChannelInitializer<SocketChannel>() {
//                    @Override
//                    protected void initChannel(SocketChannel ch) {
//                        ChannelPipeline p = ch.pipeline();
//                        p.addLast(new MsgDecode());
//                        p.addLast(downstreamServerHandler);
//                    }
//                });
//    }
//
//
//    public ServerConfig getChannelKey(int protocolId) {
//        ServerConfig serverConfig;
//        if (protocolId > 2000) {
//            serverConfig = serverConfig1;
//        } else {
//            serverConfig = serverConfig2;
//        }
//        return serverConfig;
//    }
//
//    //发送
//    public CompletableFuture<ByteBufferServerMessage> sentMsgAsync(int protocolId, GeneratedMessage.Builder<?> builder) {
//        ServerConfig serverConfig = getChannelKey(protocolId);
//        if (serverConfig == null) {
//            return CompletableFuture.failedFuture(new IllegalArgumentException("No server config for protocolId: " + protocolId));
//        }
//
//        int serverId = serverConfig.getServerId();
//
//        Channel channel = serverChannelMap.get(serverId);
//        CompletableFuture<ByteBufferServerMessage> future = new CompletableFuture<>();
//
//        if (channel == null) {
//            channel = connectToDownStreamServer(serverConfig.getHost(), serverConfig.getPort(), serverId);
//        }
//
//        if (channel == null) {
//            future.completeExceptionally(new RuntimeException("Channel connection failed"));
//            return future;
//        }
//
//        int nextCid = UniqueCidGenerator.getNextCid();
//        pendingRequests.put(nextCid, future);
//        byte[] body = builder.buildPartial().toByteArray();
////        System.out.println("=====nextCid:=========" + nextCid);
//
//
//        ByteBuf out = buildMsg(channel,0, nextCid, 0, protocolId, 0, 3, body);
//        channel.writeAndFlush(out).addListener(writeFuture -> {
//            if (!writeFuture.isSuccess()) {
//                pendingRequests.remove(nextCid);
//                future.completeExceptionally(writeFuture.cause());
//            }
//        });
//
//        return future;
//    }
//
//    public <T extends GeneratedMessage> CompletableFuture<T> sentMsgAsync(
//            int protocolId,
//            GeneratedMessage.Builder<?> builder,
//            Function<ByteBuf, T> parserFunction) {
//        CompletableFuture<ByteBufferServerMessage> rawFuture = sentMsgAsync(protocolId, builder);
//        return rawFuture.thenApply(response -> {
//            try {
//                return parserFunction.apply(response.getBody());
//            } catch (Exception e) {
//                throw new RuntimeException("Failed to parse response", e);
//            }
//        });
//    }
//
//    public <T extends GeneratedMessage> CompletableFuture<T> sentMsgAsync(
//            int protocolId,
//            GeneratedMessage.Builder<?> builder,
//            Class<T> responseClass) {
//
//        CompletableFuture<ByteBufferServerMessage> rawFuture = sentMsgAsync(protocolId, builder);
//        return rawFuture.thenApply(response -> {
//            try {
//                Method orDefault = methodMap.getOrDefault(protocolId, null);
//                if (orDefault == null) {
//                    orDefault = responseClass.getMethod("parseFrom", ByteBuf.class);
//                    methodMap.put(protocolId, orDefault);
//                }
//                @SuppressWarnings("unchecked")
//                T result = (T) orDefault.invoke(null, response.getBody());
//                return result;
//            } catch (Exception e) {
//                throw new RuntimeException("Failed to parse response", e);
//            }
//        });
//    }
//
//    /**
//     * 连接远程节点之间；
//     *
//     * @param host     服务器地址
//     * @param port     服务器端口
//     * @param serverId 服务器标识
//     * @throws InterruptedException
//     */
//    public Channel connectToDownStreamServer(String host, int port, int serverId) {
//        //TODO
//        ChannelFuture channelFuture = null;
//        try {
//            channelFuture = bootstrap.connect(host, port).sync();
//        } catch (Exception e) {
//            System.out.println("连接失败，远程调用失败");
//            return null;
////            throw new RuntimeException(e);
//        }
//        if (channelFuture.isSuccess()) {
//            Channel downstreamChannel = channelFuture.channel();
//            serverChannelMap.put(serverId, downstreamChannel);
//            downstreamChannel.closeFuture().addListener(future -> {
//                serverChannelMap.remove(serverId);
//            });
//            return downstreamChannel;
//        }
//        return null;
//    }
//
//
//    public CompletableFuture<ByteBufferServerMessage> getPendingRequests(int cid) {
//        return pendingRequests.getOrDefault(cid, null);
//    }
//
//    public void removeCompletableFutureMap(int cid) {
//        pendingRequests.remove(cid);
//    }
//
//
//    private ByteBuf buildMsg(Channel ctx, long userId, int cid, int errorCode, int protocolId, int zip, int version, byte[] bodyArray) {
//        int length = bodyArray.length;
//        ByteBuf out = ctx.alloc().buffer(24 + length);
//        out.writeLong(userId);
//        out.writeInt(cid);
//        out.writeInt(errorCode);
//        out.writeInt(protocolId);
//        out.writeByte(zip);
//        out.writeByte(0);
//        out.writeShort(length);
//        out.writeBytes(bodyArray);
//        return out;
//    }
//
//}
