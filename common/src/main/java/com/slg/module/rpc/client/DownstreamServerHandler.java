package com.slg.module.rpc.client;

import com.slg.module.message.ByteBufferMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;
import org.springframework.stereotype.Component;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;

/**
 * 处理远程服务器连接
 */
@Component
@ChannelHandler.Sharable
public class DownstreamServerHandler extends SimpleChannelInboundHandler<ByteBufferMessage> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Connected to downstream server " + ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext cxt, ByteBufferMessage byteBufferMessage) throws Exception {
//        int protocolId = byteBufferMessage.getProtocolId();
//        long sessionId = byteBufferMessage.getSessionId();
//        Method parse = postProcessor.getParseFromMethod(protocolId);
//        if (parse == null) {
//            cxt.close();
//            return;
//        }
//        Object msgObject = parse.invoke(null, byteBufferMessage.getByteBuffer());
//        //todo
//        route(cxt, msgObject, protocolId,sessionId);
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

