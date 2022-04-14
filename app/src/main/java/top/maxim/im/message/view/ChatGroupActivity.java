
package top.maxim.im.message.view;

import android.content.Context;

import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXMessage;
import top.maxim.im.R;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.message.contract.ChatGroupContract;
import top.maxim.im.message.presenter.ChatGroupPresenter;

/**
 * Description : 群聊activity Created by Mango on 2018/11/06.
 */
public class ChatGroupActivity extends ChatBaseActivity implements ChatGroupContract.View {

    private ChatGroupContract.Presenter mPresenter;

    @Override
    protected void onHeaderRightClick() {
        ChatGroupOperateActivity.startGroupOperateActivity(this, mChatId);
    }

    @Override
    protected void initChatInfo(long myUserId, long chatId) {
        mPresenter = new ChatGroupPresenter(this);
        mPresenter.setChatInfo(BMXMessage.MessageType.Group, myUserId, chatId);
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
    public Context getContext() {
        return this;
    }
}
