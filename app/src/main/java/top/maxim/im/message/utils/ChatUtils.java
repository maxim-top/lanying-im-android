
package top.maxim.im.message.utils;

import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXFileAttachment;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXImageAttachment;
import im.floo.floolib.BMXLocationAttachment;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXUserProfile;
import im.floo.floolib.BMXVoiceAttachment;
import im.floo.floolib.FileProgressListener;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.bean.MessageBean;
import top.maxim.im.common.view.BMImageLoader;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.message.interfaces.FileCallback;

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
     * 根据消息获取描述
     * 
     * @param message message
     * @return String
     */
    public String getMessageDesc(BMXMessage message) {
        if (message == null) {
            return "";
        }
        BMXMessage.ContentType type = message.contentType();
        if (type == null) {
            return "[未知消息]";
        }
        String desc = "";
        if (type == BMXMessage.ContentType.Text) {
            // 文本
            desc = message.content();
        } else if (type == BMXMessage.ContentType.Image) {
            // 图片
            desc = "[图片]";
        } else if (type == BMXMessage.ContentType.Video) {
            desc = "[视频]";
        } else if (type == BMXMessage.ContentType.Location) {
            desc = "[位置]";
        } else if (type == BMXMessage.ContentType.File) {
            desc = "[文件]";
        } else if (type == BMXMessage.ContentType.Voice) {
            // 语音
            desc = "[语音]";
        }
        return desc;
    }

    /**
     * 根据消息获取描述
     *
     * @param message message
     * @return String
     */
    public String getMessageDesc(MessageBean message) {
        if (message == null) {
            return "";
        }
        BMXMessage.ContentType type = message.getContentType();
        if (type == null) {
            return "[未知消息]";
        }
        String desc = "";
        if (type == BMXMessage.ContentType.Text) {
            // 文本
            desc = message.getContent();
        } else if (type == BMXMessage.ContentType.Image) {
            // 图片
            desc = "[图片]";
        } else if (type == BMXMessage.ContentType.Video) {
            desc = "[视频]";
        } else if (type == BMXMessage.ContentType.Location) {
            desc = "[位置]";
        } else if (type == BMXMessage.ContentType.File) {
            desc = "[文件]";
        } else if (type == BMXMessage.ContentType.Voice) {
            // 语音
            desc = "[语音]";
        }
        return desc;
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
        if (rosterItem == null) {
            mViewCache.remove(imageView.hashCode());
            BMImageLoader.getInstance().display(imageView, "", config);
            return;
        }
        // 对需要展示的view加入缓存 防止在页面频繁刷新 view复用的时候展示错乱
        String avatarUrl = "";
        if (!TextUtils.isEmpty(rosterItem.avatarThumbnailPath())
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
        if (!TextUtils.isEmpty(groupItem.avatarThumbnailPath())
                && new File(groupItem.avatarThumbnailPath()).exists()
                && new File(groupItem.avatarThumbnailPath()).isFile()) {
            avatarUrl = groupItem.avatarThumbnailPath();
            BMImageLoader.getInstance().display(imageView, avatarUrl, config);
        } else if (!TextUtils.isEmpty(groupItem.avatarPath())
                && new File(groupItem.avatarPath()).exists()
                && new File(groupItem.avatarPath()).isFile()) {
            avatarUrl = groupItem.avatarPath();
            BMImageLoader.getInstance().display(imageView, avatarUrl, config);
        } else {
            BMImageLoader.getInstance().display(imageView, "", config);
            downloadGroupAvatar(groupItem, imageView, config);
        }
    }

    /**
     * 下载头像
     */
    private void downloadProfileAvatar(final BMXUserProfile profile, final ShapeImageView imageView,
            final ImageRequestConfig config) {
        if (profile == null || imageView == null) {
            return;
        }
        FileCallback callback = new FileCallback(profile.avatarUrl()) {
            @Override
            protected void onProgress(long percent, String path, boolean isThumbnail) {

            }

            @Override
            protected void onFinish(String url, boolean isThumbnail) {
                String avatarUrl = "";
                if (isThumbnail) {
                    if (!TextUtils.isEmpty(profile.avatarThumbnailPath())
                            && new File(profile.avatarThumbnailPath()).exists()
                            && new File(profile.avatarThumbnailPath()).isFile()) {
                        avatarUrl = "file://" + profile.avatarThumbnailPath();
                    }
                } else {
                    if (!TextUtils.isEmpty(profile.avatarPath())
                            && new File(profile.avatarPath()).exists()
                            && new File(profile.avatarPath()).isFile()) {
                        avatarUrl = "file://" + profile.avatarPath();
                    }
                }
                BMImageLoader.getInstance().display(imageView, avatarUrl, config);
            }

            @Override
            protected void onFail(String path, boolean isThumbnail) {
                BMImageLoader.getInstance().display(imageView, "", config);
            }
        };
        Observable.just("").map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return UserManager.getInstance().downloadAvatar(profile, new FileProgressListener(){
                    @Override
                    public int onProgressChange(String percent) {
//                                if (Integer.valueOf(percent) >= 100) {
//                                    String avatarUrl = "";
//                                    if (!TextUtils.isEmpty(profile.avatarThumbnailPath())
//                                            && new File(profile.avatarThumbnailPath()).exists()
//                                            && new File(profile.avatarThumbnailPath()).isFile()) {
//                                        avatarUrl = "file://" + profile.avatarThumbnailPath();
//                                    } else
//                                        if (!TextUtils.isEmpty(profile.avatarPath())
//                                            && new File(profile.avatarPath()).exists()
//                                            && new File(profile.avatarPath()).isFile()) {
//                                        avatarUrl = "file://" + profile.avatarPath();
//                                    }
//                                    String finalAvatarUrl = avatarUrl;
//                                    TaskDispatcher.postMainDelayed(() -> {
//                                        BMImageLoader.getInstance().display(imageView, finalAvatarUrl, config);
//                                    }, 500);
//                                }
                        Log.i(TAG, "onProgressChange profile:"+ profile.userId() + "-" + percent);
                        return 0;
                    }
                });
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        // BMImageLoader.getInstance().display(imageView, "", config);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
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
                    }
                });
    }

    /**
     * 下载头像
     */
    private void downloadUserAvatar(final BMXRosterItem item, final ShapeImageView imageView,
            final ImageRequestConfig config) {
        if (item == null || imageView == null) {
            return;
        }
        final int hashCode = imageView.hashCode();
        mViewCache.put(hashCode, item.rosterId());
        FileCallback callback = new FileCallback(item.avatarUrl()) {
            @Override
            protected void onProgress(long percent, String path, boolean isThumbnail) {

            }

            @Override
            protected void onFinish(String url, boolean isThumbnail) {
                if (!mViewCache.containsKey(hashCode)
                        || mViewCache.get(hashCode) != item.rosterId()) {
                    return;
                }
                mViewCache.remove(hashCode);
                String avatarUrl = "";
                if (isThumbnail) {
                    if (!TextUtils.isEmpty(item.avatarThumbnailPath())
                            && new File(item.avatarThumbnailPath()).exists()
                            && new File(item.avatarThumbnailPath()).isFile()) {
                        avatarUrl = "file://" + item.avatarThumbnailPath();
                    }
                } else {
                    if (!TextUtils.isEmpty(item.avatarPath())
                            && new File(item.avatarPath()).exists()
                            && new File(item.avatarPath()).isFile()) {
                        avatarUrl = "file://" + item.avatarPath();
                    }
                }
                BMImageLoader.getInstance().display(imageView, avatarUrl, config);
            }

            @Override
            protected void onFail(String path, boolean isThumbnail) {
                if (mViewCache.containsKey(hashCode)
                        && mViewCache.get(hashCode) == item.rosterId()) {
                    mViewCache.remove(hashCode);
                }
                BMImageLoader.getInstance().display(imageView, "", config);
            }
        };
        Observable.just(item).map(new Func1<BMXRosterItem, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXRosterItem s) {
                return RosterManager.getInstance().downloadAvatar(s, new FileProgressListener() {
                    @Override
                    public int onProgressChange(String percent) {
//                        if (Integer.valueOf(percent) >= 100) {
//                            // 成功
//                            if (!mViewCache.containsKey(hashCode)
//                                    || mViewCache.get(hashCode) != item.rosterId()) {
//                                return 0;
//                            }
//                            mViewCache.remove(hashCode);
//                            String avatarUrl = "";
//                            if (!TextUtils.isEmpty(item.avatarThumbnailPath())
//                                    && new File(item.avatarThumbnailPath()).exists()
//                                    && new File(item.avatarThumbnailPath()).isFile()) {
//                                avatarUrl = "file://" + item.avatarThumbnailPath();
//                            } else
//                                if (!TextUtils.isEmpty(item.avatarPath())
//                                    && new File(item.avatarPath()).exists()
//                                    && new File(item.avatarPath()).isFile()) {
//                                avatarUrl = "file://" + item.avatarPath();
//                            }
//                            String finalAvatarUrl = avatarUrl;
//                            TaskDispatcher.postMainDelayed(() -> {
//                                BMImageLoader.getInstance().display(imageView, finalAvatarUrl, config);
//                            }, 500);
//                        }
                        Log.i(TAG, "onProgressChange roster:" + s.rosterId() + "-" + percent);
                        return 0;
                    }
                });
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        // if (mViewCache.containsKey(hashCode)
                        // && mViewCache.get(hashCode) == item.rosterId()) {
                        // mViewCache.remove(hashCode);
                        // }
                        // BMImageLoader.getInstance().display(imageView, "", config);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
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
                    }
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
        FileCallback callback = new FileCallback(item.avatarUrl()) {
            @Override
            protected void onProgress(long percent, String path, boolean isThumbnail) {

            }

            @Override
            protected void onFinish(String url, boolean isThumbnail) {
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
                    return;
                }
                mViewCache.remove(hashCode);
                BMImageLoader.getInstance().display(imageView, avatarUrl, config);
            }

            @Override
            protected void onFail(String path, boolean isThumbnail) {
                if (mViewCache.containsKey(hashCode)
                        && mViewCache.get(hashCode) == item.groupId()) {
                    mViewCache.remove(hashCode);
                }
                BMImageLoader.getInstance().display(imageView, "", config);
            }
        };
        Observable.just(item).map(new Func1<BMXGroup, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXGroup s) {
                return GroupManager.getInstance().downloadAvatar(s, new FileProgressListener() {
                    @Override
                    public int onProgressChange(String percent) {
//                        if (Integer.valueOf(percent) >= 100) {
//                            String avatarUrl = "";
//                            if (!TextUtils.isEmpty(item.avatarThumbnailPath())
//                                    && new File(item.avatarThumbnailPath()).exists()
//                                    && new File(item.avatarThumbnailPath()).isFile()) {
//                                avatarUrl = "file://" + item.avatarThumbnailPath();
//                            } else if (!TextUtils.isEmpty(item.avatarPath())
//                                    && new File(item.avatarPath()).exists()
//                                    && new File(item.avatarPath()).isFile()) {
//                                avatarUrl = "file://" + item.avatarPath();
//                            }
//                            if (!mViewCache.containsKey(hashCode)
//                                    || mViewCache.get(hashCode) != item.groupId()) {
//                                return 0;
//                            }
//                            mViewCache.remove(hashCode);
//                            String finalAvatarUrl = avatarUrl;
//                            TaskDispatcher.postMainDelayed(() -> {
//                                BMImageLoader.getInstance().display(imageView, finalAvatarUrl, config);
//                            }, 500);
//                        }
                        Log.i(TAG, "onProgressChange group:"+ s.groupId() + "-" + percent);
                        return 0;
                    }
                });
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        // if (mViewCache.containsKey(hashCode)
                        // && mViewCache.get(hashCode) == item.groupId()) {
                        // mViewCache.remove(hashCode);
                        // }
                        // BMImageLoader.getInstance().display(imageView, "", config);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
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
}
