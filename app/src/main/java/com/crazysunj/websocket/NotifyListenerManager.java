package com.crazysunj.websocket;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;


public class NotifyListenerManager {
    private volatile static NotifyListenerManager manager;
    private Map<String, INotifyListener> map = new HashMap<>();

    private NotifyListenerManager() {
        regist();
    }

    public static NotifyListenerManager getInstance() {
        if (manager == null) {
            synchronized (NotifyListenerManager.class) {
                if (manager == null) {
                    manager = new NotifyListenerManager();
                }
            }
        }
        return manager;
    }

    private void regist() {
        map.put("notifyAnnounceMsg", new AnnounceMsgListener());
    }

    /**
     * 后台推送
     * @param content
     */
    public void push(String content) {
        INotifyListener listener = map.get("notifyAnnounceMsg");
        if (listener == null) {
//            Logger.t(TAG).d("no found notify listener");
            Log.e("robin debug", "no found notify listener");
            return;
        }
        listener.fire(content);
    }
}
