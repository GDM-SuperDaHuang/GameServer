package com.slg.commom.rpc;


import com.slg.protobuffile.message.MSG;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;

public interface Process {
    void route(ChannelHandlerContext ctx, MSG.Message request) throws Exception;
}
