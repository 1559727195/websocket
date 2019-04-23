package com.crazysunj.websocket;

public interface ICallback<T> {

    void onSuccess(T t);

    void onFail(String msg);

}
