package com.crazysunj.websocket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.gson.Gson;
import com.massky.wsface.service.WsManagerService;
import com.massky.wsface.util.SerialNumberUtil;
import com.massky.wsface.util.ToastUtil;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, WsManagerService.ConnectFaceWsLisenter {//

    /**
     * 服务器推送到设备需用FacePushReceiver自行过滤
     */
    private Button btn_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String serial_number = SerialNumberUtil.getSerialNumber();
        WsManagerService.getInstance().set_device_serial_number(serial_number);
        WsManagerService.getInstance().setUrl("ws://masskyface.massky.com/Face/myHandler");//
        WsManagerService.getInstance().init(this, WsApplication.getInstance());//建立连接

        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WsManagerService.getInstance().disconnect();//断开连接
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                Map map = new HashMap();
                map.put("cmd", "validate");
                map.put("type", "web");
                map.put("loginAccount", "39937093");
                //示例代码，仅供参考，
                WsManagerService.getInstance().send(new Gson().toJson(map));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        WsManagerService.getInstance().onresume();//唤醒必须调
    }


    /**
     * 发送数据之前，判断这个值是否为true,true为连接成功
     * @param isconnect
     */
    @Override
    public void connected(boolean isconnect) {
        if (isconnect) {//连接成功(校验成功)
            ToastUtil.showToast(MainActivity.this,"连接成功");
        } else {//连接失败
            ToastUtil.showToast(MainActivity.this,"连接失败");
        }
    }
}
