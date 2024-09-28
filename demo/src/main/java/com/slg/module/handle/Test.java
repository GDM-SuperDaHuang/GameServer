package com.slg.module.handle;
import com.slg.module.annotation.ToMethod;
import com.slg.module.annotation.ToServer;
import com.slg.module.annotation.route.RouteServer;
import com.slg.module.message.MSG;
import com.slg.module.protubuf.SendMsg;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ToServer
public class Test extends RouteServer {

    private SendMsg sendMsg;
    public Test(SendMsg sendMsg) {
        this.sendMsg = sendMsg;
    }

    @ToMethod(value = 1)
    public void diy(ChannelHandlerContext ctx, MSG.LoginRequest request) {
        System.out.println("request"+request+"---"+request.getPassword()+"*****"+request.getUsername());

        byte[] byteArray = MSG.LoginResponse.newBuilder()
                .setAaa(1111111111)
                .setBbb(2132123132)
                .buildPartial()
                .toByteArray();
        sendMsg.send(ctx,byteArray);
    }

}
