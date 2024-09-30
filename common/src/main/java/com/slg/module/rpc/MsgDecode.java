package com.slg.module.rpc;

import com.slg.module.message.PbMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 注意确保之前的包完整性
 */
public class MsgDecode extends ByteToMessageDecoder {
    //入
    @Override
    protected void decode(ChannelHandlerContext cxt, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 16) { // 确保有足够的字节来读取头部
            return;
        }
        //消息头
        //sessionId
        long sessionId = in.readLong();
        //协议id
        int protocolId = in.readInt();
        // 压缩标志
        byte zip = in.readByte();
        //版本
        byte pbVersion = in.readByte();
        //长度
        short length = in.readShort();

        if (in.readableBytes() < length) {//长度不够继续等待
            return;
        }

        //消息体
        byte[] bytes = new byte[length];
        // 6. 读取protobuf字节数组
        in.readBytes(bytes, 0, length);
        PbMessage pbMessage = new PbMessage(sessionId,protocolId,bytes);
        out.add(pbMessage);
//        in.release();
    }
}
