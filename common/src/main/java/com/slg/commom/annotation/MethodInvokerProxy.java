//package com.slg.common.annotation;
//import com.slg.common.message.GDM;
//import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
//
//import java.lang.reflect.InvocationHandler;
//import java.lang.reflect.Method;
//import java.lang.reflect.Proxy;
//
//public class MethodInvokerProxy implements InvocationHandler {
//
//    private final Object target;
//
//    public MethodInvokerProxy(Object target) {
//        this.target = target;
//    }
//
//    @Override
//    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        if ("execute".equals(method.getName()) && args != null && args.length > 0
//                && args[0] instanceof String
//                && args[1] instanceof ChannelHandlerContext
//                && args[2] instanceof GDM.Request) {
//            String param0 = (String) args[0];
//            ChannelHandlerContext param1 = (ChannelHandlerContext) args[1];
//            GDM.Request param2 = (GDM.Request) args[2];
//            // 查找并调用匹配的方法
//            Method specificMethod = findAndInvokeSpecificMethod(target, param0, param1, param2);
//            if (specificMethod != null) {
//                // 如果找到了匹配的方法并已调用，则不调用原始方法
//                return null;
//            }
//        }
//        // 如果没有找到匹配的方法或不是execute方法，则直接调用原始方法
//        return method.invoke(target, args);
//    }
//
//    private Method findAndInvokeSpecificMethod(Object target, String param0, ChannelHandlerContext param1, GDM.Request param2) {
//        for (Method m : target.getClass().getMethods()) {
//            if (m.isAnnotationPresent(ToMethod.class)) {
//                ToMethod annotation = m.getAnnotation(ToMethod.class);
//                if (annotation.value().equals(param0)
//                        && m.getParameterCount() == 3
//                        && m.getParameterTypes()[1] == ChannelHandlerContext.class
//                        && m.getParameterTypes()[2] == GDM.Request.class) {
//                    try {
//                        m.invoke(target, param0, param1, param2);
//                        return m; // 返回方法对象，表示已调用
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//        return null; // 没有找到匹配的方法
//    }
//
//    @SuppressWarnings("unchecked")
//    public static <T> T createProxy(T target) {
//        return (T) Proxy.newProxyInstance(
//                target.getClass().getClassLoader(),
//                target.getClass().getInterfaces(),
//                new MethodInvokerProxy(target)
//        );
//    }
//}