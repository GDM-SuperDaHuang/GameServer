package com.slg.common.rpc;

import com.slg.common.message.MSG;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;

public interface Process {
    void route(ChannelHandlerContext ctx, MSG.Message request) throws Exception;
}
