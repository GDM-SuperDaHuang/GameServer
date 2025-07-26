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
import io.netty.util.concurrent.FastThreadLocal;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 网关--本地服务器
 */
@ChannelHandler.Sharable
public class PbMessageHandler extends SimpleChannelInboundHandler<ByteBufferServerMessage> {
    HandlePbBeanManager handlePbBeanManager = HandlePbBeanManager.getInstance();


    // 虚拟线程池（每个用户一个专用虚拟线程）
    private final ConcurrentHashMap<Long, ExecutorService> userThreadMap = new ConcurrentHashMap<>();

    // 虚拟线程工厂（命名线程）
    private static final ThreadFactory virtualThreadFactory = Thread.ofVirtual()
            .name("user-processor-", 0)
            .factory();


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBufferServerMessage msg) {
        long userId = msg.getUserId();
        int protocolId = msg.getProtocolId();
        final EventLoop worker = ctx.channel().eventLoop();
        // 获取或创建用户专属虚拟线程
        ExecutorService userExecutor = userThreadMap.computeIfAbsent(userId, uid -> Executors.newSingleThreadExecutor(virtualThreadFactory));

        // 提交任务到用户专属虚拟线程
        userExecutor.execute(() -> {
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
                    safeRelease(ctx, zipBuf);
//                    zipBuf.release();
                }
                failedNotificationClient(ctx, msg, ErrorCodeConstants.SERIALIZATION_METHOD_LACK);
                return;
            }
            Object msgObject;
            if (original != null) {
                try {
                    msgObject = parse.invoke(null, original);
                } catch (Exception e) {
                    failedNotificationClient(ctx, msg, ErrorCodeConstants.SERIALIZATION_METHOD_LACK);
                    return;
                }
            } else {
                try {
                    msgObject = parse.invoke(null, reqBody.nioBuffer());
                } catch (Exception e) {
                    failedNotificationClient(ctx, msg, ErrorCodeConstants.SERIALIZATION_METHOD_LACK);
                    return;
                }
            }


            //响应
            MsgResponse response = route(ctx, msgObject, protocolId, userId);
            if (response == null) {
                failedNotificationClient(ctx, msg, ErrorCodeConstants.SERIALIZATION_METHOD_LACK);
                if (zipBuf != null) {
                    safeRelease(ctx, zipBuf);
//                    zipBuf.release();
                }
                return;
            }
            int cid = msg.getCid();
            //释放
            msg.recycle();

            if (zipBuf != null) {
                safeRelease(ctx, zipBuf);
//                zipBuf.release();
            }

            GeneratedMessage.Builder<?> responseBody = response.getBody();
            Message message = responseBody.buildPartial();
            ByteBuf respBody = ctx.alloc().buffer(message.getSerializedSize());

            try {
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
            } catch (IOException e) {
                // 日志记录 todo
                safeRelease(ctx, respBody);
//                respBody.release();
                return;
            }

            short bodyLength = (short) respBody.readableBytes(); // 原始数据长度
            if (bodyLength > 20) {
                //先压缩
                ByteBuf compressBuf = LZ4Compression.compressWithLengthHeader(respBody, bodyLength);
                short zipLength = (short) compressBuf.readableBytes();
                ByteBuf out;
                if (zipLength < bodyLength) {
                    out = MsgUtil.buildServerMsg(ctx, userId, cid, response.getErrorCode(), protocolId, Constants.Zip, Constants.NoEncrypted, zipLength, compressBuf);
                } else {
                    safeRelease(ctx, compressBuf);
//                    compressBuf.release();
                    out = MsgUtil.buildServerMsg(ctx, userId, cid, response.getErrorCode(), protocolId, Constants.NoZip, Constants.NoEncrypted, bodyLength, respBody);
                }
                ChannelFuture channelFuture = ctx.writeAndFlush(out);
                channelFuture.addListener(future -> {
                    if (!future.isSuccess()) {
//                        out.release();
                        System.err.println("Write and flush failed: " + future.cause());
                    }
                });
                safeRelease(ctx, respBody);
//                respBody.release();
                return;
            }

            //写回
            ByteBuf out = MsgUtil.buildServerMsg(ctx, userId, cid, response.getErrorCode(), protocolId, 0, 1, bodyLength, respBody);
            //对象回收
            safeRelease(ctx,response);
//            response.recycle();
            ChannelFuture channelFuture = ctx.writeAndFlush(out);
            channelFuture.addListener(future -> {
                if (future.isSuccess()) {
                } else {
//                    out.release();
                    System.err.println("Write and flush failed!!!!!: " + future.cause());
                }
            });
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

    private MsgResponse route(ChannelHandlerContext ctx, Object message, int protocolId, long userId) {
        try {
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
        } catch (Exception e) {
            return null;
        }
    }


    private void failedNotificationClient(ChannelHandlerContext ctx, ByteBufferServerMessage msg, int errorCode) {
        //日志记录失败日志 todo

        // 发送失败,直接返回，告诉客户端
        ByteBuf out = MsgUtil.buildClientMsg(ctx, msg.getCid(), errorCode, msg.getProtocolId(), Constants.NoZip, Constants.NoEncrypted, Constants.NoLength, null);
        ChannelFuture channelFuture = ctx.writeAndFlush(out);
        channelFuture.addListener(future -> {
            safeRelease(ctx,msg);
            msg.recycle();
            if (!future.isSuccess()) {//通知客户端失败 日志 todo
                System.err.println("Write and flush failed: " + future.cause());
            } else {
            }
        });
    }


    private void safeRelease(ChannelHandlerContext ctx, ByteBuf resources) {
        ctx.executor().execute(() -> {
            if (resources != null) {
                resources.release();
            }
        });
    }

    private void safeRelease(ChannelHandlerContext ctx, MsgResponse resources) {
        ctx.executor().execute(() -> {
            if (resources != null) {
                resources.recycle();
            }
        });
    }

    private void safeRelease(ChannelHandlerContext ctx, ByteBufferServerMessage resources) {
        ctx.executor().execute(() -> {
            if (resources != null) {
                resources.recycle();
            }
        });
    }



}
