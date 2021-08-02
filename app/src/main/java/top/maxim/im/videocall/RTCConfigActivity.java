package top.maxim.im.videocall;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import top.maxim.im.R;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.dialog.CustomDialog;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ItemLine;
import top.maxim.im.common.view.ItemLineArrow;
import top.maxim.im.common.view.ItemLineSwitch;
import top.maxim.im.common.view.recyclerview.BaseRecyclerAdapter;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.rtc.engine.EngineConfig;
import top.maxim.rtc.engine.EngineParams;

/**
 * Description 音视频设置
 */
public class RTCConfigActivity extends BaseTitleActivity {

    private static final String TAG = "RTCConfigActivity";

    private LinearLayout mContainer;

    /* 分辨率 */
    private ItemLineArrow.Builder mVideoProfile;

    /* 自动发布 */
    private ItemLineSwitch.Builder mSwitchAutoPublish;

    /* 自动订阅 */
    private ItemLineSwitch.Builder mSwitchAutoSubscribe;

    /* 扬声器 */
    private ItemLineSwitch.Builder mSwitchSpeaker;

    public static void openRTCConfig(Context context) {
        Intent intent = new Intent(context, RTCConfigActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.config_rtc);
        builder.setBackIcon(R.drawable.header_back_icon, v -> finish());
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_video_call_config, null);
        mContainer = view.findViewById(R.id.ll_video_call_container);
        addChildren(mContainer);
        return view;
    }

    /**
     * 添加子view
     */
    private void addChildren(ViewGroup container){
        // videoProfile
        mVideoProfile = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.config_rtc_video_profile))
                .setEndContent(EngineConfig.VIDEO_PROFILE)
                .setOnItemClickListener(v -> showVideoProfile());
        container.addView(mVideoProfile.build());
        // 分割线
        addLineView(container);
        // 自动发布
        mSwitchAutoPublish = new ItemLineSwitch.Builder(this)
                .setLeftText(getString(R.string.config_rtc_auto_publish))
                .setCheckStatus(EngineConfig.SWITCH_AUTO_PUBLISH)
                .setOnItemSwitchListener((v, check) ->
                        EngineConfig.SWITCH_AUTO_PUBLISH = check);
//        container.addView(mSwitchAutoPublish.build());
//        // 分割线
//        addLineView(container);
        // 自动订阅
        mSwitchAutoSubscribe = new ItemLineSwitch.Builder(this)
                .setLeftText(getString(R.string.config_rtc_auto_subscribe))
                .setCheckStatus(EngineConfig.SWITCH_AUTO_SUBSCRIBE)
                .setOnItemSwitchListener((v, check) ->
                        EngineConfig.SWITCH_AUTO_SUBSCRIBE = check);
//        container.addView(mSwitchAutoSubscribe.build());
//        // 分割线
//        addLineView(container);

        // 扬声器
        mSwitchSpeaker = new ItemLineSwitch.Builder(this)
                .setLeftText(getString(R.string.config_rtc_speaker))
                .setCheckStatus(EngineConfig.SWITCH_SPEAKER)
                .setOnItemSwitchListener((v, check) ->
                        EngineConfig.SWITCH_SPEAKER = check);
        container.addView(mSwitchSpeaker.build());
        // 分割线
        addLineView(container);
    }

    // 添加分割线
    private View addLineView(ViewGroup container) {
        // 分割线
        View view;
        ItemLine.Builder itemLine = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(view = itemLine.build());
        return view;
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
    }

    /**
     * 展示videoProfile
     */
    private void showVideoProfile() {
        List<String> list = new ArrayList<>();
        list.add(EngineParams.VideoProfile.VIDEO_PROFILE_320_180);
        list.add(EngineParams.VideoProfile.VIDEO_PROFILE_480_360);
        list.add(EngineParams.VideoProfile.VIDEO_PROFILE_640_360);
        list.add(EngineParams.VideoProfile.VIDEO_PROFILE_640_480);
        list.add(EngineParams.VideoProfile.VIDEO_PROFILE_1280_720);
        list.add(EngineParams.VideoProfile.VIDEO_PROFILE_1920_1080);
        CustomDialog dialog = new CustomDialog();
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        VideoProfileAdapter adapter = new VideoProfileAdapter(this, list);
        adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                mVideoProfile.setEndContent(adapter.getItem(position));
                EngineConfig.VIDEO_PROFILE = adapter.getItem(position);
            }
        });
        recyclerView.setAdapter(adapter);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.addView(recyclerView, params);
        dialog.setCustomView(ll);
        dialog.showDialog(this);
    }

    private class VideoProfileAdapter extends BaseRecyclerAdapter<String> {

        public VideoProfileAdapter(Context context) {
            super(context);
        }

        public VideoProfileAdapter(Context context, List<String> list) {
            super(context, list);
        }

        @Override
        protected int onCreateViewById(int viewType) {
            return R.layout.item_video_profile;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            TextView tv = holder.findViewById(R.id.tv_video_profile);
            String text = getItem(position);
            tv.setText(TextUtils.isEmpty(text) ? "" : text);
        }
    }
}
