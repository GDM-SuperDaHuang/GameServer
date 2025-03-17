package com.slg.module.rpc.server;

import com.slg.module.message.ByteMessage;
import com.slg.module.register.HandleBeanDefinitionRegistryPostProcessor;
import com.slg.module.util.BeanTool;
import io.netty.channel.*;
import io.netty.handler.codec.DecoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;

/**
 * 网关--本地服务器
 */
@Component
@ChannelHandler.Sharable
public class PbMessageHandler extends SimpleChannelInboundHandler<ByteMessage> {


    @Autowired
    private HandleBeanDefinitionRegistryPostProcessor postProcessor;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteMessage ByteMessage) throws Exception {
        long userId = ByteMessage.getUserId();
        int protocolId = ByteMessage.getProtocolId();
//        ByteBuffer byteBuffer = ByteMessage.getByteBuffer();

        Method parse = postProcessor.getParseFromMethod(protocolId);
        if (parse == null) {
            ctx.close();
            return;
        }
        byte[] body = ByteMessage.getBody();
        //todo
        //注册中心获取信息，进行选择
        //本地
        Object msgObject = parse.invoke(null, body);
        //todo
        ByteMessage message = route(ctx, msgObject, protocolId, userId);
        message.setProtocolId(protocolId);
        message.setCid(ByteMessage.getCid());
        message.setUserId(userId);
        message.setErrorCode(message.getErrorCode());
        ctx.writeAndFlush(message);
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


    public ByteMessage route(ChannelHandlerContext ctx, Object message, int protocolId, long userId) throws Exception {
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
        if (invoke instanceof ByteMessage){
            return (ByteMessage)invoke;
        }else {
            return null;
        }
    }
}
