
package top.maxim.im.group.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupList;
import im.floo.floolib.BMXMessage;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.bean.MessageBean;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.RecyclerWithHFAdapter;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;
import top.maxim.im.sdk.utils.MessageSendUtils;

/**
 * Description : 群 Created by Mango on 2018/11/21.
 */
public class ForwardMsgGroupActivity extends BaseTitleActivity {

    private RecyclerView mGroupView;

    private GroupAdapter mAdapter;

    private MessageBean messageBean;

    private long mUserId;

    private MessageSendUtils mSendUtils;

    public static void openForwardMsgGroupActivity(Context context, MessageBean message, int requestCode) {
        Intent intent = new Intent(context, ForwardMsgGroupActivity.class);
        intent.putExtra(MessageConfig.CHAT_MSG, message);
        ((Activity)context).startActivityForResult(intent, requestCode);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle("转发");
        builder.setBackIcon(R.drawable.header_back_icon, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_group, null);
        mGroupView = view.findViewById(R.id.group_recycler);
        mGroupView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new GroupAdapter(this);
        mGroupView.setAdapter(mAdapter);
        return view;
    }

    @Override
    protected void setViewListener() {
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BMXGroup item = mAdapter.getItem(position);
                if (item == null) {
                    return;
                }
                forwardMessage(item.groupId());
            }
        });
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            messageBean = (MessageBean)intent.getSerializableExtra(MessageConfig.CHAT_MSG);
        }
        mUserId = SharePreferenceUtils.getInstance().getUserId();
        mSendUtils = new MessageSendUtils();
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        getAllGroup();
    }

    private void getAllGroup() {
        final BMXGroupList list = new BMXGroupList();
        Observable.just(list).map(new Func1<BMXGroupList, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXGroupList bmxGroupList) {
                return GroupManager.getInstance().search(bmxGroupList, false);
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
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        String error = e != null ? e.getMessage() : "网络错误";
                        ToastUtil.showTextViewPrompt(error);
                        GroupManager.getInstance().search(list, false);
                        List<BMXGroup> groupList = new ArrayList<>();
                        for (int i = 0; i < list.size(); i++) {
                            groupList.add(list.get(i));
                        }
                        mAdapter.replaceList(groupList);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        List<BMXGroup> groupList = new ArrayList<>();
                        for (int i = 0; i < list.size(); i++) {
                            groupList.add(list.get(i));
                        }
                        mAdapter.replaceList(groupList);
                    }
                });
    }

    private void forwardMessage(long groupId) {
        Intent intent = new Intent();
        intent.putExtra(MessageConfig.CHAT_ID, groupId);
        intent.putExtra(MessageConfig.CHAT_TYPE, BMXMessage.MessageType.Group);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private class GroupAdapter extends RecyclerWithHFAdapter<BMXGroup> {

        private ImageRequestConfig mConfig;

        public GroupAdapter(Context context) {
            super(context);
            mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
                    .showImageForEmptyUri(R.drawable.default_group_icon)
                    .showImageOnFail(R.drawable.default_group_icon)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .showImageOnLoading(R.drawable.default_group_icon).build();
        }

        @Override
        protected int onCreateViewById(int viewType) {
            return R.layout.item_contact_view;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            ShapeImageView avatar = holder.findViewById(R.id.contact_avatar);
            TextView title = holder.findViewById(R.id.contact_title);
            BMXGroup bean = getItem(position);
            String name = bean == null ? "" : bean.name();
            title.setText(name);
            ChatUtils.getInstance().showGroupAvatar(bean, avatar, mConfig);
        }
    }
}
