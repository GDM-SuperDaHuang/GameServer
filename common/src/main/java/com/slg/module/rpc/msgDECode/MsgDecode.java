package com.slg.module.rpc.msgDECode;


import com.slg.module.message.ByteBufferMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 注意确保之前的包完整性,--入
 */
public class MsgDecode extends ByteToMessageDecoder {

    //入
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 确保有足够的字节来读取头部
        if (in.readableBytes() < 16) {
            return;
        }
        // 缓存 readableBytes 不够则暂时到局部变量
        int readableBytes = in.readableBytes();

        // 消息头
        long userId = in.readLong();
        int cid = in.readInt();
        int errorCode = in.readInt();
        int protocolId = in.readInt();
        byte zip = in.readByte();
        byte pbVersion = in.readByte();
        short length = in.readShort();
        // 检查是否有足够的字节来读取整个消息体
        if (readableBytes < 16 + length) {
            // 如果没有，丢弃已经读取的头部信息，并返回
            in.readerIndex(in.readerIndex() - 16);
            return;
        }
        //消息体
        ByteBuf messageBody = in.readBytes(length);


        //方式1
        ByteBuffer byteBuffer = messageBody.nioBuffer();
        ByteBufferMessage byteBufferMessage = new ByteBufferMessage(userId, cid, errorCode, protocolId, byteBuffer);
        out.add(byteBufferMessage);
        //释放 messageBody 的引用
        messageBody.release();

        //方式二
//        byte[] array = messageBody.array();
//        ByteMessage byteBufMessage = new ByteMessage(userId, cid, errorCode, protocolId, array);
//        out.add(byteBufMessage);
//        //释放 messageBody 的引用
//        messageBody.release();
    }
}
