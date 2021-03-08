
package top.maxim.im.push;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import top.maxim.im.R;
import top.maxim.im.bmxmanager.ChatManager;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.sdk.utils.MsgConstants;

/**
 * Description : notification Created by Mango on 2019/06/13.
 */
public class NotificationUtils {

    private static NotificationUtils instance = new NotificationUtils();

    private NotificationManager manager;

    private int mNotifyId = 0;

    private NotificationUtils() {
        manager = (NotificationManager)AppContextUtils.getAppContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static NotificationUtils getInstance() {
        return instance;
    }

    /**
     * 展示notification
     *
     * @param title 标题
     * @param content 内容
     * @param it 跳转
     */
    public int showNotify(int importance, String title, String content, Intent it) {
        showNotify(importance, title, content, it, mNotifyId, 1);
        return mNotifyId++;
    }

    /**
     * 展示notification
     *
     * @param title 标题
     * @param content 内容
     * @param it 跳转
     * @param notifyId 唯一Id
     */
    public void showNotify(int importance, String title, String content, Intent it, int notifyId,
            int count) {
        showNotify(importance, title, content, it, notifyId, null, count);
    }

    /**
     * 展示notification
     *
     * @param title 标题
     * @param content 内容
     * @param it 跳转
     * @param notifyId 唯一Id
     */
    private void showNotify(int importance, String title, String content, Intent it, int notifyId,
            Bitmap bitmap, int count) {
        long when = System.currentTimeMillis();
        String channelId = String.format(
                AppContextUtils.getAppContext().getString(R.string.channel_id),
                AppContextUtils.getPackageName(AppContextUtils.getAppContext()), notifyId);
        // 这里必须设置chanenelId,要不然该通知在8.0手机上，不能正常显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelId(importance, channelId);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                AppContextUtils.getAppContext(), channelId).setWhen(when).setAutoCancel(true)
                        .setContentTitle(title).setContentText(content)
                        .setSmallIcon(R.drawable.bmx_icon144).setTicker(content);
        if (bitmap == null) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            opt.inPurgeable = true;
            opt.inInputShareable = true;
            opt.inJustDecodeBounds = false;
            opt.inSampleSize = 1;// 设置缩放比例
            bitmap = BitmapFactory.decodeResource(AppContextUtils.getAppContext().getResources(),
                    R.drawable.bmx_icon144, opt);
        }
        builder.setLargeIcon(bitmap);
        PendingIntent pIntent = PendingIntent.getActivity(AppContextUtils.getAppContext(), notifyId,
                it, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pIntent);
        Notification notification = builder.build();
        manager.notify(notifyId, notification);
        getAllCount(notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannelId(int importance, String channelId) {
        NotificationChannel channel;
        switch (importance) {
            case MsgConstants.ChannelImportance.PRIVATE:
                channel = new NotificationChannel(channelId,
                        MsgConstants.NOTIFICATION_CHANNEL_PRIVATE,
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(MsgConstants.NOTIFICATION_CHANNEL_PRIVATE);
                channel.canShowBadge();
                channel.enableLights(true);
                channel.setShowBadge(true);
                channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
                break;
            case MsgConstants.ChannelImportance.TOPIC:
                channel = new NotificationChannel(channelId,
                        MsgConstants.NOTIFICATION_CHANNEL_TOPIC,
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(MsgConstants.NOTIFICATION_CHANNEL_TOPIC);
                channel.canShowBadge();
                channel.enableLights(true);
                channel.setShowBadge(true);
                channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_SECRET);
                break;
            case MsgConstants.ChannelImportance.PUBLIC:
            default:
                channel = new NotificationChannel(channelId,
                        MsgConstants.NOTIFICATION_CHANNEL_PUBLIC,
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(MsgConstants.NOTIFICATION_CHANNEL_PUBLIC);
                break;
        }
        channel.setSound(null, null);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * 取消notification
     *
     * @param notifyId 唯一Id
     */
    public void cancel(int notifyId) {
        manager.cancel(notifyId);
    }

    /**
     * 取消所有notification
     */
    public void cancelAll() {
        manager.cancelAll();
    }

    private void getAllCount(Notification notification) {
        ChatManager.getInstance().getAllConversationsUnreadCount((bmxErrorCode, integer) -> {
            setCorner(notification, integer == null ? 0 : integer);
        });
    }
    
    public void setCorner(Notification notification, int count) {
        setHuaWeiCorner(count);
        setXiaomiCount(notification, count);
    }

    /**
     * 设置华为手机角标
     *
     * @param count
     */
    private void setHuaWeiCorner(int count) {
        if (android.os.Build.BRAND.toLowerCase().contains("huawei")
                || android.os.Build.BRAND.toLowerCase().contains("honor")) {
            try {
                Bundle bunlde = new Bundle();
                bunlde.putString("package", AppContextUtils.getAppContext().getPackageName());
                String launchClassName = AppContextUtils.getAppContext().getPackageManager()
                        .getLaunchIntentForPackage(AppContextUtils.getAppContext().getPackageName())
                        .getComponent().getClassName();
                bunlde.putString("class", launchClassName);
                bunlde.putInt("badgenumber", count);
                AppContextUtils.getAppContext().getContentResolver().call(
                        Uri.parse("content://com.huawei.android.launcher.settings/badge/"),
                        "change_badge", null, bunlde);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setXiaomiCount(Notification notification, int count) {
        try {
            if (notification != null) {
                Field field = notification.getClass().getDeclaredField("extraNotification");
                Object extraNotification = field.get(notification);
                Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount",
                        int.class);
                method.invoke(extraNotification, count);
            }
            // 设置小米 vivo 三星 角标
            Intent intent = new Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM");
            intent.putExtra("packageName", AppContextUtils.getAppContext().getPackageName());
            intent.putExtra("notificationNum", count);
            AppContextUtils.getAppContext().sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
