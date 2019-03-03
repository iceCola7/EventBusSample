package com.cxz.eventbuslib;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author chenxz
 * @date 2019/3/3
 * @desc
 */
public class EventBus {

    private static final String TAG = "EventBus";

    private static volatile EventBus instance;
    private Map<Object, List<SubscribeMethod>> cacheMap = new ConcurrentHashMap<>();
    private Handler mHandler;
    private ExecutorService mExecutorService;

    private EventBus() {
        mHandler = new Handler(Looper.getMainLooper());
        mExecutorService = Executors.newCachedThreadPool();
    }

    public static EventBus getDefault() {
        if (instance == null) {
            synchronized (EventBus.class) {
                if (instance == null) {
                    instance = new EventBus();
                }
            }
        }
        return instance;
    }

    public void register(Object object) {
        // 将object中的方法添加到EventBus中来管理
        List<SubscribeMethod> list = cacheMap.get(object);
        if (list == null) {
            list = findSubscribeMethods(object);
            cacheMap.put(object, list);
        }
    }

    private List<SubscribeMethod> findSubscribeMethods(Object object) {
        List<SubscribeMethod> list = new ArrayList<>();
        Class<?> clazz = object.getClass();

        while (clazz != null) {

            // 判断当前是否是系统类，如果是就退出循环
            String name = clazz.getName();
            if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")) {
                break;
            }

            // 得到object中所有的方法
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                // 通过注解 Subscribe 找到需要注册到EventBus中的方法
                Subscribe subscribe = method.getAnnotation(Subscribe.class);
                if (subscribe == null) {
                    continue;
                }
                // 获取方法中的参数，并判断是否唯一
                Class<?>[] types = method.getParameterTypes();
                if (types.length != 1) {
                    Log.e(TAG, "EventBus 只能接受一个参数！");
                }
                // 获取线程模式
                ThreadMode threadMode = subscribe.threadMode();
                SubscribeMethod subscribeMethod = new SubscribeMethod(method, threadMode, types[0]);
                list.add(subscribeMethod);
            }
            // 寻找父类
            clazz = clazz.getSuperclass();
        }

        return list;
    }

    public void post(final Object type) {
        Set<Object> set = cacheMap.keySet();
        Iterator<Object> iterator = set.iterator();
        while (iterator.hasNext()) {
            final Object obj = iterator.next();
            List<SubscribeMethod> list = cacheMap.get(obj);
            if (list == null) return;
            for (final SubscribeMethod subscribeMethod : list) {
                if (subscribeMethod.getType().isAssignableFrom(type.getClass())) {
                    switch (subscribeMethod.getThreadMode()) {
                        // 不管post是在主线程还是在子线程，这里都在主线程
                        case MAIN:
                            // 主 - 主
                            if (Looper.myLooper() == Looper.getMainLooper()) {
                                invoke(subscribeMethod, obj, type);
                            } else {
                                // 子 - 主
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(subscribeMethod, obj, type);
                                    }
                                });
                            }
                            break;
                        // 不管post是在主线程还是在子线程，这里都在子线程
                        case BACKGROUND:
                            // 主 - 子
                            if (Looper.myLooper() == Looper.getMainLooper()) {
                                mExecutorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(subscribeMethod, obj, type);
                                    }
                                });
                            } else {
                                // 子 - 子
                                invoke(subscribeMethod, obj, type);
                            }
                            break;
                    }
                }
            }
        }
    }

    private void invoke(SubscribeMethod subscribeMethod, Object obj, Object type) {
        Method method = subscribeMethod.getMethod();
        try {
            method.invoke(obj, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregister(Object object) {
        cacheMap.remove(object);
    }

}
