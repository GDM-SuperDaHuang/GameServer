//package com.slg.common.message;
//
////import io.netty.channel.ChannelHandlerContext;
//
//import com.slg.protobuffile.message.MSG;
//import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
//
//public class ResultNotice {
//    public ResultNotice() {
//    }
//
//
//    public ResultNotice(ChannelHandlerContext ctx, Object object) {
//        MSG.LoginResponse loginResponse = MSG.LoginResponse.newBuilder()
//                .setAaa(1)
//                .setBbb(2).build();
//        MSG.Response.newBuilder().setUnknownFields()
//        MSG.Response response = MSG.Message.newBuilder().setResponse();
//
//        MSG.Message.Builder builder = MSG.Message.newBuilder().setResponse(loginResponse);
//
//    }
//
//}
