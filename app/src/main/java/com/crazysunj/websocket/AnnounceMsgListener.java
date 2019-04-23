package com.crazysunj.websocket;


import android.content.Intent;

import static com.crazysunj.websocket.MainActivity.COM_MASSKY_FACE_PUSH;

public class AnnounceMsgListener implements INotifyListener<String> {


    //系统分发，系统通知，需要拦截，这里包含控制后，接收到的消息；
    @Override
    public void fire(String content) {
        Intent intent = new Intent();
        intent.putExtra("push_content", content);
        intent.setAction(COM_MASSKY_FACE_PUSH);
        WsApplication.getInstance().sendBroadcast(intent);
    }
}
