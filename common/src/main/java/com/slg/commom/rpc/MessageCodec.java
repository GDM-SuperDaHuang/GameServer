package com.slg.commom.rpc;


import com.slg.protobuffile.message.MSG;
import io.grpc.netty.shaded.io.netty.buffer.ByteBuf;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandler;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
import io.grpc.netty.shaded.io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 确保之前的包完整性
 */
@ChannelHandler.Sharable
public class MessageCodec extends MessageToMessageCodec<ByteBuf, MSG.Message> {//INBOUND_IN, OUTBOUND_IN
    Map<Long,Long> sessionMap = new ConcurrentHashMap<>();

    //出
    @Override
    protected void encode(ChannelHandlerContext ctx, MSG.Message msg, List<Object> list) throws Exception {
        System.out.println("99999999999999999999999");
        ByteBuf out = ctx.alloc().buffer();
        // 1. 8 字节session
        out.writeLong(1111111111L);
        //消息id
        out.writeInt(0);
        //压缩标志
        out.writeByte(0);
        //版本
        out.writeByte(3);
        //长度
        byte[] byteArray = msg.toByteArray();
        out.writeShort(byteArray.length);
        //写入protobuf
        out.writeBytes(byteArray);
        list.add(out);
    }


    //入
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        //sessionId
        long sessionId = in.readLong();
        //消息id
        int orderId = in.readInt();
        // 压缩标志
        byte zip = in.readByte();
        //版本
        byte pbVersion = in.readByte();
        //长度
        short length = in.readShort();
        byte[] bytes = new byte[length];
        // 6. 读取protobuf字节数组
        in.readBytes(bytes, 0, length);
        //反序列化成java对象
        MSG.Message message = MSG.Message.parseFrom(bytes);
        out.add(message);
    }
}
