package com.slg.module.handle;

import com.slg.module.annotation.ToMethod;
import com.slg.module.annotation.ToServer;
import com.slg.module.interfaceT.monitor1.EventPublisher;
import com.slg.module.message.ByteBufferMessage;
import com.slg.module.message.MSG;
import com.slg.module.message.SendMsg;
import com.slg.module.rpc.client.NettyClient;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@ToServer
public class Test {
    private static final Logger logger = LogManager.getLogger(Test.class);
    private int sum = 0;
    private SendMsg sendMsg;
    @Autowired
    private NettyClient client;

    @Autowired
    EventPublisher publisher;
    public Test(SendMsg sendMsg) {
        this.sendMsg = sendMsg;
        this.client = client;
    }

    @ToMethod(value = 1)
    public ByteBufferMessage diy(ChannelHandlerContext ctx, MSG.LoginRequest request, long userId) throws IOException, InterruptedException {
        sum++;

        MSG.LoginResponse.Builder LoginResponseBuilder = MSG.LoginResponse.newBuilder()
                .setAaa(999999999)
                .setBbb(777777777);
        LoginResponseBuilder.
        sendMsg.send(ctx, LoginResponseBuilder);

//        MSG.FriendRequest.Builder builder = MSG.FriendRequest.newBuilder()
//                .setUserId(111222333L);
//        client.sentMsg(2,builder);
//        publisher.publishCustomEvent(LoginResponseBuilder);
    }


    @ToMethod(value = 2)
    public void ffff(ChannelHandlerContext ctx, MSG.FriendRequest request,long userId) throws IOException {
        byte[] byteArray = MSG.LoginResponse.newBuilder()
                .setAaa(1111111111)
                .setBbb(2132123132)
                .buildPartial()
                .toByteArray();


        MSG.LoginResponse.Builder builder = MSG.LoginResponse.newBuilder()
                .setAaa(1111111111)
                .setBbb(2132123132);

        MSG.LoginResponse.Builder LoginResponseBuilder = MSG.LoginResponse.newBuilder()
                .setAaa(999999999)
                .setBbb(777777777);
        sendMsg.send(ctx, LoginResponseBuilder);
        sendMsg.send(ctx, byteArray);

    }

}
