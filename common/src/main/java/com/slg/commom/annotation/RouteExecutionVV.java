package com.slg.commom.annotation;

import com.google.protobuf.ByteString;
import com.slg.commom.annotation.route.RouteServer;
import com.slg.commom.util.BeanTool;
import com.slg.protobuffile.message.MSG;
import io.grpc.netty.shaded.io.netty.buffer.ByteBuf;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class RouteExecutionVV {
    //    private static final Map<String, String> ENUMTOCLASSMAP = new HashMap<>();
    private static final Map<Integer, Object> HandleMap = new HashMap<>();
    private static final Map<Integer, Method> MethodMap = new HashMap<>();
    private static String HANDLERPATH = "";

//    static {
//        try {
//            // 使用ClassLoader来加载资源
//            InputStream inputStream = RouteExecution.class.getClassLoader().getResourceAsStream("route.xml");
//            if (inputStream == null) {
//                throw new RuntimeException("Resource not found: route.xml");
//            }
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(inputStream);
//            doc.getDocumentElement().normalize();
//
//            NodeList entries = doc.getElementsByTagName("entry");
//            for (int i = 0; i < entries.getLength(); i++) {
//                Element entry = (Element) entries.item(i);
//                String key = entry.getAttribute("key");
//                String value = entry.getTextContent().trim(); // 可能需要trim()来去除前后的空白字符
//                ENUMTOCLASSMAP.put(key, value);
//            }
//            NodeList handlerPath = doc.getElementsByTagName("handlerPath");
//            Node item = handlerPath.item(0);
//            HANDLERPATH = item.getTextContent().trim();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public static void getBeanAndExecute(ChannelHandlerContext ctx, byte[] messageBytes) {
////        Object handle = HandleMap.get(protocolId);
////        if (handle != null) {
////            Method method = MethodMap.get(protocolId);
////            try {
////                if (method != null) {
////                    method.invoke(handle, ctx, request);
////                    ctx.close();
////                    return;
////                }
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
////        }
//
//        Reflections reflections = new Reflections(new ConfigurationBuilder()
//                .setUrls(ClasspathHelper.forPackage(HANDLERPATH))
//                .setScanners(new SubTypesScanner(false)));
//
//        boolean flag = false;
//        Set<Class<? extends RouteServer>> classes = reflections.getSubTypesOf(RouteServer.class);
//        for (Class<?> clazz : classes) {
//            if (clazz.isAnnotationPresent(ToServer.class)) {
//                Object bean = BeanTool.getBean(clazz);
//                // 查找并调用方法
//                Method[] methods = clazz.getDeclaredMethods();
//                for (Method method : methods) {
//                    if (method.isAnnotationPresent(ToMethod.class)) {
//                        ToMethod annotation = method.getAnnotation(ToMethod.class);
//
//                        //--todo
//                        if (annotation.value() != protocolId) {
//                            continue;
//                        }
//                        // 获取方法的所有参数类型
//                        Class<?>[] parameterTypes = method.getParameterTypes();
//                        //获取第二个参数，加载Protobuf类
//                        Class<?> parameterType = parameterTypes[1];
//                        // 获取parseFrom方法
//                        Method parseFromMethod = clazz.getMethod("parseFrom", ByteString.class);
//                        //todo
//                        // 使用ByteString封装数据
//                        ByteString byteString = ByteString.copyFrom(messageBytes);
//
//                        // 调用parseFrom方法
//                        Object invoke = parseFromMethod.invoke(null, byteString);
//
//
//
//
//
//
//                        try {
//                            flag = true;
//                            method.setAccessible(true);
////                            MSG.Request.RequestOneofCase requestOneofCase = request.getRequestOneofCase();
////                            Class<MSG.Request.RequestOneofCase> enumClass = MSG.Request.RequestOneofCase.class;
////                            Object enumc = BeanTool.getBean(enumClass);
//                            method.invoke(bean, ctx, object);
//                            HandleMap.put(protocolId, bean);
//                            MethodMap.put(protocolId, method);
//                            ctx.close();
//                            return;
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }
//        //释放
//        if (!flag) {
//            ctx.close();
//        }
//    }
//

//    public static void getBeanAndExecute(String param, ChannelHandlerContext ctx, GDM.Request request) {
//        Reflections reflections = new Reflections(new ConfigurationBuilder()
//                .setUrls(ClasspathHelper.forPackage(HANDLERPATH))
//                .setScanners(new SubTypesScanner(false)));
//
//        boolean flag = false;
//        Set<Class<? extends RouteServer>> classes = reflections.getSubTypesOf(RouteServer.class);
//        for (Class<?> clazz : classes) {
//            if (clazz.isAnnotationPresent(ToServer.class)) {
//                ToServer toServer = clazz.getAnnotation(ToServer.class);
//                // 注解参数对比
//                String methodName = toServer.value();
//                String orDefault = ENUMTOCLASSMAP.getOrDefault(param, null);
//                if (orDefault == null) {
//                    continue;
//                }
//                if (!methodName.equals(orDefault)) {
//                    continue;
//                }
//                Object bean = BeanTool.getBean(clazz);
//                // 查找并调用方法
//                Method[] methods = clazz.getDeclaredMethods();
//                for (Method method : methods) {
//                    if (method.isAnnotationPresent(ToMethod.class)) {
//                        ToMethod annotation = method.getAnnotation(ToMethod.class);
//                        if (!annotation.value().equals(param)) {
//                            continue;
//                        }
//                        try {
//                            flag=true;
//                            method.setAccessible(true);
//                            method.invoke(bean, ctx, request);
//                            ctx.close();
//                            return;
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }
//        if (!flag){
//            ctx.close();
//        }
//    }

}