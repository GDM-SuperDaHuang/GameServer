package com.slg.commom.rpc;


import com.slg.commom.annotation.ToMethod;
import com.slg.commom.annotation.ToServer;
import com.slg.commom.annotation.route.RouteServer;
import com.slg.commom.register.ToServerBeanDefinitionRegistryPostProcessor;
import com.slg.commom.util.BeanTool;
import com.slg.protobuffile.message.MSG;
import io.grpc.netty.shaded.io.netty.buffer.ByteBuf;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandler;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
import io.grpc.netty.shaded.io.netty.handler.codec.MessageToMessageCodec;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 确保之前的包完整性
 */
@ChannelHandler.Sharable
public class MessageCodecVVV extends MessageToMessageCodec<ByteBuf, MSG.Message> {
    Map<Long, Long> sessionMap = new ConcurrentHashMap<>();

    @Autowired
    public ToServerBeanDefinitionRegistryPostProcessor postProcessor;

    //出
    @Override
    protected void encode(ChannelHandlerContext ctx, MSG.Message msg, List<Object> list) throws Exception {
        System.out.println("99999999999999999999999");
        ByteBuf out = ctx.alloc().buffer();
        // 1. 8 字节session
        out.writeLong(1111111111L);
        //消息id
        out.writeInt(0);
        //压缩标志
        out.writeByte(0);
        //版本
        out.writeByte(3);
        //长度
        byte[] byteArray = msg.toByteArray();
        out.writeShort(byteArray.length);
        //写入protobuf
        out.writeBytes(byteArray);
        list.add(out);
    }

    //入
    @Override
    protected void decode(ChannelHandlerContext cxt, ByteBuf in, List<Object> out) throws Exception {
        //sessionId
        long sessionId = in.readLong();
        //消息id
        int orderId = in.readInt();
        // 压缩标志
        byte zip = in.readByte();
        //版本
        byte pbVersion = in.readByte();
        //消息id
        int protocolId = in.readInt();
        //长度
        short length = in.readShort();
        byte[] bytes = new byte[length];
        // 6. 读取protobuf字节数组
        in.readBytes(bytes, 0, length);

        Method parse = postProcessor.getParseFromMethodMap(protocolId);
        Object invoke = parse.invoke(null, bytes);
        out.add(invoke);

//        // todo 获取对象 MSG.Message
//        Reflections reflections = new Reflections(new ConfigurationBuilder()
//                .setUrls(ClasspathHelper.forPackage(""))
//                .setScanners(new SubTypesScanner(false)));
//        Set<Class<? extends RouteServer>> classes = reflections.getSubTypesOf(RouteServer.class);
//
//        for (Class<?> clazz : classes) {
//            if (clazz.isAnnotationPresent(ToServer.class)) {
//                Object bean = BeanTool.getBean(clazz);
//                // 查找并调用方法
//                Method[] methods = clazz.getDeclaredMethods();
//                for (Method method : methods) {
//                    if (method.isAnnotationPresent(ToMethod.class)) {
//                        ToMethod annotation = method.getAnnotation(ToMethod.class);
//                        if (annotation.value() != protocolId) {
//                            continue;
//                        }
//                        // 获取方法的所有参数类型
//                        Class<?>[] parameterTypes = method.getParameterTypes();
//                        //获取第二个参数，加载Protobuf类
//                        Class<?> parameterType = parameterTypes[1];
//
//                        // 获取parseFrom方法
//                        Method parseFromMethod = parameterType.getMethod("parseFrom", byte[].class);
//
//                        // 调用parseFrom方法
//                        Object msgObject = parseFromMethod.invoke(null, bytes);
//                        out.add(msgObject);
//                    }
//                }
//            }
//        }

//
//        //反序列化成java对象
//        MSG.Message message = MSG.Message.parseFrom(bytes);
////        out.add(message);
//        out.add(bytes);
    }
}
