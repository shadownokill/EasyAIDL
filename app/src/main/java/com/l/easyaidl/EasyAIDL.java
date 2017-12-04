package com.l.easyaidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by zige on 2017/12/4.
 */

public class EasyAIDL {
    private static final String TAG = EasyAIDL.class.getSimpleName();
    public static final int ERROR_NO_THIS_SERVICE = 0, ERROR_SERVICE_UNBIND = 1;

    public interface CallBack {
        void onServiceConnected();

        void onServiceDisconnected(int code, String reson);
    }

    private static EasyAIDL instance;

    public static EasyAIDL getInstance() {
        if (instance == null) {
            synchronized (EasyAIDL.class) {
                if (instance == null) {
                    instance = new EasyAIDL();
                }
            }
        }
        return instance;
    }

    private EasyAIDL() {
    }


    private CallBack callBack;
    private Context context;
    private boolean bind = false;
    private ICommonAidlInterface iCommonAidlInterface;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: ");
            iCommonAidlInterface = ICommonAidlInterface.Stub.asInterface(service);
            try {
                iCommonAidlInterface.asBinder().linkToDeath(deathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            bind = true;
            if (callBack != null) {
                callBack.onServiceConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: ");
            iCommonAidlInterface = null;
            bind = false;
            if (callBack != null) {
                callBack.onServiceDisconnected(ERROR_SERVICE_UNBIND, "远程服务断开连接");
            }
        }
    };

    private IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "binderDied: ");
            bind = false;
            iCommonAidlInterface.asBinder().unlinkToDeath(deathRecipient, 0);
            iCommonAidlInterface = null;
        }
    };

    /**
     * 如果不存在service,则返回false
     *
     * @param context
     * @param service
     * @return
     */
    public void bind(Context context, String service, CallBack callBack) {
        if (bind) {
            Log.d(TAG, "bind: service already binded here");
            return;
        }
        this.context = context;
        this.callBack = callBack;
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(context, service));
        boolean b = context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        if (!b) {
            Log.d(TAG, "bind: no this service : " + service);
            if (callBack != null) {
                callBack.onServiceDisconnected(ERROR_NO_THIS_SERVICE, "没有找到" + service + "这个服务");
            }
            context.unbindService(connection);
        } else {
            Log.d(TAG, "bind: service bind");
        }
    }

    public void unbind() {
        if (!bind) {
            Log.d(TAG, "unbind: service not binded, can't to unbind");
            return;
        }
        context.unbindService(connection);
        Log.d(TAG, "unbind: service unbind");
    }
}
