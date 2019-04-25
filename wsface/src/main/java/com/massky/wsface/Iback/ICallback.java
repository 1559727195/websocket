package com.massky.wsface.Iback;

public interface ICallback<T> {

    void onSuccess(T t);

    void onFail(String msg);

}
