
package top.maxim.im.push;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import im.floo.BMXCallBack;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageList;
import im.floo.floolib.BMXPushServiceListener;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.PushManager;

/**
 * Description : push服务 Created by mango on 2020/9/10.
 */
public class MaxIMPushService extends Service {

    public static void startPushService(Context context) {
        Intent intent = new Intent(context, MaxIMPushService.class);
        context.startService(intent);
    }
    
    private static final String TAG = MaxIMPushService.class.getName();

    private BMXPushServiceListener mListener = new BMXPushServiceListener() {

        @Override
        public void onPushStart(String bmxToken) {
            super.onPushStart(bmxToken);
            Log.d(TAG, "onPushStart");
        }

        @Override
        public void onPushStop() {
            super.onPushStop();
            Log.d(TAG, "onPushStop");
        }

        @Override
        public void onGetTags(String operationId) {
            super.onGetTags(operationId);
            Log.d(TAG, "onGetTags");
        }

        @Override
        public void onSetTags(String operationId) {
            super.onSetTags(operationId);
            Log.d(TAG, "onSetTags");
        }

        @Override
        public void onDeleteTags(String operationId) {
            super.onDeleteTags(operationId);
            Log.d(TAG, "onDeleteTags");
        }

        @Override
        public void onClearTags(String operationId) {
            super.onClearTags(operationId);
            Log.d(TAG, "onClearTags");
        }

        @Override
        public void onStatusChanged(BMXMessage msg, BMXErrorCode error) {
            super.onStatusChanged(msg, error);
            Log.d(TAG, "onStatusChanged");
        }

        @Override
        public void onReceivePush(BMXMessageList list) {
            super.onReceivePush(list);
            Log.d(TAG, "onReceivePush");
        }

        @Override
        public void onCertRetrieved(String cert) {
            super.onCertRetrieved(cert);
            Log.d(TAG, "onCertRetrieved");
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PushManager.getInstance().addPushListener(mListener);
        PushManager.getInstance().getToken();
        PushManager.getInstance().getCert();
        PushManager.getInstance().start(new BMXCallBack() {
            @Override
            public void onResult(BMXErrorCode bmxErrorCode) {
                Log.e(TAG, "start service");
                if (!BaseManager.bmxFinish(bmxErrorCode)) {
                    Log.e(TAG, "start failed");
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            PushManager.getInstance().removePushListener(mListener);
            mListener = null;
        }
    }
}
