
package top.maxim.im.message.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupServiceListener;
import im.floo.floolib.BMXMessage;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.message.contract.ChatGroupContract;
import top.maxim.im.message.presenter.ChatGroupPresenter;

/**
 * Description : 群聊activity Created by Mango on 2018/11/06.
 */
public class ChatGroupActivity extends ChatBaseActivity implements ChatGroupContract.View {

    private ChatGroupContract.Presenter mPresenter;

    private long mMyUserId;

    private long mChatId;

    @Override
    protected void onHeaderRightClick() {
        ChatGroupOperateActivity.startGroupOperateActivity(this, mChatId);
    }

    @Override
    protected void initChatInfo(long myUserId, long chatId) {
        mMyUserId = myUserId;
        mChatId = chatId;
        mPresenter = new ChatGroupPresenter(this);
    }

    @Override
    protected void setViewListener() {
        super.setViewListener();
    }

    @Override
    public void onChatAtMember() {
        // 跳转@
        if (mPresenter != null) {
            mPresenter.onChatAtMember();
        }
    }

    @Override
    public void showReadAck(boolean readAck) {
        if (mPresenter != null) {
            mPresenter.setGroupAck(readAck);
        }
        super.showReadAck(readAck);

    }

    @Override
    public void showJoinGroupDialog(BMXGroup group) {
        if (group == null) {
            return;
        }
        DialogUtils.getInstance().showEditDialog(this, getString(R.string.join_group_chat),
                getString(R.string.confirm), getString(R.string.cancel),
                new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        if (mPresenter != null) {
                            mPresenter.joinGroup(group, content);
                        }
                    }

                    @Override
                    public void onCancelListener() {
                        finish();
                    }
                });
    }

    @Override
    public void showLoading(boolean cancel) {
        showLoadingDialog(cancel);
    }

    @Override
    public void cancelLoading() {
        dismissLoadingDialog();
    }

    @Override
    public void enableInputBar(boolean enable, boolean isMuteAll) {
        super.enableInputBar(enable, isMuteAll);
    }

    @Override
    public Context getContext() {
        return this;
    }

    public void onResume() {
        super.onResume();
        mPresenter.setChatInfo(BMXMessage.MessageType.Group, mMyUserId, mChatId);
    }
}
