package com.crazysunj.websocket;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class WsManager {
    private static WsManager mInstance;
    private final String TAG = this.getClass().getSimpleName();
    private static final long HEARTBEAT_INTERVAL = 30000;//心跳间隔
    /**
     * WebSocket config
     */
    private static final int FRAME_QUEUE_SIZE = 5;
    private static final int CONNECT_TIMEOUT = 5000;
    private static final String DEF_TEST_URL = "ws://test.sraum.com/WebSocketTest/websocket";//测试服默认地址
    private static final String DEF_RELEASE_URL = "ws://masskyface.massky.com:22765";//正式服默认地址-ws://192.168.169.220:8080/Mysql/myHandler
    private static final String DEF_URL = BuildConfig.DEBUG ? DEF_TEST_URL : DEF_RELEASE_URL;
    private String url;

    private WsStatus mStatus;
    private WebSocket ws;
    private WsListener mListener;
    private FaceControlResponseListener faceControlResponseListener;

    private WsManager() {

    }

    public static WsManager getInstance() {
        if (mInstance == null) {
            synchronized (WsManager.class) {
                if (mInstance == null) {
                    mInstance = new WsManager();
                }
            }
        }
        return mInstance;
    }

    public void init() {
        try {
            /**
             * configUrl其实是缓存在本地的连接地址
             * 这个缓存本地连接地址是app启动的时候通过http请求去服务端获取的,
             * 每次app启动的时候会拿当前时间与缓存时间比较,超过6小时就再次去服务端获取新的连接地址更新本地缓存
             */
            String configUrl = "";
            url = TextUtils.isEmpty(configUrl) ? DEF_URL : configUrl;
            ws = new WebSocketFactory().createSocket(url, CONNECT_TIMEOUT)
                    .setFrameQueueSize(FRAME_QUEUE_SIZE)//设置帧队列最大值为5
                    .setMissingCloseFrameAllowed(false)//设置不允许服务端关闭连接却未发送关闭帧
                    .addListener(mListener = new WsListener())//添加回调监听
                    .connectAsynchronously();//异步连接
            setStatus(WsStatus.CONNECTING);
//            Logger.t(TAG).d("第一次连接");
            Log.e("robin debug", "第一次连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 继承默认的监听空实现WebSocketAdapter,重写我们需要的方法
     * onTextMessage 收到文字信息
     * onConnected 连接成功
     * onConnectError 连接失败
     * onDisconnected 连接关闭
     */
    class WsListener extends WebSocketAdapter {
        @Override
        public void onTextMessage(WebSocket websocket, String text) throws Exception {
            super.onTextMessage(websocket, text);
//            Logger.t(TAG).d(text);
            Log.e("robin debug", text);
            Response response = Codec.decoder(text);//解析出第一层bean
            for (Map.Entry<Long, CallbackWrapper> vo : callbacks.entrySet()) {
                CallbackWrapper value = vo.getValue();
                if (value.getAction().toString().toLowerCase().equals(response.getCmd())) {
                    CallbackWrapper wrapper = callbacks.remove(Long.parseLong(String.valueOf(vo.getKey())));//找到对应callback
                    if (wrapper == null) {
                        return;
                    }
                    wrapper.getTimeoutTask().cancel(true);//取消超时任务
                    wrapper.getTempCallback().onSuccess(text);
                    break;
                }
            }

            //消息分发
            NotifyListenerManager.getInstance().push(text);
        }

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
                throws Exception {
            super.onConnected(websocket, headers);
//            Logger.t(TAG).d("连接成功");
            Log.e("robin debug", "连接成功");
            setStatus(WsStatus.CONNECT_SUCCESS);
//            ws.sendText("你好");
        }


        @Override
        public void onConnectError(WebSocket websocket, WebSocketException exception)
                throws Exception {
            super.onConnectError(websocket, exception);
//            Logger.t(TAG).d("连接错误");
            Log.e("robin debug", "连接错误");
            setStatus(WsStatus.CONNECT_FAIL);
            reconnect();
        }


        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer)
                throws Exception {
            super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
//            Logger.t(TAG).d("断开连接");
            Log.e("robin debug", "断开连接");
            setStatus(WsStatus.CONNECT_FAIL);
            reconnect();
        }
    }

    private void setStatus(WsStatus status) {
        this.mStatus = status;
    }

    private WsStatus getStatus() {
        return mStatus;
    }

    public void disconnect() {
        if (ws != null)
            ws.disconnect();
    }

    public enum WsStatus {
        CONNECT_SUCCESS,//连接成功
        CONNECT_FAIL,//连接失败
        CONNECTING,//正在连接
        AUTH_SUCCESS;//授权成功
    }


//    private Handler mHandler = new Handler();

    private int reconnectCount = 0;//重连次数
    private long minInterval = 3000;//重连最小时间间隔
    private long maxInterval = 60000;//重连最大时间间隔


    private Runnable mReconnectTask = new Runnable() {

        @Override
        public void run() {
            try {
                ws = new WebSocketFactory().createSocket(url, CONNECT_TIMEOUT)
                        .setFrameQueueSize(FRAME_QUEUE_SIZE)//设置帧队列最大值为5
                        .setMissingCloseFrameAllowed(false)//设置不允许服务端关闭连接却未发送关闭帧
                        .addListener(mListener = new WsListener())//添加回调监听
                        .connectAsynchronously();//异步连接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * 取消在次连接
     */
    private void cancelReconnect() {
        reconnectCount = 0;
        mHandler.removeCallbacks(mReconnectTask);
    }


    private boolean isNetConnect() {
        ConnectivityManager connectivity = (ConnectivityManager) WsApplication.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }


    private static final int REQUEST_TIMEOUT = 10000;//请求超时时间
    private AtomicLong seqId = new AtomicLong(SystemClock.uptimeMillis());//每个请求的唯一标识

    public void sendReq(Action action, String content, ICallback callback) {
        sendReq(action, content, callback, REQUEST_TIMEOUT);
    }


    public void sendReq(Action action, String content, ICallback callback, long timeout) {
        sendReq(action, content, callback, timeout, 1);
    }


    private final int SUCCESS_HANDLE = 0x01;
    private final int ERROR_HANDLE = 0x02;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS_HANDLE:
                    CallbackDataWrapper successWrapper = (CallbackDataWrapper) msg.obj;
                    successWrapper.getCallback().onSuccess(successWrapper.getData());
                    break;
                case ERROR_HANDLE:
                    CallbackDataWrapper errorWrapper = (CallbackDataWrapper) msg.obj;
                    errorWrapper.getCallback().onFail((String) errorWrapper.getData());
                    break;
            }
        }
    };

    /**
     * 超时处理
     */
    private void timeoutHandle(Request request, String content, Action action, ICallback callback, long timeout) {
        if (request.getReqCount() > 3) {
//            Logger.t(TAG).d("(action:%s)连续3次请求超时 执行http请求", action.getAction());
            Log.e("robin debug", "(action:%s)连续3次请求超时 执行http请求" + action.getAction());
            //走http请求
        } else {
            sendReq(action, content, callback, timeout, request.getReqCount() + 1);
//            Logger.t(TAG).d("(action:%s)发起第%d次请求", action.getAction(), request.getReqCount());
            Log.e("robin debug", "(action:%s)发起第%d次请求" + action.getAction());
        }
    }


    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private Map<Long, CallbackWrapper> callbacks = new HashMap<>();

    /**
     * 发送
     *
     * @param action
     * @param callback
     * @param timeout
     * @param reqCount
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    private <T> void sendReq(Action action, final String content, final ICallback callback, final long timeout, int reqCount) {
        if (!isNetConnect()) {
            callback.onFail("网络不可用");
            return;
        }

        Request request = new Request.Builder<T>()
                .action(action.getAction())
                .seqId(seqId.getAndIncrement())
                .reqCount(reqCount)
                .build();

        ScheduledFuture timeoutTask = enqueueTimeout(request.getSeqId(), timeout);//添加超时任务

        IWsCallback tempCallback = new IWsCallback() {

            @Override
            public void onSuccess(Object o) {
                mHandler.obtainMessage(SUCCESS_HANDLE, new CallbackDataWrapper(callback, o))
                        .sendToTarget();
            }


            @Override
            public void onError(String msg, Request request, Action action) {
                mHandler.obtainMessage(ERROR_HANDLE, new CallbackDataWrapper(callback, msg))
                        .sendToTarget();
            }


            @Override
            public void onTimeout(Request request, Action action) {
                timeoutHandle(request, content, action, callback, timeout);
            }
        };

        callbacks.put(request.getSeqId(),
                new CallbackWrapper(tempCallback, timeoutTask, action, request));

//        Logger.t(TAG).d("send text : %s", new Gson().toJson(request));
        Log.e("robin debug", "send text : %s" + new Gson().toJson(request));
        ws.sendText(new Gson().toJson(content));

    }

    /**
     * 添加超时任务
     */
    private ScheduledFuture enqueueTimeout(final long seqId, long timeout) {
        return executor.schedule(new Runnable() {
            @Override
            public void run() {
                CallbackWrapper wrapper = callbacks.remove(seqId);
                if (wrapper != null) {
//                    Logger.t(TAG).d("(action:%s)第%d次请求超时", wrapper.getAction().getAction(), wrapper.getRequest().getReqCount());
                    wrapper.getTempCallback().onTimeout(wrapper.getRequest(), wrapper.getAction());
                }
            }
        }, timeout, TimeUnit.MILLISECONDS);
    }


    /**
     * 发送方
     *  @param action
     * @param content
     * @param faceControlResponseListener
     */
    public void send(Action action, String content, final FaceControlResponseListener faceControlResponseListener) {
        this.faceControlResponseListener = faceControlResponseListener;
        sendReq(action, content, new ICallback() {
            @Override
            public void onSuccess(Object o) {
                setStatus(WsStatus.AUTH_SUCCESS);
                startHeartbeat();
                if (faceControlResponseListener != null) {
                    faceControlResponseListener.face_response((String) o);
                }
            }


            @Override
            public void onFail(String msg) {

            }
        });
    }


    /**
     * 开始心跳
     */
    private void startHeartbeat() {
        mHandler.postDelayed(heartbeatTask, HEARTBEAT_INTERVAL);
    }


    /**
     * 取消心跳
     */
    private void cancelHeartbeat() {
        heartbeatFailCount = 0;
        mHandler.removeCallbacks(heartbeatTask);
    }


    private int heartbeatFailCount = 0;
    private Runnable heartbeatTask = new Runnable() {
        @Override
        public void run() {
            sendReq(Action.BEAT, "", new ICallback() {
                @Override
                public void onSuccess(Object o) {
                    heartbeatFailCount = 0;
                }


                @Override
                public void onFail(String msg) {
                    heartbeatFailCount++;
                    if (heartbeatFailCount >= 3) {
                        reconnect();
                    }
                }
            });

            mHandler.postDelayed(this, HEARTBEAT_INTERVAL);
        }
    };


    public void reconnect() {
        if (!isNetConnect()) {
            reconnectCount = 0;
//            Logger.t(TAG).d("重连失败网络不可用");
            Log.e("robin debug", "重连失败网络不可用");
            return;
        }

        //这里其实应该还有个用户是否登录了的判断 因为当连接成功后我们需要发送用户信息到服务端进行校验
        //由于我们这里是个demo所以省略了
        if (ws != null &&
                !ws.isOpen() &&//当前连接断开了
                getStatus() != WsStatus.CONNECTING) {//不是正在重连状态

            reconnectCount++;
            setStatus(WsStatus.CONNECTING);
            cancelHeartbeat();//取消心跳

            long reconnectTime = minInterval;
            if (reconnectCount > 3) {
                url = DEF_URL;
                long temp = minInterval * (reconnectCount - 2);
                reconnectTime = temp > maxInterval ? maxInterval : temp;
            }

//            Logger.t(TAG).d("准备开始第%d次重连,重连间隔%d -- url:%s", reconnectCount, reconnectTime, url);
            Log.e("robin debug", "准备开始第%d次重连,重连间隔%d -- url:%s" + reconnectCount);
            mHandler.postDelayed(mReconnectTask, reconnectTime);
        }
    }


    public interface FaceControlResponseListener {
        void face_response(String content);
    }

}
