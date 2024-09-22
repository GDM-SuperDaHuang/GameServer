package com.slg.common.protubuf;

import io.grpc.netty.shaded.io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ProtobufLengthDecoder extends LengthFieldBasedFrameDecoder {
    public ProtobufLengthDecoder(){
        this(1024,12,4,0,0);
    }
    public ProtobufLengthDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }


}
