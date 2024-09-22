//package com.slg.common.message;
//
//import io.netty.channel.ChannelHandlerContext;
//
//public class ResultNotice {
//    public ResultNotice() {
//    }
//
//    public ResultNotice(ChannelHandlerContext ctx, Object object) {
//        if (object instanceof GDM.Response.Builder ){
//            GDM.Response.Builder builder=(GDM.Response.Builder)object;
//            GDM.Message.Builder messageBuilder = GDM.Message.newBuilder();
//            messageBuilder.setResponse(builder);
//            GDM.Message message = messageBuilder.build();
//            ctx.writeAndFlush(message);
//        }else {
//
//        }
//    }
//
////
////    public void sendMessage(ChannelHandlerContext ctx){
////        Notification
////    }
//}
