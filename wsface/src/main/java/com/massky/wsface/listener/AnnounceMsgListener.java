package com.massky.wsface.listener;


import android.content.Context;
import android.content.Intent;
import com.massky.wsface.Iback.INotifyListener;
import com.massky.wsface.service.WsManagerService;


public class AnnounceMsgListener implements INotifyListener<String> {


    //接收到的消息
    @Override
    public void fire(String content, Context context) {
        Intent intent = new Intent();
        intent.putExtra("push_content", content);
        intent.setAction(WsManagerService.COM_MASSKY_FACE_PUSH);
        if(context != null)
        context.sendBroadcast(intent);
    }
}
