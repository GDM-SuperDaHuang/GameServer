//package com.slg.module.rpc;
//import com.slg.module.annotation.RouteExecution;
//import com.slg.module.interfaceT.monitor1.EventPublisher;
//import com.slg.module.util.BeanTool;
//import com.slg.module.message.MSG;
//import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
//import io.grpc.netty.shaded.io.netty.channel.SimpleChannelInboundHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class MyServerHandler extends SimpleChannelInboundHandler<MSG.Message> implements Process {
//    private static final Logger logger = LoggerFactory.getLogger(MyServerHandler.class);
//    private EventPublisher eventPublisher =eventPublisher = BeanTool.getBean(EventPublisher.class);;
//    private RouteExecution routeExecution = BeanTool.getBean(RouteExecution.class);
//
//    /**
//     * 服务端读取消息
//     */
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, MSG.Message message) throws Exception {
//        MSG.Request request = message.getRequest();
//        int protocolId = request.getProtocolId();
//        System.out.println("====服务器收到信息======"+message);
//        route(ctx, message);
//    }
//
//    @Override
//    public void route(ChannelHandlerContext ctx, MSG.Message message) throws Exception {
//        MSG.Request request = message.getRequest();
//        int protocolId = request.getProtocolId();
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
//}
