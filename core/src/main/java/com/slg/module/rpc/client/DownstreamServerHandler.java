package com.slg.module.rpc.client;
import com.slg.module.message.ByteBufferServerMessage;
import com.slg.module.util.SentMsgUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.util.concurrent.CompletableFuture;

/**
 * 处理远程服务器连接
 */
@ChannelHandler.Sharable
public class DownstreamServerHandler extends SimpleChannelInboundHandler<ByteBufferServerMessage> {
   private SentMsgUtil sentUtil ;

    public DownstreamServerHandler(SentMsgUtil sentUtil) {
        this.sentUtil = sentUtil;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Connected to downstream server " + ctx.channel().remoteAddress());
    }


    @Override
    protected void channelRead0(ChannelHandlerContext cxt, ByteBufferServerMessage msg) throws Exception {
        int cid = msg.getCid();
        // 获取关联的 Future 并完成
        CompletableFuture<ByteBufferServerMessage> future = sentUtil.getPendingRequests(cid);
        if (future != null) {
            sentUtil.removeCompletableFutureMap(cid);
            future.complete(msg);
        } else {
            System.err.println("Received orphan response for CID: " + cid);
        }
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

