package com.slg.commom.register;

import com.slg.commom.annotation.ToMethod;
import com.slg.commom.annotation.ToServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ToServerBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    private final Map<Integer, MethodInfo> methodInfoMap = new ConcurrentHashMap<>();

    private final Map<Integer, Class<?>> classMap = new ConcurrentHashMap<>();
    private final Map<Integer, Method> parseFromMethodMap = new ConcurrentHashMap<>();

    //todo
    private final Map<Integer, MethodInfo> handleMap = new ConcurrentHashMap<>();
    private final Map<Integer, MethodInfo> methodMap = new ConcurrentHashMap<>();

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ToServer.class));
        String[] basePackages = new String[]{""}; // 替换为你的包名
        for (String basePackage : basePackages) {
            for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
                Class<?> clazz = null;
                try {
                    clazz = Class.forName(candidate.getBeanClassName());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                if (clazz.isAnnotationPresent(ToServer.class)) {
                    registry.registerBeanDefinition(clazz.getSimpleName(), candidate);
                    // 处理@ToMethod注解的方法
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(ToMethod.class)) {
                            int key = method.getAnnotation(ToMethod.class).value();
                            // 获取方法的所有参数类型
                            Class<?>[] parameterTypes = method.getParameterTypes();
                            int length = parameterTypes.length;
                            //获取第二个参数，加载Protobuf类
                            Class<?> parameterType = parameterTypes[1];
                            classMap.put(key, parameterType);
                            try {
                                // 获取parseFrom方法
                                Method parseFromMethod = parameterType.getMethod("parseFrom", byte[].class);
                                parseFromMethodMap.put(key, parseFromMethod);
                            } catch (NoSuchMethodException e) {
                                throw new RuntimeException(e);
                            }
                            methodInfoMap.put(key, new MethodInfo(clazz, method));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 这里可以进一步处理beanFactory，但在这个案例中我们主要关注registry
    }

    public MethodInfo getMethodInfoMap (Integer key) {
        return methodInfoMap.getOrDefault(key, null);
    }

    public Method getParseFromMethodMap (Integer key) {
        return parseFromMethodMap.getOrDefault(key, null);
    }

    public  Class<?> getClassMap (Integer key) {
        return classMap.getOrDefault(key, null);
    }


    // 提供访问methodInfoMap的方法（例如通过@Autowired注入此类并访问）
}