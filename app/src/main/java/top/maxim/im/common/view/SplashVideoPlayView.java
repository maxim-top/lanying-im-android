
package top.maxim.im.common.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.RawRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import top.maxim.im.R;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.VideoPlay;

/**
 * 视频播放的view
 */
public class SplashVideoPlayView extends FrameLayout {

    /* 小视频播放工具类 */
    private VideoPlay mVideoPlay;

    private View contentView;

    private SurfaceView mSurfaceView;

    private OnPlayVideoListener mListener;

    public SplashVideoPlayView(Context context) {
        this(context, null);
    }

    public SplashVideoPlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SplashVideoPlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initEvent() {
        mVideoPlay.setOnPlayListener(new VideoPlay.OnPlayListener() {
            @Override
            public void onPlayComplete() {
                stopPlay();
                if (mListener != null) {
                    mListener.onFinish();
                }
            }

            @Override
            public void changeSurfaceViewSize(int width, int height) {
                final double scale = (double)height / (double)width;
                mSurfaceView.post(new Runnable() {
                    @Override
                    public void run() {
                        LayoutParams params = (LayoutParams)mSurfaceView.getLayoutParams();
                        params.width = LayoutParams.MATCH_PARENT;
                        if (ScreenUtils.widthPixels * scale > ScreenUtils.heightPixels) {
                            params.height = LayoutParams.MATCH_PARENT;
                        } else {
                            params.height = (int)(ScreenUtils.widthPixels * scale);
                        }
                        mSurfaceView.setLayoutParams(params);
                    }
                });
            }

            @Override
            public void onError() {
                if (mListener != null) {
                    mListener.onError();
                }
            }

            @Override
            public void onPrepare() {
                if (mListener != null) {
                    mListener.onStart();
                }
                startPlay();
            }
        });
    }

    private void initView(Context context) {
        contentView = LayoutInflater.from(context).inflate(R.layout.splash_video_play_view, this);
        mSurfaceView = contentView.findViewById(R.id.surface);
        mVideoPlay = new VideoPlay(getContext(), mSurfaceView);
        mSurfaceView.setZOrderOnTop(true);
        mSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        initEvent();
    }

    public void setPlayListener(OnPlayVideoListener listenr) {
        mListener = listenr;
    }

    public void setPrepareVideoPath(@RawRes int videoRes) {
        mVideoPlay.setVideoPath(videoRes);
    }

    public void startPlay() {
        mVideoPlay.start();
    }

    public void stopPlay() {
        if (mVideoPlay != null) {
            mVideoPlay.stop();
            mVideoPlay = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mVideoPlay != null) {
            mVideoPlay.stop();
            mVideoPlay = null;
        }
    }

    public interface OnPlayVideoListener {

        void onStart();

        void onFinish();

        void onError();
    }
}
