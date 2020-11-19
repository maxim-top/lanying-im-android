
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.UUID;

import im.floo.BMXCallBack;
import im.floo.floolib.BMXConnectStatus;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXPushService;
import im.floo.floolib.TagList;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.PushManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonCustomDialog;
import top.maxim.im.common.utils.dialog.CommonDialog;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ItemLine;
import top.maxim.im.common.view.ItemLineArrow;
import top.maxim.im.common.view.ItemLineSwitch;

/**
 * Description : Push设置 Created by Mango on 2018/11/21.
 */
public class PushSetActivity extends BaseTitleActivity {

    /* token */
    private ItemLineArrow.Builder mPushToken;

    /* cert */
    private ItemLineArrow.Builder mPushCert;

    /* push状态 */
    private ItemLineArrow.Builder mPushStatus;

    /* 开启push */
    private ItemLineArrow.Builder mStartPush;

    /* 切换push */
    private ItemLineSwitch.Builder mSwitchPush;

    /* push Tags */
    private ItemLineArrow.Builder mPushTags;

    /* 添加tag */
    private ItemLineArrow.Builder mAddTags;

    /* 删除tag */
    private ItemLineArrow.Builder mDelTags;

    /* 清除tag */
    private ItemLineArrow.Builder mClearTags;

    /* 角标 */
    private ItemLineArrow.Builder mSetBadge;

    /* pushMode */
    private ItemLineSwitch.Builder mSwitchPushMode;

    /* 推送时间 */
    private ItemLineArrow.Builder mPushTime;

    /* 静默时间 */
    private ItemLineArrow.Builder mSilenceTime;

    /* 后台运行 */
    private ItemLineSwitch.Builder mSwitchRunBack;

    /* 删除通知 */
    private ItemLineArrow.Builder mDelNotification;

    /* 清除通知 */
    private ItemLineArrow.Builder mClearNotification;

    /* 发送消息 */
    private ItemLineArrow.Builder mSendPushMsg;

    /* 推送消息 */
    private ItemLineArrow.Builder mPushMsg;

    public static void openPushSet(Context context) {
        Intent intent = new Intent(context, PushSetActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.set_push);
        builder.setBackIcon(R.drawable.header_back_icon, v -> finish());
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_push_set, null);
        LinearLayout container = view.findViewById(R.id.ll_setting_container);
        // push token
//        mPushToken = new ItemLineArrow.Builder(this)
//                .setStartContent(getString(R.string.set_push_token));
//        container.addView(mPushToken.build());
//        // 分割线
//        addLineView(container);
//        // push cert
//        mPushCert = new ItemLineArrow.Builder(this)
//                .setStartContent(getString(R.string.set_push_cert));
//        container.addView(mPushCert.build());
//        // 分割线
//        addLineView(container);
        // push status
        mPushStatus = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.set_push_status));
        container.addView(mPushStatus.build());
        // 分割线
        addLineView(container);
        // 开启push
        // mStartPush = new ItemLineArrow.Builder(this)
        // .setStartContent(getString(R.string.set_push_start))
        // .setOnItemClickListener(v -> showStartPush());
        // container.addView(mStartPush.build());
        // // 分割线
        // addLineView(container);

        // 切换push开关
        mSwitchPush = new ItemLineSwitch.Builder(this)
                .setLeftText(getString(R.string.set_push_switch))
                .setOnItemSwitchListener((v, curCheck) -> {
                    showLoadingDialog(true);
                    BMXCallBack callBack = bmxErrorCode -> {
                        dismissLoadingDialog();
                        if (BaseManager.bmxFinish(bmxErrorCode)) {
//                            initData();  不能重新获取数据  有延迟
                            mSwitchPush.setCheckStatus(curCheck);
                        } else {
                            mSwitchPush.setCheckStatus(!curCheck);
                            toastError(bmxErrorCode);
                        }
                    };
                    if (curCheck) {
                        PushManager.getInstance().resume(callBack);
                    } else {
                        PushManager.getInstance().stop(callBack);
                    }
                });
        container.addView(mSwitchPush.build());
        // 分割线
        addLineView(container);

        // push tag
        mPushTags = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.set_push_tags))
                .setOnItemClickListener(v -> showTotalTag());
        container.addView(mPushTags.build());
        // 分割线
        addLineView(container);

        // 添加tag
        mAddTags = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.set_push_add_tags))
                .setOnItemClickListener(v -> showAddTag());
        container.addView(mAddTags.build());
        // 分割线
        addLineView(container);

        // 删除tag
        mDelTags = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.set_push_del_tags))
                .setOnItemClickListener(v -> showDelTag());
        container.addView(mDelTags.build());
        // 分割线
        addLineView(container);

        // 清除tag
        mClearTags = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.set_push_clear_tags))
                .setOnItemClickListener(v -> showClearTag());
        container.addView(mClearTags.build());
        // 分割线
        addLineView(container);

        // 角标数
        mSetBadge = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.set_push_badge))
                .setOnItemClickListener(v -> showBadge());
        container.addView(mSetBadge.build());
        // 分割线
        addLineView(container);

        // pushMode
        mSwitchPushMode = new ItemLineSwitch.Builder(this)
                .setLeftText(getString(R.string.set_push_mode))
                .setOnItemSwitchListener((v, curCheck) -> {
                    showLoadingDialog(true);
                    BMXCallBack callBack = bmxErrorCode -> {
                        dismissLoadingDialog();
                        if (!BaseManager.bmxFinish(bmxErrorCode)) {
                            mSwitchPushMode.setCheckStatus(!curCheck);
                            toastError(bmxErrorCode);
                        }
                    };
                    PushManager.getInstance().setPushMode(curCheck, callBack);
                });
        container.addView(mSwitchPushMode.build());
        // 分割线
        addLineView(container);
        
        // 推送时间
        mPushTime = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.set_push_time))
                .setOnItemClickListener(v -> showTime(getString(R.string.set_push_time)));
        container.addView(mPushTime.build());
        // 分割线
        addLineView(container);
        
        // 静默时间
        mSilenceTime = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.set_push_silence_time))
                .setOnItemClickListener(v -> showTime(getString(R.string.set_push_silence_time)));
        container.addView(mSilenceTime.build());
        // 分割线
        addLineView(container);

        // 后台运行
        mSwitchRunBack = new ItemLineSwitch.Builder(this)
                .setLeftText(getString(R.string.set_push_run_background))
                .setOnItemSwitchListener((v, curCheck) -> {
                    showLoadingDialog(true);
                    BMXCallBack callBack = bmxErrorCode -> {
                        dismissLoadingDialog();
                        if (!BaseManager.bmxFinish(bmxErrorCode)) {
                            mSwitchRunBack.setCheckStatus(!curCheck);
                            toastError(bmxErrorCode);
                        }
                    };
                    PushManager.getInstance().setRunBackgroundMode(curCheck, callBack);
                });
        container.addView(mSwitchRunBack.build());
        // 分割线
        addLineView(container);

        // 删除通知
        mDelNotification = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.set_push_del_notification))
                .setOnItemClickListener(v -> showDelNotification());
        container.addView(mDelNotification.build());
        // 分割线
        addLineView(container);
        
        // 清除通知
        mClearNotification = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.set_push_clear_notification))
                .setOnItemClickListener(v -> showClearNotification());
        container.addView(mClearNotification.build());
        // 分割线
        addLineView(container);
        
        // 发送消息
        mSendPushMsg = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.set_push_send_message))
                .setOnItemClickListener(v -> showSendMessage());
        container.addView(mSendPushMsg.build());
        // 分割线
        addLineView(container);
        
        // 发送消息
        mPushMsg = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.set_push_message))
                .setOnItemClickListener(v -> PushMessageActivity.openPushMessageActivity(this));
        container.addView(mPushMsg.build());
        // 分割线
        addLineView(container);
        return view;
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
        // 默认push相关的开关都是true
        mSwitchPushMode.setCheckStatus(true);
        mSwitchRunBack.setCheckStatus(true);
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
//        String token = PushManager.getInstance().getToken();
//        String cert = PushManager.getInstance().getCert();
//        mPushToken.setEndContent(TextUtils.isEmpty(token) ? "" : token);
//        mPushCert.setEndContent(TextUtils.isEmpty(cert) ? "" : token);
        // IM连接状态
        BMXConnectStatus connectStatus = UserManager.getInstance().connectStatus();
        mSwitchPush.setCheckStatus(connectStatus == BMXConnectStatus.Connected);
        // PushMode
        BMXPushService.PushSdkStatus pushStatus = PushManager.getInstance().status();
        String status = "不可用";
        if (pushStatus != null) {
            if (pushStatus == BMXPushService.PushSdkStatus.Started) {
                status = "已开启";
            } else if (pushStatus == BMXPushService.PushSdkStatus.Starting) {
                status = "开启中";
            } else if (pushStatus == BMXPushService.PushSdkStatus.Stoped) {
                status = "已关闭";
            } else if (pushStatus == BMXPushService.PushSdkStatus.Offline) {
                status = "离线";
            }
        }
        mPushStatus.setEndContent(status);
        // Tag
        getTags();
    }

    /**
     * 获取推送tag
     */
    private void getTags() {
        TagList tagList = new TagList();
        PushManager.getInstance().getTags(tagList, buildOperateId(), bmxErrorCode -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < tagList.size(); i++) {
                    builder.append(tagList.get(i)).append(",");
                }
                if (!TextUtils.isEmpty(builder)) {
                    builder.deleteCharAt(builder.length() - 1);
                }
                mPushTags.setEndContent(TextUtils.isEmpty(builder) ? "" : builder.toString());
            }
        });
    }

    /**
     * 开启push
     */
    private void showStartPush() {
        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams editP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editP.setMargins(ScreenUtils.dp2px(10), ScreenUtils.dp2px(5), ScreenUtils.dp2px(10),
                ScreenUtils.dp2px(5));
        // 别名
        TextView name = new TextView(this);
        name.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        name.setTextColor(getResources().getColor(R.color.color_black));
        name.setBackgroundColor(getResources().getColor(R.color.color_white));
        name.setText(getString(R.string.set_push_alias));
        ll.addView(name, textP);

        final EditText editName = new EditText(this);
        editName.setBackgroundResource(R.drawable.common_edit_corner_bg);
        editName.setPadding(ScreenUtils.dp2px(5), 0, ScreenUtils.dp2px(5), 0);
        editName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        editName.setTextColor(getResources().getColor(R.color.color_black));
        editName.setMinHeight(ScreenUtils.dp2px(40));
        ll.addView(editName, editP);

        // token
        TextView desc = new TextView(this);
        desc.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        desc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        desc.setTextColor(getResources().getColor(R.color.color_black));
        desc.setBackgroundColor(getResources().getColor(R.color.color_white));
        desc.setText(getString(R.string.set_push_token));
        ll.addView(desc, textP);

        final EditText editDesc = new EditText(this);
        editDesc.setBackgroundResource(R.drawable.common_edit_corner_bg);
        editDesc.setPadding(ScreenUtils.dp2px(5), 0, ScreenUtils.dp2px(5), 0);
        editDesc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        editDesc.setTextColor(getResources().getColor(R.color.color_black));
        editDesc.setMinHeight(ScreenUtils.dp2px(40));
        ll.addView(editDesc, editP);

        DialogUtils.getInstance().showCustomDialog(this, ll, getString(R.string.set_push_start),
                getString(R.string.confirm), getString(R.string.cancel),
                new CommonCustomDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener() {
                        showLoadingDialog(true);
                        BMXCallBack callBack = bmxErrorCode -> {
                            dismissLoadingDialog();
                            if (BaseManager.bmxFinish(bmxErrorCode)) {
                                initData();
                            } else {
                                toastError(bmxErrorCode);
                            }
                        };
                        String alias = editName.getEditableText().toString().trim();
                        String token = editDesc.getEditableText().toString().trim();
                        if (!TextUtils.isEmpty(alias) && !TextUtils.isEmpty(token)) {
                            PushManager.getInstance().start(alias, token, callBack);
                        } else if (!TextUtils.isEmpty(alias)) {
                            PushManager.getInstance().start(alias, callBack);
                        } else {
                            PushManager.getInstance().start(callBack);
                        }
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    private BMXCallBack tagCallBack = bmxErrorCode -> {
        dismissLoadingDialog();
        if (BaseManager.bmxFinish(bmxErrorCode)) {
            getTags();
        } else {
            toastError(bmxErrorCode);
        }
    };

    /**
     * 展示所有tag
     */
    private void showTotalTag() {
        TagList tagList = new TagList();
        PushManager.getInstance().getTags(tagList, buildOperateId(), bmxErrorCode -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < tagList.size(); i++) {
                    builder.append(tagList.get(i)).append(",");
                }
                if (!TextUtils.isEmpty(builder)) {
                    builder.deleteCharAt(builder.length() - 1);
                    DialogUtils.getInstance().showDialog(this, getString(R.string.set_push_tags),
                            builder.toString(), getString(R.string.confirm),
                            getString(R.string.cancel), null);
                }
            }
        });
    }

    /**
     * 添加tag
     */
    private void showAddTag() {
        DialogUtils.getInstance().showEditDialog(this, getString(R.string.set_push_add_tags),
                getString(R.string.confirm), getString(R.string.cancel),
                new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        if (TextUtils.isEmpty(content)) {
                            return;
                        }
                        showLoadingDialog(true);
                        TagList tags = new TagList();
                        tags.add(content);
                        PushManager.getInstance().setTags(tags, buildOperateId(), tagCallBack);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 删除tag
     */
    private void showDelTag() {
        DialogUtils.getInstance().showEditDialog(this, getString(R.string.set_push_del_tags),
                getString(R.string.confirm), getString(R.string.cancel),
                new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        if (TextUtils.isEmpty(content)) {
                            return;
                        }
                        showLoadingDialog(true);
                        TagList tags = new TagList();
                        tags.add(content);
                        PushManager.getInstance().deleteTags(tags, buildOperateId(), tagCallBack);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 清除tag
     */
    private void showClearTag() {
        DialogUtils.getInstance().showDialog(this, getString(R.string.set_push_clear_tags), "",
                getString(R.string.confirm), getString(R.string.cancel),
                new CommonDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener() {
                        showLoadingDialog(true);
                        PushManager.getInstance().clearTags(buildOperateId(), tagCallBack);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 设置角标
     */
    private void showBadge() {
        DialogUtils.getInstance().showEditDialog(this, getString(R.string.set_push_badge), getString(R.string.confirm), getString(R.string.cancel),
                true, new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        if (!TextUtils.isEmpty(content)) {
                            showLoadingDialog(true);
                            PushManager.getInstance().setBadge(Integer.valueOf(content),
                                    bmxErrorCode -> {
                                        dismissLoadingDialog();
                                        if (!BaseManager.bmxFinish(bmxErrorCode)) {
                                            toastError(bmxErrorCode);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 设置角标
     */
    private void showTime(String title) {
        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 开始时间
        TextView name = new TextView(this);
        name.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        name.setTextColor(getResources().getColor(R.color.color_black));
        name.setBackgroundColor(getResources().getColor(R.color.color_white));
        name.setText(getString(R.string.set_push_start_time));
        ll.addView(name, textP);

        final EditText editName = buildNumEdit(true);
        ll.addView(editName);

        // 结束时间
        TextView desc = new TextView(this);
        desc.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        desc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        desc.setTextColor(getResources().getColor(R.color.color_black));
        desc.setBackgroundColor(getResources().getColor(R.color.color_white));
        desc.setText(getString(R.string.set_push_end_time));
        ll.addView(desc, textP);

        final EditText editDesc = buildNumEdit(true);
        ll.addView(editDesc);

        DialogUtils.getInstance().showCustomDialog(this, ll, title,
                getString(R.string.confirm), getString(R.string.cancel),
                new CommonCustomDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener() {
                        String startTime = editName.getEditableText().toString().trim();
                        String endTime = editDesc.getEditableText().toString().trim();
                        if (!TextUtils.isEmpty(startTime) && !TextUtils.isEmpty(endTime)) {
                            showLoadingDialog(true);
                            BMXCallBack callBack = bmxErrorCode -> {
                                dismissLoadingDialog();
                                if (!BaseManager.bmxFinish(bmxErrorCode)) {
                                    toastError(bmxErrorCode);
                                }
                            };
                            if (TextUtils.equals(title, getString(R.string.set_push_time))) {
                                PushManager.getInstance().setPushTime(Integer.valueOf(startTime),
                                        Integer.valueOf(endTime), callBack);
                            } else if (TextUtils.equals(title,
                                    getString(R.string.set_push_silence_time))) {
                                PushManager.getInstance().setSilenceTime(Integer.valueOf(startTime),
                                        Integer.valueOf(endTime), callBack);
                            }
                        }
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 删除通知
     */
    private void showDelNotification() {
        DialogUtils.getInstance().showEditDialog(this, getString(R.string.set_push_del_notification),
                getString(R.string.confirm), getString(R.string.cancel), true,
                new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        if (!TextUtils.isEmpty(content)) {
                            PushManager.getInstance().clearNotification(Long.valueOf(content));
                        }
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 清除通知
     */
    private void showClearNotification() {
        DialogUtils.getInstance().showDialog(this, getString(R.string.set_push_clear_notification),
                "", getString(R.string.confirm), getString(R.string.cancel),
                new CommonDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener() {
                        PushManager.getInstance().clearAllNotifications();
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 清除通知
     */
    private void showSendMessage() {
        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 接受人
        TextView name = new TextView(this);
        name.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        name.setTextColor(getResources().getColor(R.color.color_black));
        name.setBackgroundColor(getResources().getColor(R.color.color_white));
        name.setText(getString(R.string.set_push_msg_receiver));
        ll.addView(name, textP);

        final EditText editName = buildNumEdit(false);
        ll.addView(editName);

        // 内容
        TextView desc = new TextView(this);
        desc.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        desc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        desc.setTextColor(getResources().getColor(R.color.color_black));
        desc.setBackgroundColor(getResources().getColor(R.color.color_white));
        desc.setText(getString(R.string.set_push_msg_content));
        ll.addView(desc, textP);

        final EditText editDesc = buildNumEdit(false);
        ll.addView(editDesc);

        DialogUtils.getInstance().showCustomDialog(this, ll, getString(R.string.set_push_send_message),
                getString(R.string.confirm), getString(R.string.cancel),
                new CommonCustomDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener() {
                        String startTime = editName.getEditableText().toString().trim();
                        String endTime = editDesc.getEditableText().toString().trim();
                        if (!TextUtils.isEmpty(startTime) && !TextUtils.isEmpty(endTime)) {
                            long from = SharePreferenceUtils.getInstance().getUserId();
                            long to = Long.valueOf(startTime);
                            BMXMessage msg = BMXMessage.createMessage(from, to,
                                    BMXMessage.MessageType.Single, to, endTime);
                            PushManager.getInstance().sendMessage(msg);
                        }
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }
    
    private EditText buildNumEdit(boolean number) {
        EditText editDesc = new EditText(this);
        editDesc.setBackgroundResource(R.drawable.common_edit_corner_bg);
        editDesc.setPadding(ScreenUtils.dp2px(5), 0, ScreenUtils.dp2px(5), 0);
        editDesc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        editDesc.setTextColor(getResources().getColor(R.color.color_black));
        editDesc.setMinHeight(ScreenUtils.dp2px(40));
//        if (number) {
//            editDesc.setInputType(InputType.TYPE_CLASS_NUMBER);
//        }
        LinearLayout.LayoutParams editP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editP.setMargins(ScreenUtils.dp2px(10), ScreenUtils.dp2px(5), ScreenUtils.dp2px(10),
                ScreenUtils.dp2px(5));
        editDesc.setLayoutParams(editP);
        return editDesc;
    }

    private void toastError(BMXErrorCode e) {
        String error = e != null ? e.name() : "网络异常";
        ToastUtil.showTextViewPrompt(error);
    }

    private String buildOperateId() {
        return UUID.randomUUID().toString();
    }
}
