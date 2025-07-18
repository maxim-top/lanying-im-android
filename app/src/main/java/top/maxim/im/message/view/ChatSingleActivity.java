
package top.maxim.im.message.view;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import im.floo.BMXDataCallBack;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXRosterServiceListener;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.contact.view.RosterDetailActivity;
import top.maxim.im.login.view.AboutUsActivity;
import top.maxim.im.message.contract.ChatSingleContract;
import top.maxim.im.message.customviews.MessageInputBar;
import top.maxim.im.message.presenter.ChatSinglePresenter;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;
import top.maxim.im.message.utils.MessageEvent;

/**
 * Description : 单聊activity Created by Mango on 2018/11/06.
 */
public class ChatSingleActivity extends ChatBaseActivity implements ChatSingleContract.View {

    private ChatSingleContract.Presenter mPresenter;
    private BMXRosterItem mRosterItem = new BMXRosterItem();
    private boolean mIsFriend = false;


    /* roster监听 */
    private BMXRosterServiceListener mRosterListener = new BMXRosterServiceListener() {
        @Override
        public void onFriendAdded(long sponsorId, long recipientId) {
            super.onFriendAdded(sponsorId, recipientId);
            // 添加好友
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initRoster();
                }
            });
        }

        @Override
        public void onFriendRemoved(long sponsorId, long recipientId) {
            super.onFriendRemoved(sponsorId, recipientId);
            // 删除好友
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initRoster();
                }
            });
        }

        @Override
        public void onApplied(long sponsorId, long recipientId, String message) {
            super.onApplied(sponsorId, recipientId, message);
            // 申请
        }

        @Override
        public void onApplicationAccepted(long sponsorId, long recipientId) {
            super.onApplicationAccepted(sponsorId, recipientId);
            // 申请被接受
        }

        @Override
        public void onApplicationDeclined(long sponsorId, long recipientId, String reason) {
            super.onApplicationDeclined(sponsorId, recipientId, reason);
            // 申请被拒绝
        }

        @Override
        public void onBlockListAdded(long sponsorId, long recipientId) {
            super.onBlockListAdded(sponsorId, recipientId);
            // 被加入黑名单
        }

        @Override
        public void onBlockListRemoved(long sponsorId, long recipientId) {
            super.onBlockListRemoved(sponsorId, recipientId);
            // 被移除黑名单
        }

        @Override
        public void onRosterInfoUpdate(BMXRosterItem item) {
            super.onRosterInfoUpdate(item);
            // roster有更新
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initRoster();
                }
            });
        }
    };

    /**
     * 输入框弹出
     */
    private void showAddReason(final long rosterId) {
        DialogUtils.getInstance().showEditDialog(this, getString(R.string.add_friend), getString(R.string.confirm),
                getString(R.string.cancel), new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        addRoster(rosterId, content);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    private void addRoster(long rosterId, final String reason) {
        if (rosterId <= 0) {
            return;
        }
        showLoadingDialog(true);
        RosterManager.getInstance().apply(rosterId, reason, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                ToastUtil.showTextViewPrompt(getString(R.string.add_successful));
                initRoster();
            } else {
                ToastUtil.showTextViewPrompt(CommonUtils.getErrorMessage(bmxErrorCode));
            }
        });
    }

    @Override
    protected void onHeaderRightClick() {
        if (mIsFriend){
            RosterDetailActivity.openRosterDetail(this, mChatId);
        }else{
            showAddReason(mChatId);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        RosterManager.getInstance().addRosterListener(mRosterListener);
        initRoster();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        RosterManager.getInstance().removeRosterListener(mRosterListener);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(MessageEvent messageEvent) {
        if (messageEvent.getMessage().equals("scroll-to-bottom")){
            LinearLayoutManager llm = (LinearLayoutManager) mChatRecyclerView.getLayoutManager();
            int pos = mChatViewHelper.getLastVisiblePosition();
            llm.scrollToPositionWithOffset(pos,-100000);
        }
    }

    @Override
    protected void initChatInfo(long myUserId, long chatId) {
        mPresenter = new ChatSinglePresenter(this);
        mPresenter.setChatInfo(BMXMessage.MessageType.Single, myUserId, chatId);
    }

    private void initRoster() {
        showLoadingDialog(true);
        BMXDataCallBack bmxDataCallBack = (BMXDataCallBack<BMXRosterItem>) (bmxErrorCode, bmxRosterItem) -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                mRosterItem = bmxRosterItem;
                RosterFetcher.getFetcher().putRoster(bmxRosterItem);
                bindRoster();
            } else {
                RosterManager.getInstance().getRosterList(mChatId, false,
                        (bmxErrorCode1, bmxRosterItem1) -> {
                            if (!BaseManager.bmxFinish(bmxErrorCode)) {
                                return;
                            }
                            if (bmxRosterItem1 != null) {
                                mRosterItem = bmxRosterItem1;
                            }
                            bindRoster();
                        });
            }
        };
        RosterManager.getInstance().getRosterList(mChatId, false, bmxDataCallBack);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                RosterManager.getInstance().getRosterList(mChatId, true, bmxDataCallBack);
            }
        }, 200);
    }

    private void bindRoster() {
        // 是否是好友
        BMXRosterItem.RosterRelation rosterRelation = mRosterItem.relation();
        boolean friend = rosterRelation == BMXRosterItem.RosterRelation.Friend;
        mHeader.setRightIcon(friend ? R.drawable.icon_more : R.drawable.icon_add);
        mIsFriend = friend;
    }

    @Override
    public void onTagChanged(int tag) {
        super.onTagChanged(tag);
        if (tag == MessageInputBar.OnInputPanelListener.TAG_OPEN
                || tag == MessageInputBar.OnInputPanelListener.TAG_CLOSE) {
            // 发送输入板状态
            String extension = "";
            try {
                JSONObject object = new JSONObject();
                object.put(MessageConfig.INPUT_STATUS,
                        tag == MessageInputBar.OnInputPanelListener.TAG_OPEN
                                ? MessageConfig.InputStatus.TYING_STATUS
                                : MessageConfig.InputStatus.NOTHING_STATUS);
                extension = object.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (mPresenter != null) {
                mPresenter.sendInputStatus(extension);
            }
        }
    }

    @Override
    protected void setViewListener() {
        super.setViewListener();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void enableInputBar(boolean enable) {
        super.enableInputBar(enable, false);
    }
}
