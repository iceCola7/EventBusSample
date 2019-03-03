package com.cxz.eventbuslib;

import java.lang.reflect.Method;

/**
 * @author chenxz
 * @date 2019/3/3
 * @desc
 */
public class SubscribeMethod {

    // 回调方法
    private Method method;

    // 线程模式
    private ThreadMode threadMode;

    // 回调方法中的参数
    private Class<?> type;

    public SubscribeMethod(Method method, ThreadMode threadMode, Class<?> type) {
        this.method = method;
        this.threadMode = threadMode;
        this.type = type;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public ThreadMode getThreadMode() {
        return threadMode;
    }

    public void setThreadMode(ThreadMode threadMode) {
        this.threadMode = threadMode;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }
}
