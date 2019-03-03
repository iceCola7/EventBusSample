package com.cxz.eventbuslib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chenxz
 * @date 2019/3/3
 * @desc
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) // 注解会在class字节码文件中存在，jvm在加载的时候可以通过反射获得
public @interface Subscribe {
    ThreadMode threadMode() default ThreadMode.MAIN;
}
