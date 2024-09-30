package com.slg.module.rpc;

import com.slg.module.message.PbMessage;
import com.slg.module.register.HandleBeanDefinitionRegistryPostProcessor;
import com.slg.module.util.BeanTool;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;

@Component
@ChannelHandler.Sharable
public class PbMessageHandler extends SimpleChannelInboundHandler<PbMessage> {

    @Autowired
    private HandleBeanDefinitionRegistryPostProcessor postProcessor;

    @Override
    protected void channelRead0(ChannelHandlerContext cxt, PbMessage pbMessage) throws Exception {
        int protocolId = pbMessage.getProtocolId();
        Method parse = postProcessor.getParseFromMethod(protocolId);
        if (parse == null) {
            cxt.close();
            return;
        }
        Object msgObject = parse.invoke(null, pbMessage.getData());
        route(cxt, msgObject, protocolId);
        cxt.close();
    }


    public void route(ChannelHandlerContext ctx, Object message, int protocolId) throws Exception {
        Class<?> handleClazz = postProcessor.getHandleMap(protocolId);
        if (handleClazz == null) {
            ctx.close();
            return;
        }
        Method method = postProcessor.getMethodMap(protocolId);
        if (method == null) {
            ctx.close();
            return;
        }
        method.setAccessible(true);
        Object bean = BeanTool.getBean(handleClazz);
        if (bean == null) {
            ctx.close();
            return;
        }
        method.invoke(bean, ctx, message);
    }
}
