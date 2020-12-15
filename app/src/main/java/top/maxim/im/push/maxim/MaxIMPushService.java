
package top.maxim.im.push.maxim;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
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
        Intent intent = new Intent();
        intent.setPackage(context.getPackageName());
        intent.setAction("top.maxim.im.push.maxim.action");
        context.bindService(intent, PushServiceConnection.getInstance().getServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    private static final String TAG = MaxIMPushService.class.getName();

    private BMXPushServiceListener mListener;

    private IPushAidlInterface.Stub mStub = new IPushAidlInterface.Stub() {
        @Override
        public void setPushIdAndToken(String pushId, String token) throws RemoteException {
            Log.e("aaaaaaa____pushId", pushId);
            Log.e("aaaaaaa____token", token);
            startPush(pushId, token, "111");
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mStub;
    }

    @Override
    public void onCreate() {
        super.onCreate();
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

    private void startPush(String pushId, String token, String alias) {
        PushManager.initBMXPushSDK(getApplication(), pushId);
        if (mListener == null) {
            mListener = new BMXPushServiceListener() {

                @Override
                public void onPushStart(String bmxToken) {
                    super.onPushStart(bmxToken);
                    Log.d(TAG, "onPushStart" + bmxToken);
                }

                @Override
                public void onPushStop() {
                    super.onPushStop();
                    Log.d(TAG, "onPushStop");
                }

                @Override
                public void onGetTags(String operationId) {
                    super.onGetTags(operationId);
                    Log.d(TAG, "onGetTags" + operationId);
                }

                @Override
                public void onSetTags(String operationId) {
                    super.onSetTags(operationId);
                    Log.d(TAG, "onSetTags" + operationId);
                }

                @Override
                public void onDeleteTags(String operationId) {
                    super.onDeleteTags(operationId);
                    Log.d(TAG, "onDeleteTags" + operationId);
                }

                @Override
                public void onClearTags(String operationId) {
                    super.onClearTags(operationId);
                    Log.d(TAG, "onClearTags" + operationId);
                }

                @Override
                public void onStatusChanged(BMXMessage msg, BMXErrorCode error) {
                    super.onStatusChanged(msg, error);
                    Log.d(TAG, "onStatusChanged" + msg.content());
                }

                @Override
                public void onReceivePush(BMXMessageList list) {
                    super.onReceivePush(list);
                    Log.d(TAG, "onReceivePush");
                }

                @Override
                public void onCertRetrieved(String cert) {
                    super.onCertRetrieved(cert);
                    PushManager.getInstance().setCert(cert);
                    Log.d(TAG, "onCertRetrieved" + cert);
                }
            };
        }
        PushManager.getInstance().addPushListener(mListener);
        PushManager.getInstance().start(alias, "", new BMXCallBack() {
            @Override
            public void onResult(BMXErrorCode bmxErrorCode) {
                Log.e(TAG, "start service");
                if (!BaseManager.bmxFinish(bmxErrorCode)) {
                    Log.e(TAG, "start failed");
                }
                //绑定pushToken
                PushManager.getInstance().bindDeviceToken(token, new BMXCallBack() {
                    @Override
                    public void onResult(BMXErrorCode bmxErrorCode) {
                        if (!BaseManager.bmxFinish(bmxErrorCode)) {
                            Log.e("bindDevice failed", bmxErrorCode.name());
                        }
                    }
                });
            }
        });
    }
}
