package top.maxim.im.net;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import im.floo.floolib.BMXNetworkType;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.common.utils.AppContextUtils;

/**
 * 网络连接管理器
 */
public class ConnectivityReceiver extends BroadcastReceiver {

    private static final String TAG = ConnectivityReceiver.class.getSimpleName();
    private static Boolean started = false;

    public static void tryConnect() {
        final ConnectivityManager manager = (ConnectivityManager) AppContextUtils.getAppContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            Log.i(TAG, "ConnectivityManager is null");
            return;
        }
        int tempNetType = getLinkNetType();
        BMXNetworkType networkType = BMXNetworkType.None;
        switch (tempNetType){
            case NetType.NONE:
                networkType = BMXNetworkType.None;
                break;
            case NetType.CABLE:
                networkType = BMXNetworkType.Cable;
                break;
            case NetType.WIFI:
                networkType = BMXNetworkType.Wifi;
                break;
            case NetType.MOBILE:
                networkType = BMXNetworkType.Mobile;
                break;
        }
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (NetworkTool.isConnected(AppContextUtils.getAppContext())) {
                Log.e(TAG, "网络状态切换 进行重连");
                BaseManager.getBMXClient().onNetworkChanged(networkType, true);
            }
        }
    }

    public static void start(Application application) {
        if (!started){
            ConnectivityReceiver mNetReceiver = new ConnectivityReceiver();
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            application.registerReceiver(mNetReceiver, filter);
            started = true;
        }
    }

    public static int getLinkNetType() {
        int tempNetType;
        NetworkTool.NetworkType type = NetworkTool.getNetworkType(AppContextUtils.getAppContext());
        if (NetworkTool.NetworkType.NETWORK_WIFI == type) {
            tempNetType = NetType.WIFI;
        } else if (NetworkTool.NetworkType.NETWORK_NO == type) {
            tempNetType = NetType.NONE;
        } else {
            tempNetType = NetType.MOBILE;
        }
        return tempNetType;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "网络状态切换");
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            tryConnect();
        }
    }

    interface NetType {
        int NONE = 0;
        int CABLE = 1;
        int WIFI = 2;
        int MOBILE = 3;
    }
}
