package com.slg.module.rpc.client;

import com.slg.module.message.ByteBufferMessage;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.util.concurrent.CompletableFuture;

/**
 * 处理远程服务器连接
 */
@Component
@ChannelHandler.Sharable
public class DownstreamServerHandler extends SimpleChannelInboundHandler<ByteBufferMessage> {
    @Autowired
    private SentUtil sentUtil;


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Connected to downstream server " + ctx.channel().remoteAddress());
    }


    @Override
    protected void channelRead0(ChannelHandlerContext cxt, ByteBufferMessage msg) throws Exception {
        long cid = msg.getCid();
        int protocolId = msg.getProtocolId();
        byte[] body = msg.getBody(); // 假设 ByteBufferMessage 有 getBody() 方法
        // 获取关联的 Future 并完成
        CompletableFuture<ByteBufferMessage> future = sentUtil.getPendingRequests(cid);
        if (future != null) {
            sentUtil.removeCompletableFutureMap(cid);
            future.complete(msg);
        } else {
            System.err.println("Received orphan response for CID: " + cid);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof InvocationTargetException) {
            //目标方法错误
        } else if (cause instanceof SocketException
                || cause instanceof DecoderException) {
            //客户端关闭连接/连接错误
            // 关闭连接
            ctx.close();
        }
    }
}

