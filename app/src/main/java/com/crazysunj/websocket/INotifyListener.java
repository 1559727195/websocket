package com.crazysunj.websocket;

import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//抽象接口
public interface INotifyListener<T> {
    void fire(T t);
}

//标记注解
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface NotifyClass {

    Class<?> value();

}


