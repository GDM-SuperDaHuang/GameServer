package com.slg.module.rpc;

import com.slg.module.register.HandleBeanDefinitionRegistryPostProcessor;
import com.slg.module.util.BeanTool;
import io.grpc.netty.shaded.io.netty.buffer.ByteBuf;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandler;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
import io.grpc.netty.shaded.io.netty.handler.codec.ByteToMessageDecoder;
import io.grpc.netty.shaded.io.netty.handler.codec.MessageToByteEncoder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 确保之前的包完整性
 */
@Component
public class MsgEncode extends MessageToByteEncoder<Object> { //INBOUND_IN, OUTBOUND_IN
    //    Map<Long, Long> sessionMap = new ConcurrentHashMap<>();
    private HandleBeanDefinitionRegistryPostProcessor postProcessor;
    public MsgEncode(HandleBeanDefinitionRegistryPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }
    private short length = 0;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object object, ByteBuf byteBuf) throws Exception {

    }
}
