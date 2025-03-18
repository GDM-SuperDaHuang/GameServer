package com.slg.module.rpc.server;

import com.google.protobuf.GeneratedMessage;
import com.slg.module.message.ByteBufferMessage;
import com.slg.module.message.MsgResponse;
import com.slg.module.register.HandleBeanDefinitionRegistryPostProcessor;
import com.slg.module.util.BeanTool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.DecoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * 网关--本地服务器
 */
@Component
@ChannelHandler.Sharable
public class PbMessageHandler extends SimpleChannelInboundHandler<ByteBufferMessage> {


    @Autowired
    private HandleBeanDefinitionRegistryPostProcessor postProcessor;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBufferMessage msg) throws Exception {
        long userId = msg.getUserId();
        int protocolId = msg.getProtocolId();
//        ByteBuffer byteBuffer = ByteMessage.getByteBuffer();

        Method parse = postProcessor.getParseFromMethod(protocolId);
        if (parse == null) {
            ctx.close();
            return;
        }
        ByteBuffer body = msg.getBody();
        //todo
        //注册中心获取信息，进行选择
        //本地
        Object msgObject = parse.invoke(null, body);
        //todo
        MsgResponse message = route(ctx, msgObject, protocolId, userId);
        GeneratedMessage.Builder<?> responseBody = message.getBody();
        byte[] bodyByteArr = responseBody.buildPartial().toByteArray();

        //写回
        ByteBuf out = Unpooled.buffer(16);

        //消息头
        out.writeLong(msg.getUserId());      // 8字节
        out.writeInt(msg.getCid());      // 4字节
        out.writeInt(message.getErrorCode());      // 4字节
        out.writeInt(msg.getProtocolId());      // 4字节
        out.writeByte(0);                       // zip压缩标志，1字节
        out.writeByte(1);                       // pb版本，1字节
        out.writeShort(bodyByteArr.length);                 // 消息体长度，2字节
        // 写入消息体
        out.writeBytes(bodyByteArr);
        ctx.writeAndFlush(out);
        // 释放 ByteBuf
        out.release();
    }

    //todo
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


    public MsgResponse route(ChannelHandlerContext ctx, Object message, int protocolId, long userId) throws Exception {
        Class<?> handleClazz = postProcessor.getHandleMap(protocolId);
        if (handleClazz == null) {
            return null;
        }
        Method method = postProcessor.getMethodMap(protocolId);
        if (method == null) {
            return null;
        }
        method.setAccessible(true);
        Object bean = BeanTool.getBean(handleClazz);
        if (bean == null) {
            return null;
        }
        Object invoke = method.invoke(bean, ctx, message, userId);
        if (invoke instanceof MsgResponse){
            return (MsgResponse)invoke;
        }else {
            return null;
        }
    }
}
