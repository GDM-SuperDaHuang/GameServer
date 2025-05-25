package com.slg.module.rpc.server;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import com.slg.module.message.*;
import com.slg.module.register.HandleBeanDefinitionRegistryPostProcessor;
import com.slg.module.util.BeanTool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.DecoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * 网关--本地服务器
 */
@Component
@ChannelHandler.Sharable
public class PbMessageHandler extends SimpleChannelInboundHandler<ByteBufferServerMessage> {
    @Autowired
    private HandleBeanDefinitionRegistryPostProcessor postProcessor;

    @Autowired
    private MsgUtil msgUtil;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBufferServerMessage msg) throws Exception {
        long userId = msg.getUserId();
        int protocolId = msg.getProtocolId();
        Method parse = postProcessor.getParseFromMethod(protocolId);
        if (parse == null) {
            ctx.close();
            failedNotificationClient(ctx, msg, ErrorCodeConstants.SERIALIZATION_METHOD_LACK);
            return;
        }
        ByteBuffer body = msg.getBody().nioBuffer();
        //本地
        Object msgObject = parse.invoke(null, body);
        MsgResponse response = route(ctx, msgObject, protocolId, userId);
        if (response == null) {
            failedNotificationClient(ctx, msg, ErrorCodeConstants.SERIALIZATION_METHOD_LACK);
            return;
        }

        GeneratedMessage.Builder<?> responseBody = response.getBody();
        Message message = responseBody.buildPartial();
        ByteBuf respBody = ByteBufAllocator.DEFAULT.buffer(message.getSerializedSize());// 预分配精确大小
        message.writeTo(new OutputStream() {
            @Override
            public void write(int b) {
                respBody.writeByte(b);
            }

            @Override
            public void write(byte[] b, int off, int len) {
                respBody.writeBytes(b, off, len);
            }
        });
        short bodyLength = (short) respBody.readableBytes(); // 这里获取长度
        //写回
        ByteBuf out = msgUtil.buildServerMsg(userId, msg.getCid(), response.getErrorCode(), protocolId, 0, 1, bodyLength, respBody);
        //对象回收
        response.recycle();
        ChannelFuture channelFuture = ctx.writeAndFlush(out);
        channelFuture.addListener(future -> {
            respBody.release();
            msg.recycle();
            if (future.isSuccess()) {
            } else {
                System.err.println("Write and flush failed!!!!!: " + future.cause());
            }
        });

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


    private MsgResponse route(ChannelHandlerContext ctx, Object message, int protocolId, long userId) throws Exception {
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
        if (invoke instanceof MsgResponse) {
            return (MsgResponse) invoke;
        } else {
            return null;
        }
    }


    private void failedNotificationClient(ChannelHandlerContext ctx, ByteBufferServerMessage msg, int errorCode) {
        //日志记录失败日志 todo

        // 发送失败,直接返回，告诉客户端
        ByteBuf out = msgUtil.buildClientMsg(msg.getCid(), errorCode, msg.getProtocolId(), Constants.NoZip, Constants.NoEncrypted, Constants.NoLength, null);
        ChannelFuture channelFuture = ctx.writeAndFlush(out);
        channelFuture.addListener(future -> {
            msg.recycle();
            if (!future.isSuccess()) {//通知客户端失败 日志 todo
                System.err.println("Write and flush failed: " + future.cause());
            } else {
            }
        });
    }


}
