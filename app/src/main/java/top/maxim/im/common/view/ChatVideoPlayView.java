
package top.maxim.im.common.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.util.Locale;

import top.maxim.im.R;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.VideoPlay;

/**
 * 视频播放的view
 */
public class ChatVideoPlayView extends FrameLayout implements View.OnClickListener {

    private static final int VIDEO_DISMISS_BAR = 1001;

    private static final int DELAY_TIME = 5000;

    /* 小视频播放工具类 */
    private VideoPlay mVideoPlay;

    private View contentView;

    private SurfaceView mSurfaceView;

    private ImageView videoBack, videoMore, videoPlay, videoPic;

    private SeekBar videoProgress;

    private TextView videoTime;

    private View topBar, bottomBar;

    /* 第一次进入时控件的宽高 */
    private int mWidth, mHeight;

    private int mStatusBarHeight;

    /* 第一次进入时控件的坐标 */
    private int mXLocation, mYLocation;

    /**
     * 是否显示操作bar
     */
    private boolean isShowBar;

    /**
     * 是否循环播放
     */
    private boolean mIsCircle;

    /**
     * 是否显示动画
     */
    private boolean isShowAnim;

    /* 是否展示顶部操作bar */
    private boolean isShowTopBar = true;

    /* 图片配置 */
    private ImageRequestConfig mConfig;
    
    private onVideoPlayCallBackListener onVideoPlayListener;

    private String mVideoPath;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            dismissActionBar();
        }
    };

    private Runnable mVideoRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (mVideoPlay == null) {
                    Log.i("ChatVideoPlayView", "mVideoPlay is null");
                    return;
                }
                if (mVideoPlay.isPlaying()) {
                    mHandler.postDelayed(this, 500);
                    showStatus(mVideoPlay.getCurrentPosition(), mVideoPlay.getDuration());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public ChatVideoPlayView(Context context) {
        this(context, null);
    }

    public ChatVideoPlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatVideoPlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void setOnVideoPlayListener(onVideoPlayCallBackListener onVideoPlayListener) {
        this.onVideoPlayListener = onVideoPlayListener;
    }

    private void initEvent() {
        dismissActionBar();
        mVideoPlay.setOnPlayListener(new VideoPlay.OnPlayListener() {
            @Override
            public void onPlayComplete() {
                if (mIsCircle) {
                    startPlay();
                } else {
                    stopPlay();
                }
            }

            @Override
            public void changeSurfaceViewSize(int width, int height) {
                final double scale = (double)height / (double)width;
                mSurfaceView.post(new Runnable() {
                    @Override
                    public void run() {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mSurfaceView
                                .getLayoutParams();
                        params.width = FrameLayout.LayoutParams.MATCH_PARENT;
                        if (ScreenUtils.widthPixels * scale > ScreenUtils.heightPixels) {
                            params.height = FrameLayout.LayoutParams.MATCH_PARENT;
                        } else {
                            params.height = (int)(ScreenUtils.widthPixels * scale);
                        }
                        mSurfaceView.setLayoutParams(params);
                    }
                });
            }

            @Override
            public void onError() {
                if (((Activity)getContext()).isFinishing()) {
                    return;
                }
                ToastUtil.showTextViewPrompt(getContext().getString(R.string.video_message_error));
                ((Activity)getContext()).finish();
            }

            @Override
            public void onPrepare() {
                if (TextUtils.isEmpty(mVideoPath)) {
                    return;
                }
                if (isShowAnim) {
                    showVideoPic(mVideoPath);
                } else {
                    startPlay();
                }
            }
        });

        videoPlay.setOnClickListener(this);
        videoBack.setOnClickListener(this);
        videoMore.setOnClickListener(this);
        mSurfaceView.setOnClickListener(this);
        contentView.setOnClickListener(this);

        videoProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (mVideoPlay.isPlaying()) {
                        mVideoPlay.pause();
                    }
                    if (mVideoPlay != null) {
                        mVideoPlay.seekTo(progress);
                    }
                    showStatus(progress, seekBar.getMax());
                    resetBarStatus();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startPlay();
            }
        });

    }

    public void setShowTopBar(boolean isShowTopBar) {
        this.isShowTopBar = isShowTopBar;
    }

    private void initView(Context context) {
        contentView = LayoutInflater.from(context).inflate(R.layout.chat_video_play_view, this);
        mSurfaceView = contentView.findViewById(R.id.surface);
        videoBack = contentView.findViewById(R.id.video_back);
        videoMore = contentView.findViewById(R.id.video_more);
        videoPlay = contentView.findViewById(R.id.video_play);
        videoPic = contentView.findViewById(R.id.video_pic);
        videoTime = contentView.findViewById(R.id.video_time);
        videoProgress = contentView.findViewById(R.id.video_progress);
        topBar = contentView.findViewById(R.id.video_top_bar);
        bottomBar = contentView.findViewById(R.id.video_bottom_bar);

        mConfig = new ImageRequestConfig.Builder().cacheInMemory(true).cacheOnDisk(true)
                .imageScaleType(ImageScaleType.NONE).bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true).build();
        mVideoPlay = new VideoPlay(getContext(), mSurfaceView);
        initEvent();
    }

    /**
     * 视频封面
     */
    private void initPicView() {
        if (!isShowAnim) {
            mSurfaceView.setVisibility(VISIBLE);
            return;
        }
        // 获取视频封面的宽高，并设置大小位置
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)videoPic
                .getLayoutParams();
        params.height = mHeight;
        params.width = mWidth;
        videoPic.setLayoutParams(params);
        videoPic.setX(mXLocation);
        videoPic.setY(mYLocation);
        // 显示视频封面，用于动画显示。
        videoPic.setVisibility(isShowAnim ? VISIBLE : GONE);
    }

    private void showEnterVideoAnim() {
        // 平移属性
        float fromX = mXLocation;
        float toX = (ScreenUtils.widthPixels - mWidth) / 2;
        ObjectAnimator moveX = ObjectAnimator.ofFloat(videoPic, "translationX", fromX, toX);
        float fromY = mYLocation;
        float toY = (ScreenUtils.heightPixels - mHeight - mStatusBarHeight) / 2;
        ObjectAnimator moveY = ObjectAnimator.ofFloat(videoPic, "translationY", fromY, toY);
        // 缩放属性
        final float scale;
        scale = (float)ScreenUtils.widthPixels / mWidth;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(videoPic, "scaleX", 1f, scale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(videoPic, "scaleY", 1f, scale);

        // 播放动画
        AnimatorSet set = new AnimatorSet();
        set.setDuration(300);
        set.play(moveX).with(moveY).with(scaleX).with(scaleY);
        // 动画结束播放视频
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                startPlay();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.start();
    }

    /**
     * 退出 动画
     */
    public void exitShowAnim() {
        // 暂停视频并显示视频封面。隐藏视频
        pausePlay();
        dismissActionBar();
        mSurfaceView.setVisibility(GONE);
        // 缩小动画，并退出
        videoPic.animate().setDuration(300).translationX(mXLocation).translationY(mYLocation)
                .scaleX(1).scaleY(1).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (onVideoPlayListener != null) {
                            onVideoPlayListener.onBack();
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }

    /**
     * 是否循环播放
     */
    public void setCirclePlay(boolean isCircle) {
        this.mIsCircle = isCircle;
    }

    /**
     * 是否静音播放
     */
    public void setIsNoSound(boolean isNoSound) {
        if (isNoSound) {
            mVideoPlay.setNotVolume();
        }
    }

    public void setPrepareVideoPath(final String videoPath) {
        mVideoPath = videoPath;
        if (TextUtils.isEmpty(videoPath) || !new File(videoPath).exists()) {
            ToastUtil.showTextViewPrompt(getResources().getString(R.string.chat_file_not_exit));
            ((Activity)getContext()).finish();
            return;
        }
        mVideoPlay.setVideoPath(mVideoPath);
    }

    /**
     * 设置视频封面图信息
     *
     * @param width 缩略图宽
     * @param height 缩略图 高
     * @param x 缩略图 位置x
     * @param y 缩略图 位置y
     */
    public void setPicViewInfo(int width, int height, int x, int y) {
        mWidth = width;
        mHeight = height;
        // 获取状态栏高度 SDK19以上 隐藏状态栏 所以为0
        mStatusBarHeight = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? 0
                : ScreenUtils.getStatusBarHeight();
        mXLocation = x;
        mYLocation = y - mStatusBarHeight < 0 ? 0 : y - mStatusBarHeight;
        // 如果宽高为0 不显示动画
        // isShowAnim = mWidth != 0 || mHeight != 0;
        isShowAnim = false;
        initPicView();
    }

    private void showVideoPic(String videoPath) {
        if (!TextUtils.isEmpty(videoPath) && videoPic != null) {
            BMImageLoader.getInstance().display(videoPic, "file://" + videoPath, mConfig,
                    new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view,
                                FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view,
                                Bitmap loadedImage) {
                            showEnterVideoAnim();
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    });
        }
    }

    public void showActionBar() {
        if (topBar != null && bottomBar != null) {
            isShowBar = true;
            topBar.setVisibility(isShowTopBar ? VISIBLE : GONE);
            bottomBar.setVisibility(VISIBLE);
            // 5s after auto dismiss
            mHandler.sendEmptyMessageDelayed(VIDEO_DISMISS_BAR, DELAY_TIME);
        }
    }

    public void dismissActionBar() {
        if (topBar != null && bottomBar != null) {
            isShowBar = false;
            topBar.setVisibility(GONE);
            bottomBar.setVisibility(GONE);
            mHandler.removeMessages(VIDEO_DISMISS_BAR);
        }
    }

    public boolean isShowActionBar() {
        return isShowBar;
    }

    public void startPlay() {
        mSurfaceView.setVisibility(VISIBLE);
        videoPlay.setImageResource(R.drawable.chat_file_video_play);
        final int max = mVideoPlay.getDuration();
        if (max <= 0) {
            Log.i("ChatVideoPlayView", "video length error");
            return;
        }
        mVideoPlay.start();
        videoProgress.setMax(max);
        mHandler.post(mVideoRunnable);
    }

    private void showStatus(int curTime, int allTime) {
        if (mVideoPlay == null) {
            return;
        }
        videoProgress.setProgress(curTime);
        String cur_time = secondsToStr(curTime);
        String all_time = secondsToStr(allTime);
        videoTime.setText(cur_time + "/" + all_time);
    }

    public void stopPlay() {
        int maxProgress = mVideoPlay.getDuration();
        videoProgress.setProgress(maxProgress);
        String all_time = secondsToStr(maxProgress);
        videoTime.setText(all_time + "/" + all_time);
        pausePlay();
    }

    public void pausePlay() {
        videoPlay.setImageResource(R.drawable.chat_file_video_pause);
        mVideoPlay.pause();
    }

    /**
     * 重置bar的显示状态
     */
    private void resetBarStatus() {
        mHandler.removeMessages(VIDEO_DISMISS_BAR);
        mHandler.sendEmptyMessageDelayed(VIDEO_DISMISS_BAR, DELAY_TIME);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        // 播放按钮
        if (i == R.id.video_play) {
            if (mVideoPlay.isPlaying()) {
                pausePlay();
            } else {
                startPlay();
            }
            resetBarStatus();
        }
        // 返回按钮
        if (i == R.id.video_back) {
            if (isShowAnim) {
                exitShowAnim();
            } else {
                if (onVideoPlayListener != null) {
                    onVideoPlayListener.onBack();
                }
            }
        }
        // 三个点
        if (i == R.id.video_more) {
            if (onVideoPlayListener != null) {
                onVideoPlayListener.onMorePress();
            }
        }
        // 点击视频的时候显示隐藏控制菜单
        if (i == R.id.surface || v == contentView) {
            if (isShowBar) {
                dismissActionBar();
            } else {
                showActionBar();
            }
        }
    }

    /**
     * 毫秒转日期字符串 65秒转换成 01:05
     */
    private String secondsToStr(long time) {
        int seconds = (int)(time + 999) / 1000;
        int second = seconds % 60;
        int minute = seconds / 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minute, second);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mVideoPlay != null) {
            mVideoPlay.stop();
            mVideoPlay = null;
        }
        // showAnim(false);
    }

    public interface onVideoPlayCallBackListener {
        /**
         * 返回按钮
         */
        void onBack();

        /**
         * 三个点
         */
        void onMorePress();
    }
}
