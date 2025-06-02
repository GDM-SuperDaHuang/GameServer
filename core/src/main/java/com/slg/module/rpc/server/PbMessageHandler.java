package com.slg.module.rpc.server;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import com.slg.module.connection.DHKeyInfo;
import com.slg.module.message.*;
import com.slg.module.register.HandlePbBeanManager;
import com.slg.module.util.BeanTool;
import com.slg.module.util.CryptoUtils;
import com.slg.module.util.LZ4Compression;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.DecoderException;

import javax.crypto.SecretKey;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * 网关--本地服务器
 */
@ChannelHandler.Sharable
public class PbMessageHandler extends SimpleChannelInboundHandler<ByteBufferServerMessage> {

    HandlePbBeanManager handlePbBeanManager = HandlePbBeanManager.getInstance();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBufferServerMessage msg) throws Exception {
        long userId = msg.getUserId();
        int protocolId = msg.getProtocolId();
        ByteBuf zipBuf = null;//解压缩标志
        ByteBuffer original = null;
        ByteBuf reqBody = msg.getBody();

        //解压缩
        if (msg.getZip() == Constants.Zip) {
            short originalLength = reqBody.readShort();
            zipBuf = LZ4Compression.decompress(reqBody, originalLength);
            original = zipBuf.nioBuffer();
        }

        Method parse = handlePbBeanManager.getParseFromMethod(protocolId);
        if (parse == null) {
            if (zipBuf != null) {
                zipBuf.release();
            }
            failedNotificationClient(ctx, msg, ErrorCodeConstants.SERIALIZATION_METHOD_LACK);
            return;
        }
        Object msgObject;
        if (original != null) {
            msgObject = parse.invoke(null, original);
        } else {
            msgObject = parse.invoke(null, reqBody.nioBuffer());
        }


        //响应
        MsgResponse response = route(ctx, msgObject, protocolId, userId);
        if (response == null) {
            failedNotificationClient(ctx, msg, ErrorCodeConstants.SERIALIZATION_METHOD_LACK);
            if (zipBuf != null) {
                zipBuf.release();
            }
            return;
        }
        int cid = msg.getCid();
        msg.recycle();

        if (zipBuf != null) {
            zipBuf.release();
        }

        GeneratedMessage.Builder<?> responseBody = response.getBody();
        Message message = responseBody.buildPartial();
        ByteBuf respBody = ctx.alloc().buffer(message.getSerializedSize());

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
        short bodyLength = (short) respBody.readableBytes(); // 原始数据长度
        if (bodyLength > 20) {
            //先压缩
            ByteBuf compressBuf = LZ4Compression.compressWithLengthHeader(respBody, bodyLength);
            short zipLength = (short) compressBuf.readableBytes();
            ByteBuf out;
            if (zipLength < bodyLength) {
                out = MsgUtil.buildServerMsg(ctx, userId, cid, response.getErrorCode(), protocolId, Constants.Zip, Constants.NoEncrypted, zipLength, compressBuf);
            } else {
                compressBuf.release();
                out = MsgUtil.buildServerMsg(ctx, userId, cid, response.getErrorCode(), protocolId, Constants.NoZip, Constants.NoEncrypted, bodyLength, respBody);
            }
            ChannelFuture channelFuture = ctx.writeAndFlush(out);
            channelFuture.addListener(future -> {
                if (!future.isSuccess()) {
                    out.release();
                    System.err.println("Write and flush failed: " + future.cause());
                }
            });
            return;
        }

        //写回
        ByteBuf out = MsgUtil.buildServerMsg(ctx, userId, cid, response.getErrorCode(), protocolId, 0, 1, bodyLength, respBody);
        //对象回收
        response.recycle();
        ChannelFuture channelFuture = ctx.writeAndFlush(out);
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
            } else {
                out.release();
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
        Class<?> handleClazz = handlePbBeanManager.getClassHandle(protocolId);
        if (handleClazz == null) {
            return null;
        }
        Method handleMethod = handlePbBeanManager.getHandleMethod(protocolId);
        if (handleMethod == null) {
            return null;
        }
        handleMethod.setAccessible(true);
        Object bean = BeanTool.getBean(handleClazz);
        if (bean == null) {
            return null;
        }
        Object invoke = handleMethod.invoke(bean, ctx, message, userId);
        if (invoke instanceof MsgResponse) {
            return (MsgResponse) invoke;
        } else {
            return null;
        }
    }


    private void failedNotificationClient(ChannelHandlerContext ctx, ByteBufferServerMessage msg, int errorCode) {
        //日志记录失败日志 todo

        // 发送失败,直接返回，告诉客户端
        ByteBuf out = MsgUtil.buildClientMsg(ctx, msg.getCid(), errorCode, msg.getProtocolId(), Constants.NoZip, Constants.NoEncrypted, Constants.NoLength, null);
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
