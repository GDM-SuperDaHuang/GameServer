package com.slg.module.rpc;



import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;

public interface Process {
    void route(ChannelHandlerContext ctx, Object request) throws Exception;
}
