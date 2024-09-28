//package com.slg.module.connection;
//
//import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
//import io.grpc.netty.shaded.io.netty.channel.ChannelInboundHandlerAdapter;
//
//public class MessageDispatcher extends ChannelInboundHandlerAdapter {
//    private final BackendConnectionManager backendManager;
//
//    public MessageDispatcher(BackendConnectionManager backendManager) {
//        this.backendManager = backendManager;
//    }
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        if (msg instanceof GenericMessage) {
//            GenericMessage message = (GenericMessage) msg;
//            // 假设protobufId映射到后端服务器的唯一标识符
//            String backendKey = mapProtobufIdToBackendKey(message.getProtobufId());
//            BackendConnection backend = backendManager.getBackendConnection(backendKey);
//            if (backend != null) {
//                // 转发消息到后端服务器
//                backend.writeMessage(message); // 注意：这里可能需要序列化或转换消息格式
//            } else {
//                // 处理找不到后端连接的情况
//                System.err.println("No backend connection found for protobufId: " + message.getProtobufId());
//                // 可能需要发送错误响应给客户端
//            }
//        }
//
//        // 继续处理下一个handler（如果有的话）
//        ctx.fireChannelRead(msg);
//    }
//
//    // 将protobufId映射到后端服务器的唯一标识符
//    private String mapProtobufIdToBackendKey(int protobufId) {
//        // 这里应该有一个逻辑来根据protobufId确定后端服务器的标识符
//        // 例如，返回一个字符串，如"192.168.1.1:8080"
//        return "exampleBackend:" + protobufId; // 仅为示例
//    }
//
//    // 其他方法省略
//}
