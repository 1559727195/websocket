package com.massky.wsface.Iback;

import android.content.Context;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//抽象接口
public interface INotifyListener<T> {
    void fire(T t, Context context);
}

//标记注解
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface NotifyClass {

    Class<?> value();

}


