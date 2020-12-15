
package top.maxim.im.push.maxim;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class PushServiceConnection {

    private static final String TAG = PushServiceConnection.class.getSimpleName();

    private static final PushServiceConnection sInstance = new PushServiceConnection();

    private ServiceConnection mServiceConnection;

    private IPushAidlInterface mPushAidlInterface;

    public static PushServiceConnection getInstance() {
        return sInstance;
    }

    private PushServiceConnection() {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mPushAidlInterface = IPushAidlInterface.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    public ServiceConnection getServiceConnection() {
        return mServiceConnection;
    }

    public void setPushIdAndToken(String pushId, String token) {
        try {
            if (mPushAidlInterface != null) {
                mPushAidlInterface.setPushIdAndToken(pushId, token);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
