package com.slg.commom.dispatcher;
import com.slg.commom.message.MSG;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;

public interface MessageDispatcher {
    void forwardMessage(ChannelHandlerContext ctx, MSG.Messgae msg, int protobufId);
}
