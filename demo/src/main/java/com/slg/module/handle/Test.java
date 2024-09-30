package com.slg.module.handle;

import com.slg.module.annotation.ToMethod;
import com.slg.module.annotation.ToServer;
import com.slg.module.annotation.route.RouteServer;
import com.slg.module.message.MSG;
import com.slg.module.protubuf.SendMsg;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
@ToServer
public class Test extends RouteServer {
    private int sum = 0;
    private SendMsg sendMsg;

    public Test(SendMsg sendMsg) {
        this.sendMsg = sendMsg;
    }

    @ToMethod(value = 1)
    public void diy(ChannelHandlerContext ctx, MSG.LoginRequest request) {
        sum++;
//        System.out.println("服务器收到数据" + request + "===" + request.getPassword() + "===" + request.getUsername());
        byte[] byteArray = MSG.LoginResponse.newBuilder()
                .setAaa(1111111111)
                .setBbb(2132123132)
                .buildPartial()
                .toByteArray();
        sendMsg.send(ctx, byteArray);
        if (sum == 70000) {
            System.out.println("服务器收到数据sum" + sum);
        }
        if (sum == 80000) {
            System.out.println("服务器收到数据sum" + sum);
        }
        if (sum == 85000) {
            System.out.println("服务器收到数据sum" + sum);
        }
        if (sum == 90000) {
            System.out.println("服务器收到数据sum" + sum);
        }
        if (sum >= 99900) {
            System.out.println("服务器收到数据sum" + sum);
        }
    }

}
