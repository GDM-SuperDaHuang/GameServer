//package com.slg.demo.handle;
//
//import com.slg.common.annotation.ToMethod;
//import com.slg.common.annotation.ToServer;
//import com.slg.common.message.MSG;
//import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
//import org.springframework.stereotype.Component;
//
//@Component
//@ToServer
//public class Test {
//    @ToMethod("1")
//    public void f(ChannelHandlerContext ctx, MSG.Request request){
//        System.out.println("----------"+ctx+"==========="+request);
//        System.out.println("----------"+ctx+"==========="+request);
//    }
//
//}
