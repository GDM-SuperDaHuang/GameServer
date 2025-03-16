package com.slg.module.rpc.server;

import com.google.protobuf.GeneratedMessage;
import com.slg.module.message.ByteBufferMessage;
import com.slg.module.message.ByteMessage;
import com.slg.module.message.MsgResponse;
import com.slg.module.register.HandleBeanDefinitionRegistryPostProcessor;
import com.slg.module.util.BeanTool;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
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
    protected void channelRead0(ChannelHandlerContext ctx, ByteBufferMessage ByteMessage) throws Exception {
        long userId = ByteMessage.getUserId();
        int cid = ByteMessage.getCid();
        int errorCode = ByteMessage.getErrorCode();
        int protocolId = ByteMessage.getProtocolId();
        ByteBuffer byteBuffer = ByteMessage.getByteBuffer();


        Method parse = postProcessor.getParseFromMethod(protocolId);
        if (parse == null) {
            ctx.close();
            return;
        }

        byte[] bytes={1,2,4,6} ;
        Object msgObject2 = parse.invoke(null, bytes);

        //todo
        //注册中心获取信息，进行选择
        //本地
        Object msgObject = parse.invoke(null, byteBuffer);
        //todo
        ByteBufferMessage message = route(ctx, msgObject, protocolId, userId);

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


    public ByteBufferMessage route(ChannelHandlerContext ctx, Object message, int protocolId, long userId) throws Exception {
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
        if (invoke instanceof ByteBufferMessage){
            return (ByteBufferMessage)invoke;
        }else {
            return null;
        }
    }
}
