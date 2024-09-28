//package com.slg.commom.rpc;
//
//import com.slg.commom.annotation.RouteExecutionVV;
//import com.slg.commom.interfaceT.monitor1.EventPublisher;
//import com.slg.commom.util.BeanTool;
//import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
//import io.grpc.netty.shaded.io.netty.channel.ChannelInboundHandlerAdapter;
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
////        byte[] bytes = (byte[]) message;
//
//        //执行方法
//        routeExecution.getBeanAndExecute(ctx, bytes);
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
