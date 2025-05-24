package com.slg.module.message;

import com.google.protobuf.GeneratedMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;


@Component
public class SendMsg {
    private void send(ChannelHandlerContext ctx, byte[] msg) {
        ByteBuf buf = Unpooled.buffer(16);
        //消息头
        buf.writeLong(0);
        buf.writeInt(0);
        buf.writeByte(0);
        buf.writeByte(0);
        int length = msg.length;
        buf.writeShort(length);
        buf.writeBytes(msg);
        Channel channel = ctx.channel();
        ChannelFuture future = ctx.writeAndFlush(buf);
    }

    private void send(ChannelHandlerContext ctx,long userId, int protocolId, GeneratedMessage.Builder<?> builder) {
        byte[] body = builder.buildPartial().toByteArray();
        ByteBuf out = buildMsg(userId, 0, 0, protocolId, 0, 0, body);
        ChannelFuture future = ctx.writeAndFlush(out);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
//                    System.out.println("消息发送成功");
                } else {
//                    System.out.println("消息发送失败: " + future.cause().getMessage());
                }
            }
        });

    }

    private ByteBuf buildMsg(long userId, int cid, int errorCode, int protocolId, int zip, int version, byte[] bodyArray) {
        int length = bodyArray.length;
        //写回
        ByteBuf out = Unpooled.buffer(24 + length);
        //消息头
        out.writeLong(userId);      // 8字节
        out.writeInt(cid);      // 4字节
        out.writeInt(errorCode);      // 4字节
        out.writeInt(protocolId);      // 4字节
        out.writeByte(zip);                       // zip压缩标志，1字节
        out.writeByte(version);                       // pb版本，1字节
        //消息体
        out.writeShort(bodyArray.length);                 // 消息体长度，2字节
        // 写入消息体
        out.writeBytes(bodyArray);
        return out;
    }

}
