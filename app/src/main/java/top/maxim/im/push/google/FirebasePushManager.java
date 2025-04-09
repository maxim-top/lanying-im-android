
package top.maxim.im.push.google;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import top.maxim.im.push.IPushManager;
import top.maxim.im.push.PushClientMgr;

/**
 * Description : Googlepush
 */
public class FirebasePushManager extends IPushManager {

    private static final String TAG = "FirebasePushManager";

    public FirebasePushManager(Context context) {
//        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
    }

    @Override
    public void register(Context context) {
        FirebaseApp.initializeApp(context);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    return;
                }
                // Get new FCM registration token
                String token = task.getResult();
                if (!TextUtils.isEmpty(token)) {
                    PushClientMgr.setPushToken(token);
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "get Token is failed" + e.getMessage());
            }
        });
    }

    @Override
    public void unRegister() {
        FirebaseMessaging.getInstance().deleteToken();
    }

    @Override
    public void startReceiveNotification() {
    }

    @Override
    public void stopReceiveNotification() {
    }
}
