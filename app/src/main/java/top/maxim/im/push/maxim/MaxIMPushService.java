
package top.maxim.im.push.maxim;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.os.Handler;

import androidx.annotation.Nullable;

import im.floo.BMXCallBack;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageList;
import im.floo.floolib.BMXPushServiceListener;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.PushManager;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.push.NotificationUtils;
import top.maxim.im.push.PushClientMgr;
import top.maxim.im.sdk.utils.MsgConstants;

/**
 * Description : push服务 Created by mango on 2020/9/10.
 */
public class MaxIMPushService extends Service {

    private Handler handler;
    private Runnable runnable;

    public static void startPushService(Context context) {
        try {
            Intent intent = new Intent(context, MaxIMPushService.class);
            context.startService(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static final String TAG = MaxIMPushService.class.getName();

    private BMXPushServiceListener mListener = new BMXPushServiceListener() {

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
            //弹出Notification
            if (list != null && !list.isEmpty()) {
                BMXMessage message = list.get(0);
                if (message != null) {
                    String title = !TextUtils.isEmpty(message.senderName()) ? message.senderName() : getString(R.string.app_name);
                    String content = ChatUtils.getInstance().getMessageDesc(getApplicationContext(), message);
                    Intent intent = getPackageManager().getLaunchIntentForPackage(getApplication().getPackageName());
                    NotificationUtils.getInstance().showNotify(MsgConstants.ChannelImportance.PRIVATE, title, content, intent);
                }
            }
        }

        @Override
        public void onCertRetrieved(String cert) {
            super.onCertRetrieved(cert);
            Log.d(TAG, "onCertRetrieved:" + cert);
            PushClientMgr.getManager().register(MaxIMPushService.this);
            int delay = 5000;//ms
            handler = new Handler(Looper.getMainLooper());
            runnable = new Runnable() {
                @Override
                public void run() {
                    new Thread(()->{
                        String token = SharePreferenceUtils.getInstance().getPushToken();
                        if (TextUtils.isEmpty(token)){
                            PushClientMgr.getManager().register(MaxIMPushService.this);
                            handler.postDelayed(this, delay);
                        }
                    }).start();
                }
            };
            handler.postDelayed(runnable, delay);
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
        startPush("", "");
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

    private void startPush(String alias, String token) {
        //创建channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.getInstance().createChannelId();
        }
        PushManager.getInstance().addPushListener(mListener);
        PushManager.getInstance().start(alias, new BMXCallBack() {
            @Override
            public void onResult(BMXErrorCode bmxErrorCode) {
                Log.e(TAG, "start service");
                if (!BaseManager.bmxFinish(bmxErrorCode)) {
                    Log.e(TAG, "start failed");
                }
            }
        });
    }
}
