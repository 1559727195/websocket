package com.massky.wsface.Iback;

import com.massky.wsface.response.Action;
import com.massky.wsface.response.Request;

public interface IWsCallback<T> {
    void onSuccess(T t);
    void onError(String msg, Request request, Action action);
    void onTimeout(Request request, Action action);
}
