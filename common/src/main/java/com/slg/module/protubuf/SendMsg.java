package com.slg.module.protubuf;

//import io.grpc.netty.shaded.io.netty.buffer.ByteBuf;
//import io.grpc.netty.shaded.io.netty.buffer.Unpooled;
//import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SendMsg {
    public void send(ChannelHandlerContext ctx, byte[] msg) {
        ByteBuf buf = Unpooled.buffer(16);
        //消息头
        buf.writeLong(1234567L);
        buf.writeInt(1);
        buf.writeByte(7);
        buf.writeByte(9);
        int length = msg.length;
        buf.writeShort(length);
        buf.writeBytes(msg);
        ctx.writeAndFlush(buf);
    }
}
