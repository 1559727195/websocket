package com.crazysunj.websocket;

import android.app.Application;
import android.util.Log;

import com.massky.wsface.Iback.ForegroundCallbacks;
import com.massky.wsface.service.WsManagerService;


public class WsApplication extends Application {
    private static WsApplication _instance;
    @Override
    public void onCreate() {
        super.onCreate();
        initAppStatusListener();
        _instance = this;
    }

    public static WsApplication getInstance(){
        return _instance;
    }

    private void initAppStatusListener() {
        ForegroundCallbacks.init(this).addListener(new ForegroundCallbacks.Listener() {
            @Override
            public void onBecameForeground() {
//                Logger.t("WsManagerService").d("应用回到前台调用重连方法");
                Log.e("robin debug","应用回到前台调用重连方法");
                WsManagerService.getInstance().reconnect(_instance);
            }

            @Override
            public void onBecameBackground() {

            }
        });
    }
}
