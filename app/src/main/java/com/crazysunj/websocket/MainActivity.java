package com.crazysunj.websocket;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_send;
    private NetStatusReceiver netWorkStateReceiver;
    public static final String COM_MASSKY_FACE_PUSH = "com.massky.face.push.receiver";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        regist_receiver();
        WsManager.getInstance().init();//建立连接
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WsManager.getInstance().disconnect();//断开连接
        unregisterReceiver(netWorkStateReceiver);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                Map map = new HashMap();
                map.put("cmd","validate");
                map.put("type","web");
                map.put("loginAccount","39937093");
                WsManager.getInstance().send(Action.VALIDATE,new Gson().toJson(map),new WsManager.FaceControlResponseListener(){
                    //设备-服务器-》控制接口，

                    @Override
                    public void face_response(String content) {
                        //"{\"loginAccount\":\"39937093\",\"cmd\":\"validate\",\"type\":\"web\"}"

                    }
                });
                break;
        }
    }

    @Override
    protected void onResume() {
//        LogUtils.e("注册");
        super.onResume();
    }

    private void regist_receiver() {
        if (netWorkStateReceiver == null) {
            netWorkStateReceiver = new NetStatusReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkStateReceiver, filter);
    }
}
