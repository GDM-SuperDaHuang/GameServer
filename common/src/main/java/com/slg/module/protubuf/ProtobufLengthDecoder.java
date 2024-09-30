package com.slg.module.protubuf;

//import io.grpc.netty.shaded.io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ProtobufLengthDecoder extends LengthFieldBasedFrameDecoder {
    public ProtobufLengthDecoder(){
        this(4096,12,4,0,0);
    }

    /**
     *
     * @param maxFrameLength 桢最大值
     * @param lengthFieldOffset 长度字段偏移字节数
     * @param lengthFieldLength 长度字段字节大小
     * @param lengthAdjustment  长度字段离消息体偏移字节量
     * @param initialBytesToStrip 剥离消息头一定数量数据字节
     */
    public ProtobufLengthDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }


}
