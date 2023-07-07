
package top.maxim.im.message.utils;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import im.floo.floolib.BMXFileAttachment;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXImageAttachment;
import im.floo.floolib.BMXLocationAttachment;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageConfig;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXUserProfile;
import im.floo.floolib.BMXVoiceAttachment;
import im.floo.floolib.FileProgressListener;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.bean.MessageBean;
import top.maxim.im.common.utils.TaskDispatcher;
import top.maxim.im.common.view.BMImageLoader;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;

/**
 * Description : 聊天工具类 Created by Mango on 2018/11/18.
 */
public class ChatUtils {

    private static ChatUtils mInstance;
    private static final String TAG = "ChatUtils";

    /* view缓存 以view的hash作为key 以cardId作为value */
    private static Map<Integer, Long> mViewCache = Collections
            .synchronizedMap(new HashMap<>());

    private ChatUtils() {
    }

    public static ChatUtils getInstance() {
        if (mInstance == null) {
            synchronized (ChatUtils.class) {
                if (mInstance == null) {
                    mInstance = new ChatUtils();
                }
            }
        }
        return mInstance;
    }

    /**
     * 获取图片的宽高
     *
     * @param imagePath 图片路径
     * @return int[]
     */
    public int[] getImageSize(String imagePath) {
        int[] res = new int[2];

        if (TextUtils.isEmpty(imagePath)) {
            return res;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(imagePath, options);

        int degree = getImageSpinAngle(imagePath);
        if (degree == 90 || degree == 270) {
            // 反转180
            res[0] = options.outHeight;
            res[1] = options.outWidth;
            return res;
        }
        res[0] = options.outWidth;
        res[1] = options.outHeight;

        return res;
    }

    /**
     * 获取图片旋转角度
     * 
     * @param path 路径
     * @return int
     */
    public int getImageSpinAngle(String path) {
        int degree = 0;
        try {
            ExifInterface e = new ExifInterface(path);
            int orientation = e.getAttributeInt("Orientation", 1);
            switch (orientation) {
                case 3:
                    degree = 180;
                    break;
                case 6:
                    degree = 90;
                    break;
                case 8:
                    degree = 270;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 根据消息类型和内容获取描述
     *
     * @param context context
     * @param type message type
     * @param content message content
     * @return String
     */
    private String getMessageDesc(Context context, BMXMessage.ContentType type, String content, boolean isReceiveMsg) {
        if (type == null) {
            return "[" + context.getString(R.string.unknown_message) + "]";
        }
        String desc = "";
        if (type == BMXMessage.ContentType.Text) {
            // 文本
            desc = content.replaceAll("\n.*", "");
        } else if (type == BMXMessage.ContentType.Image) {
            // 图片
            desc = "[" + context.getString(R.string.image) + "]";
        } else if (type == BMXMessage.ContentType.Video) {
            desc = "[" + context.getString(R.string.video) + "]";
        } else if (type == BMXMessage.ContentType.Location) {
            desc = "[" + context.getString(R.string.location) + "]";
        } else if (type == BMXMessage.ContentType.File) {
            desc = "[" + context.getString(R.string.file) + "]";
        } else if (type == BMXMessage.ContentType.Voice) {
            // 语音
            desc = "[" + context.getString(R.string.voice) + "]";
        } else if (type == BMXMessage.ContentType.RTC) {
            if (content.equals("rejected")){
                if (isReceiveMsg){
                    content = context.getString(R.string.call_be_declined);
                } else {
                    content = context.getString(R.string.call_declined);
                }
            } else if (content.equals("canceled")){
                if (isReceiveMsg){
                    content = context.getString(R.string.call_be_canceled);
                } else {
                    content = context.getString(R.string.call_canceled);
                }
            } else if (content.equals("timeout")){
                if (isReceiveMsg){
                    content = context.getString(R.string.call_not_responding);
                } else {
                    content = context.getString(R.string.callee_not_responding);
                }
            } else if (content.equals("busy")){
                if (isReceiveMsg){
                    content = context.getString(R.string.callee_busy);
                } else {
                    content = context.getString(R.string.call_busy);
                }
            } else if(content.length()>0) {
                try {
                    long sec = Long.valueOf(content)/1000;
                    content = String.format("通话时长：%02d:%02d",sec/60, sec%60);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            // 语音
            desc = content;
        }
        return desc;
    }

    /**
     * 根据消息获取描述
     *
     * @param context context
     * @param message message
     * @return String
     */
    public String getMessageDesc(Context context, BMXMessage message) {
        if (message == null) {
            return "";
        }
        BMXMessage.ContentType type = message.contentType();
        String content = message.content();
        return getMessageDesc(context, type, content, message.isReceiveMsg());
    }

    /**
     * 根据消息获取描述
     *
     * @param context context
     * @param message message
     * @return String
     */
    public String getMessageDesc(Context context, MessageBean message) {
        if (message == null) {
            return "";
        }
        BMXMessage.ContentType type = message.getContentType();
        String content = message.getContent();
        return getMessageDesc(context, type, content, message.isReceiveMsg());
    }

    /**
     * 展示头像
     * 
     * @param imageView
     * @param config
     */
    public void showProfileAvatar(BMXUserProfile profile, ShapeImageView imageView,
            ImageRequestConfig config) {
        if (imageView == null) {
            return;
        }
        if (profile == null) {
            BMImageLoader.getInstance().display(imageView, "", config);
            return;
        }
        // 对需要展示的view加入缓存 防止在页面频繁刷新 view复用的时候展示错乱
        String avatarUrl = "";
        if (!TextUtils.isEmpty(profile.avatarThumbnailPath())
                && new File(profile.avatarThumbnailPath()).exists()
                && new File(profile.avatarThumbnailPath()).isFile()) {
            avatarUrl = "file://" + profile.avatarThumbnailPath();
        } else if (!TextUtils.isEmpty(profile.avatarPath())
                && new File(profile.avatarPath()).exists()
                && new File(profile.avatarPath()).isFile()) {
            avatarUrl = "file://" + profile.avatarPath();
        } else {
            downloadProfileAvatar(profile, imageView, config);
        }
        BMImageLoader.getInstance().display(imageView, avatarUrl, config);
    }

    /**
     * 展示头像
     *
     * @param imageView
     * @param config
     */
    public void showRosterAvatar(BMXRosterItem rosterItem, ShapeImageView imageView,
            ImageRequestConfig config) {
        if (imageView == null) {
            return;
        }
        String avatarUrl = "";
        if (rosterItem == null) {
            mViewCache.remove(imageView.hashCode());
            avatarUrl = "drawable://" + R.drawable.bmx_icon;
        } else {
            // 对需要展示的view加入缓存 防止在页面频繁刷新 view复用的时候展示错乱
            // 新增直接展示头像地址 不需下载
            if (!TextUtils.isEmpty(rosterItem.avatarThumbnailUrl())) {
                avatarUrl = rosterItem.avatarThumbnailUrl();
            } else if (!TextUtils.isEmpty(rosterItem.avatarUrl())) {
                avatarUrl = rosterItem.avatarUrl();
            } else if (!TextUtils.isEmpty(rosterItem.avatarThumbnailPath())
                    && new File(rosterItem.avatarThumbnailPath()).exists()
                    && new File(rosterItem.avatarThumbnailPath()).isFile()) {
                avatarUrl = "file://" + rosterItem.avatarThumbnailPath();
            } else if (!TextUtils.isEmpty(rosterItem.avatarPath())
                    && new File(rosterItem.avatarPath()).exists()
                    && new File(rosterItem.avatarPath()).isFile()) {
                avatarUrl = "file://" + rosterItem.avatarPath();
            } else {
                downloadUserAvatar(rosterItem, imageView, config);
            }
        }
        BMImageLoader.getInstance().display(imageView, avatarUrl, config);
    }

    /**
     * 展示头像
     *
     * @param imageView
     * @param config
     */
    public void showGroupAvatar(BMXGroup groupItem, ShapeImageView imageView,
            ImageRequestConfig config) {
        if (imageView == null) {
            return;
        }
        if (groupItem == null) {
            mViewCache.remove(imageView.hashCode());
            BMImageLoader.getInstance().display(imageView, "", config);
            return;
        }
        // 对需要展示的view加入缓存 防止在页面频繁刷新 view复用的时候展示错乱
        String avatarUrl = "";
        // 新增直接展示头像地址 不需下载
        if (!TextUtils.isEmpty(groupItem.avatarThumbnailUrl())) {
            avatarUrl = groupItem.avatarThumbnailUrl();
        } else if (!TextUtils.isEmpty(groupItem.avatarUrl())) {
            avatarUrl = groupItem.avatarUrl();
        } else if (!TextUtils.isEmpty(groupItem.avatarThumbnailPath())
                && new File(groupItem.avatarThumbnailPath()).exists()
                && new File(groupItem.avatarThumbnailPath()).isFile()) {
            avatarUrl = "file://" + groupItem.avatarThumbnailPath();
        } else if (!TextUtils.isEmpty(groupItem.avatarPath())
                && new File(groupItem.avatarPath()).exists()
                && new File(groupItem.avatarPath()).isFile()) {
            avatarUrl = "file://" + groupItem.avatarPath();
        } else {
            downloadGroupAvatar(groupItem, imageView, config);
        }
        BMImageLoader.getInstance().display(imageView, avatarUrl, config);
    }

    /**
     * 下载头像
     */
    private void downloadProfileAvatar(final BMXUserProfile profile, final ShapeImageView imageView,
            final ImageRequestConfig config) {
        if (profile == null || imageView == null) {
            return;
        }
        UserManager.getInstance().downloadAvatar(profile, s -> {
            if (Integer.valueOf(s) >= 100) {
                String avatarUrl = "";
                if (!TextUtils.isEmpty(profile.avatarThumbnailPath())
                        && new File(profile.avatarThumbnailPath()).exists()
                        && new File(profile.avatarThumbnailPath()).isFile()) {
                    avatarUrl = "file://" + profile.avatarThumbnailPath();
                } else if (!TextUtils.isEmpty(profile.avatarPath())
                        && new File(profile.avatarPath()).exists()
                        && new File(profile.avatarPath()).isFile()) {
                    avatarUrl = "file://" + profile.avatarPath();
                }
                String finalAvatarUrl = avatarUrl;
                TaskDispatcher.postMainDelayed(() -> {
                    BMImageLoader.getInstance().display(imageView, finalAvatarUrl, config);
                }, 500);
            }
            Log.i(TAG, "onProgressChange profile:" + profile.userId() + "-" + s);
            return 0;
        }, bmxErrorCode -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                // String avatarUrl = "";
                // // if (!TextUtils.isEmpty(profile.avatarThumbnailPath())
                // // && new File(profile.avatarThumbnailPath()).exists()
                // // && new File(profile.avatarThumbnailPath()).isFile()) {
                // // avatarUrl = + profile.avatarThumbnailPath();
                // // } else
                // if (!TextUtils.isEmpty(profile.avatarPath())
                // && new File(profile.avatarPath()).exists()
                // && new File(profile.avatarPath()).isFile()) {
                // avatarUrl = + profile.avatarPath();
                // }
                // RosterFetcher.getFetcher().putProfile(profile);
                // BMImageLoader.getInstance().display(imageView, avatarUrl, config);
            } else {
                // BMImageLoader.getInstance().display(imageView, "", config);
            }
        });
    }

    /**
     * 下载头像
     */
    public void downloadUserAvatar(final BMXRosterItem item, final ShapeImageView imageView,
            final ImageRequestConfig config) {
        if (item == null || imageView == null) {
            return;
        }
        final int hashCode = imageView.hashCode();
        mViewCache.put(hashCode, item.rosterId());
        RosterManager.getInstance().downloadAvatar(item, new FileProgressListener() {
            @Override
            public int onProgressChange(String percent) {
                if (Integer.valueOf(percent) >= 100) {
                    // 成功
                    if (!mViewCache.containsKey(hashCode)
                            || mViewCache.get(hashCode) != item.rosterId()) {
                        return 0;
                    }
                    mViewCache.remove(hashCode);
                    String avatarUrl = "";
                    if (!TextUtils.isEmpty(item.avatarThumbnailPath())
                            && new File(item.avatarThumbnailPath()).exists()
                            && new File(item.avatarThumbnailPath()).isFile()) {
                        avatarUrl = "file://" + item.avatarThumbnailPath();
                    } else
                    if (!TextUtils.isEmpty(item.avatarPath())
                            && new File(item.avatarPath()).exists()
                            && new File(item.avatarPath()).isFile()) {
                        avatarUrl = "file://" + item.avatarPath();
                    }
                    String finalAvatarUrl = avatarUrl;
                    TaskDispatcher.postMain(() -> {
                        BMImageLoader.getInstance().display(imageView, finalAvatarUrl, config);
                    });
                }
                Log.i(TAG, "onProgressChange roster:" + item.rosterId() + "-" + percent);
                return 0;
            }
        }, bmxErrorCode -> {
            if(BaseManager.bmxFinish(bmxErrorCode)){
                //成功
                // String avatarUrl = "";
                // // if (!TextUtils.isEmpty(item.avatarThumbnailPath())
                // // && new File(item.avatarThumbnailPath()).exists()
                // // && new File(item.avatarThumbnailPath()).isFile()) {
                // // avatarUrl = + item.avatarThumbnailPath();
                // // } else
                // if (!TextUtils.isEmpty(item.avatarPath())
                // && new File(item.avatarPath()).exists()
                // && new File(item.avatarPath()).isFile()) {
                // avatarUrl = + item.avatarPath();
                // }
                // RosterFetcher.getFetcher().putRoster(item);
                // if (!mViewCache.containsKey(hashCode)
                // || mViewCache.get(hashCode) != item.rosterId()) {
                // return;
                // }
                // mViewCache.remove(hashCode);
                // BMImageLoader.getInstance().display(imageView, avatarUrl, config);
//                return;
            }
            //失败
            // if (mViewCache.containsKey(hashCode)
            // && mViewCache.get(hashCode) == item.rosterId()) {
            // mViewCache.remove(hashCode);
            // }
            // BMImageLoader.getInstance().display(imageView, "", config);
        });
    }

    /**
     * 下载头像
     */
    private void downloadGroupAvatar(final BMXGroup item, final ShapeImageView imageView,
            final ImageRequestConfig config) {
        if (item == null || imageView == null) {
            return;
        }
        final int hashCode = imageView.hashCode();
        mViewCache.put(hashCode, item.groupId());
        GroupManager.getInstance().downloadAvatar(item, new FileProgressListener() {
            @Override
            public int onProgressChange(String percent) {
                if (Integer.valueOf(percent) >= 100) {
                    String avatarUrl = "";
                    if (!TextUtils.isEmpty(item.avatarThumbnailPath())
                            && new File(item.avatarThumbnailPath()).exists()
                            && new File(item.avatarThumbnailPath()).isFile()) {
                        avatarUrl = "file://" + item.avatarThumbnailPath();
                    } else if (!TextUtils.isEmpty(item.avatarPath())
                            && new File(item.avatarPath()).exists()
                            && new File(item.avatarPath()).isFile()) {
                        avatarUrl = "file://" + item.avatarPath();
                    }
                    if (!mViewCache.containsKey(hashCode)
                            || mViewCache.get(hashCode) != item.groupId()) {
                        return 0;
                    }
                    mViewCache.remove(hashCode);
                    String finalAvatarUrl = avatarUrl;
                    TaskDispatcher.postMainDelayed(() -> {
                        BMImageLoader.getInstance().display(imageView, finalAvatarUrl, config);
                    }, 500);
                }
                Log.i(TAG, "onProgressChange group:" + item.groupId() + "-" + percent);
                return 0;
            }
        }, bmxErrorCode -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                // String avatarUrl = "";
                // // if (!TextUtils.isEmpty(item.avatarThumbnailPath())
                // // && new File(item.avatarThumbnailPath()).exists()
                // // && new File(item.avatarThumbnailPath()).isFile()) {
                // // avatarUrl = + item.avatarThumbnailPath();
                // // } else
                // if (!TextUtils.isEmpty(item.avatarPath())
                // && new File(item.avatarPath()).exists()
                // && new File(item.avatarPath()).isFile()) {
                // avatarUrl = + item.avatarPath();
                // }
                // RosterFetcher.getFetcher().putGroup(item);
                // if (!mViewCache.containsKey(hashCode)
                // || mViewCache.get(hashCode) != item.groupId()) {
                // return;
                // }
                // mViewCache.remove(hashCode);
                // BMImageLoader.getInstance().display(imageView, avatarUrl, config);
            } else {
                // if (mViewCache.containsKey(hashCode)
                // && mViewCache.get(hashCode) == item.groupId()) {
                // mViewCache.remove(hashCode);
                // }
                // BMImageLoader.getInstance().display(imageView, "", config);
            }
        });
    }

    public MessageBean buildMessage(BMXMessage mBmxMessage, BMXMessage.MessageType type,
            long chatId) {
        if (mBmxMessage == null) {
            return null;
        }
        MessageBean messageBean = new MessageBean();
        BMXMessage.ContentType contentType = mBmxMessage.contentType();
        messageBean.setContentType(contentType);
        messageBean.setType(type);
        messageBean.setChatId(chatId);
        messageBean.setReceiveMsg(mBmxMessage.isReceiveMsg());
        if (contentType == BMXMessage.ContentType.Text) {
            String text = mBmxMessage.content();
            if (!TextUtils.isEmpty(text)) {
                messageBean.setContent(text);
                return messageBean;
            }
            return null;
        }
        if (contentType == BMXMessage.ContentType.Image) {
            // 图片
            BMXImageAttachment body = BMXImageAttachment.dynamic_cast(mBmxMessage.attachment());
            if (body == null || body.size() == null || TextUtils.isEmpty(body.path())) {
                return null;
            }
            int w = (int)body.size().getMWidth();
            int h = (int)body.size().getMHeight();
            messageBean.setPath(body.path());
            messageBean.setW(w);
            messageBean.setH(h);
            return messageBean;
        }
        if (contentType == BMXMessage.ContentType.Voice) {
            // 语音
            BMXVoiceAttachment body = BMXVoiceAttachment.dynamic_cast(mBmxMessage.attachment());
            if (body == null || TextUtils.isEmpty(body.path())) {
                return null;
            }
            messageBean.setPath(body.path());
            messageBean.setDuration(body.duration());
            return messageBean;
        }
        if (contentType == BMXMessage.ContentType.File) {
            // 文件
            BMXFileAttachment body = BMXFileAttachment.dynamic_cast(mBmxMessage.attachment());
            if (body == null || TextUtils.isEmpty(body.path())) {
                return null;
            }
            messageBean.setPath(body.path());
            messageBean.setDisplayName(body.displayName());
            return messageBean;
        }
        if (contentType == BMXMessage.ContentType.Location) {
            // 地图
            BMXLocationAttachment body = BMXLocationAttachment
                    .dynamic_cast(mBmxMessage.attachment());
            if (body == null) {
                return null;
            }
            messageBean.setLatitude(body.latitude());
            messageBean.setLongitude(body.longitude());
            messageBean.setDisplayName(body.address());
            return messageBean;
        }
        return null;
    }


    /**
     * 清除头像缓存
     *
     * @param url 地址
     */
    public void removeAvatarCache(String url) {
        if (!TextUtils.isEmpty(url)) {
            DiskCacheUtils.removeFromCache(url, ImageLoader.getInstance().getDiskCache());
            MemoryCacheUtils.removeFromCache(url, ImageLoader.getInstance().getMemoryCache());
        }
//        ImageLoader.getInstance().clearDiskCache();
//        ImageLoader.getInstance().clearMemoryCache();
    }
}
