package com.slg.module.util;

public class ReflectionUtil {
    public static Object createAndFillObject(String classNamePath,String InnerClassName) throws Exception {
        // 步骤1: 加载外部类的Class对象
        Class<?> outerClass = Class.forName(classNamePath);
        Class<?>[] declaredClasses = outerClass.getDeclaredClasses();
        // 遍历所有内部类和接口，查找特定的内部类
        for (Class<?> cls : declaredClasses) {
            if (InnerClassName.equals(cls.getSimpleName())) { // 或者使用cls.getName()进行比较，如果需要包含包名
                // 对于静态内部类，你可以直接创建其实例
                if (java.lang.reflect.Modifier.isStatic(cls.getModifiers())) {
                    // 创建静态内部类的实例（如果有无参构造函数）
                    return cls.getDeclaredConstructor().newInstance();
                }
                break;
            }
        }
        return null;
    }
}