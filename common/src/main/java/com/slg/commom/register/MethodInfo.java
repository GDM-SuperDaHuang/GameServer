package com.slg.commom.register;

import java.lang.reflect.Method;

public class MethodInfo {

    private Class<?> clazz;
    private Method method;

    public MethodInfo(Class<?> clazz, Method method) {
        this.clazz = clazz;
        this.method = method;
    }

    public MethodInfo() {
    }
}
