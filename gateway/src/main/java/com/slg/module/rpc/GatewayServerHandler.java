package com.slg.module.rpc;

import com.slg.module.message.ByteBufferMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;


@Component
@ChannelHandler.Sharable
public class GatewayServerHandler extends SimpleChannelInboundHandler<ByteBufferMessage> {

    private GatewayServer gatewayServer;
    private ConcurrentHashMap<Integer, Channel> channelMap;
    public GatewayServerHandler() {
        this.channelMap = gatewayServer.getChannelMap();
    }


    /**
     *
     * @param channelHandlerContext 网关--用户
     * @param byteBufferMessage
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBufferMessage byteBufferMessage) throws Exception {
        long sessionId = byteBufferMessage.getSessionId();
        int protocolId = byteBufferMessage.getProtocolId();

        //todo 映射算法 下游目标服务器
        if (protocolId>100000){
            Channel channel = channelMap.get(1212);
            channel.writeAndFlush(byteBufferMessage);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}
