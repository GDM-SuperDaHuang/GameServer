//package com.slg.common.rpc;
//import com.slg.common.annotation.RouteExecution;
//import com.slg.common.annotation.RouteExecutionVV;
//import com.slg.common.interfaceT.monitor1.EventPublisher;
//import com.slg.common.message.MSG;
//import com.slg.common.util.BeanTool;
//import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
//import io.grpc.netty.shaded.io.netty.channel.ChannelInboundHandlerAdapter;
//import io.grpc.netty.shaded.io.netty.channel.SimpleChannelInboundHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//public class VersatileServerHandler extends ChannelInboundHandlerAdapter {
//    private static final Logger logger = LoggerFactory.getLogger(VersatileServerHandler.class);
//    private EventPublisher eventPublisher =eventPublisher = BeanTool.getBean(EventPublisher.class);;
//    private RouteExecutionVV routeExecution = BeanTool.getBean(RouteExecutionVV.class);
//
//    /**
//     * 服务端读取消息
//     */
//    @Override
//    public  void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
////        MSG.Request request = message.getRequest();
////        int protocolId = request.getProtocolId();
//
//        System.out.println("====服务器收到信息VVVV"+message);
//        route(ctx, message);
//    }
//
//
//    public void route(ChannelHandlerContext ctx, Object message) throws Exception {
//        //执行方法
//        routeExecution.getBeanAndExecute(ctx, protocolId,request);
//    }
//
//    /**
//     * 服务端异常处理
//     */
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        // 记录异常到日志
//        System.out.println(ctx + "====服务器收到异常======" + cause);
//        // 关闭连接，释放资源
//        ctx.close();
//        // 注意：通常不需要调用super.exceptionCaught(ctx, cause)，因为ChannelInboundHandlerAdapter的此方法为空实现
//    }
//
//}
