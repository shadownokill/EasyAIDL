package com.l.easyaidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public abstract class EasyAIDLService extends Service {
    public EasyAIDLService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    public abstract void action(String action);


    private ICommonAidlInterface.Stub stub = new ICommonAidlInterface.Stub() {
        @Override
        public void action(String action) throws RemoteException {
            action(action);
        }
    };
}
