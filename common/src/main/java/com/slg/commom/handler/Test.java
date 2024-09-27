package com.slg.commom.handler;

import com.slg.commom.annotation.ToMethod;
import com.slg.commom.annotation.ToServer;
import com.slg.commom.annotation.route.RouteServer;
//import com.slg.gateway.message.MSG;
import com.slg.protobuffile.message.MSG;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
@ToServer
public class Test extends RouteServer {
    @ToMethod(value = 1)
    public MSG.LoginResponse p(ChannelHandlerContext ctx, MSG.Request request) {

        MSG.LoginRequest login = request.getLogin();
        f(ctx,login);
        System.out.println("request"+request);
        return null;
    }

    public void f(ChannelHandlerContext ctx, MSG.LoginRequest login) {

    }


//    @ToMethod("Test_Request")
//    public void ff( ChannelHandlerContext ctx, GDM.Request request) {
//        GDM.TestRequest testRequest = request.getTestRequest();
//        System.out.println(ctx + "oooooooooooooo" + testRequest);
//
//    }
//
//    @ToMethod("Send_Message_Request")
//    public void ddd( ChannelHandlerContext ctx, GDM.Request request) {
//        GDM.SendMessageRequest sendMessage = request.getSendMessage();
//        System.out.println(ctx + "oooooooooooooo" + sendMessage);
//    }

}
