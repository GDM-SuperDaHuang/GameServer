//package com.slg.module;
//
//
//import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
//
//
//public class NettyClientHandler extends SimpleChannelInboundHandler<MSG.Message> {
//    @Override
//    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MSG.Message message) throws Exception {
//        System.out.println("客户端收到: " + message);
//
//    }
//
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        // 当连接建立时，发送一个Protobuf消息
//        System.out.println("---------客户端连接成功出发："+ctx);
//
//    }
//
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        // 处理异常
//        cause.printStackTrace();
//        ctx.close();
//    }
//
//}
