package com.crazysunj.websocket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FacePushReceiver extends BroadcastReceiver {
    /**
     * 人脸push消息接收
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String content = intent.getStringExtra("push_content");
        ToastUtil.showToast(context, content);
    }
}
