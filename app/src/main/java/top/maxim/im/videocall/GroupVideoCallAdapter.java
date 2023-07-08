package top.maxim.im.videocall;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.HashSet;
import java.util.Set;

import im.floo.floolib.BMXRosterItem;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseRecyclerAdapter;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.rtc.view.BMXRtcRenderView;
import top.maxim.rtc.view.RTCRenderView;

/**
 * Description : 群音视频adapter
 */
class GroupVideoCallAdapter extends BaseRecyclerAdapter<String> {

    private ImageRequestConfig mConfig;

    private int spanCount;

    //开启视频的用户缓存
    private Set<String> mVideoCache = new HashSet<>();

    public GroupVideoCallAdapter(Context context) {
        super(context);
        mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
                .showImageForEmptyUri(R.drawable.default_avatar_icon)
                .showImageOnFail(R.drawable.default_avatar_icon)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageOnLoading(R.drawable.default_avatar_icon).build();
    }

    private void addVideoRoster(long rosterId) {
        mVideoCache.add(String.valueOf(rosterId));
    }

    private void removeVideoRoster(long rosterId) {
        mVideoCache.remove(String.valueOf(rosterId));
    }

    private boolean isVideo(long rosterId) {
        return mVideoCache.contains(String.valueOf(rosterId));
    }

    public void setSpanCount(int spanCount){
        this.spanCount = spanCount;
    }

    @Override
    protected int onCreateViewById(int viewType) {
        return R.layout.item_group_video_call_member;
    }

    @Override
    protected void onBindHolder(BaseViewHolder holder, int position) {
        ViewGroup container = holder.findViewById(R.id.group_video_call_container);
        ShapeImageView audioView = holder.findViewById(R.id.group_audio_view);
        FrameLayout videoView = holder.findViewById(R.id.group_video_view);
        String rosterId = getItem(position);
        if (spanCount <= 0) {
            spanCount = 2;
        }
        int width = ScreenUtils.widthPixels / spanCount;
        container.setLayoutParams(new LinearLayout.LayoutParams(width, width));
        boolean hasVideo = isVideo(Long.valueOf(rosterId));
        boolean isMe = TextUtils.equals(SharePreferenceUtils.getInstance().getUserId() + "", rosterId);
        if(hasVideo){
            audioView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            addVideoView(videoView);
        }else{
            audioView.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            removeVideoView(videoView);
            if (isMe) {
                //我自己
                ChatUtils.getInstance().showProfileAvatar(UserManager.getInstance().getProfileByDB(), audioView, mConfig);
            } else {
                BMXRosterItem rosterItem = RosterFetcher.getFetcher().getRoster(Long.valueOf(rosterId));
                ChatUtils.getInstance().showRosterAvatar(rosterItem, audioView, mConfig);
            }
        }
    }

    private void addVideoView(FrameLayout videoContainer){
        videoContainer.removeAllViews();
        BMXRtcRenderView view = new RTCRenderView(mContext);
        view.init();
        view.getSurfaceView().setZOrderMediaOverlay(true);
        videoContainer.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void removeVideoView(FrameLayout videoContainer){
        videoContainer.removeAllViews();
    }

}
