package com.slg.module.rpc.server;

import com.slg.module.message.ByteBufferMessage;
import com.slg.module.register.HandleBeanDefinitionRegistryPostProcessor;
import com.slg.module.util.BeanTool;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;

@Component
@ChannelHandler.Sharable
public class PbMessageHandler extends SimpleChannelInboundHandler<ByteBufferMessage> {


    @Autowired
    private HandleBeanDefinitionRegistryPostProcessor postProcessor;

    @Override
    protected void channelRead0(ChannelHandlerContext cxt, ByteBufferMessage byteBufferMessage) throws Exception {
        int protocolId = byteBufferMessage.getProtocolId();
        long sessionId = byteBufferMessage.getSessionId();
        Method parse = postProcessor.getParseFromMethod(protocolId);
        if (parse == null) {
            cxt.close();
            return;
        }
        Object msgObject = parse.invoke(null, byteBufferMessage.getByteBuffer());
        //todo
        route(cxt, msgObject, protocolId,sessionId);
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


    public void route(ChannelHandlerContext ctx, Object message, int protocolId,long userId) throws Exception {
        Class<?> handleClazz = postProcessor.getHandleMap(protocolId);
        if (handleClazz == null) {
//            ctx.close();
            return;
        }
        Method method = postProcessor.getMethodMap(protocolId);
        if (method == null) {
//            ctx.close();
            return;
        }
        method.setAccessible(true);
        Object bean = BeanTool.getBean(handleClazz);
        if (bean == null) {
//            ctx.close();
            return;
        }
        method.invoke(bean, ctx, message,userId);
    }
}
