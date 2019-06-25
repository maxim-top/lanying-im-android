
package top.maxim.im.message.view;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.view.ChatVideoPlayView;
import top.maxim.im.common.view.Header;

public class VideoDetailActivity extends BaseTitleActivity {

    private String mPath;

    private ChatVideoPlayView mVideoView;

    public static final String VIDEO_DATA = "videoData";

    public static void openVideoDetail(Context context, String path) {
        Intent intent = new Intent(context, VideoDetailActivity.class);
        intent.putExtra(VIDEO_DATA, path);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        return new Header.Builder(this, headerContainer).build();
    }

    @Override
    protected View onCreateView() {
        hideStatusBar();
        FrameLayout frameLayout = new FrameLayout(this);
        mVideoView = new ChatVideoPlayView(this);
        frameLayout.addView(mVideoView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return frameLayout;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        if (intent != null) {
            mPath = intent.getStringExtra(VIDEO_DATA);
        }
    }

    @Override
    protected void setViewListener() {
        super.setViewListener();
        mVideoView.setOnVideoPlayListener(new ChatVideoPlayView.onVideoPlayCallBackListener() {
            @Override
            public void onBack() {
                if (mVideoView != null) {
                    mVideoView.pausePlay();
                }
                finish();
            }

            @Override
            public void onMorePress() {

            }
        });
    }

    @Override
    protected void initDataForActivity() {
        // 不展示顶部操作bar
        mVideoView.setPicViewInfo(0, 0, 0, 0);
        mVideoView.setPrepareVideoPath(mPath);
    }

    /**
     * 设置全屏展示 隐藏状态栏 底部导航栏
     */
    private void hideStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final View decorView = getWindow().getDecorView();
            doFullScreen(decorView);
            decorView.setOnSystemUiVisibilityChangeListener(
                    new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        doFullScreen(decorView);
                                    }
                                }, 500);
                            }
                        }
                    });
        }
        hideHeader();
    }

    /**
     * 设置全屏
     * 
     * @param decorView
     */
    private void doFullScreen(View decorView) {
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

}
