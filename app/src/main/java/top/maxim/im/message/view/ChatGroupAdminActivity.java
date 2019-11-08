
package top.maxim.im.message.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupMemberList;
import im.floo.floolib.ListOfLongLong;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : 群聊管理员 Created by Mango on 2018/11/25.
 */
public class ChatGroupAdminActivity extends ChatGroupListMemberActivity {

    private boolean isEdit = false;

    private int CHOOSE_ADMIN_CODE = 1000;

    public static void startGroupAdminActivity(Context context, long groupId) {
        Intent intent = new Intent(context, ChatGroupAdminActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.group_manager_list);
        builder.setRightText(R.string.edit, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEdit = !isEdit;
                if (isEdit) {
                    mHeader.setRightText(getString(R.string.confirm));
                } else {
                    mHeader.setRightText(getString(R.string.edit));
                    showRemoveAdmin();
                }
                mAdapter.setShowCheck(isEdit);
                mAdapter.notifyDataSetChanged();
            }
        });
        builder.setBackIcon(R.drawable.header_back_icon, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        return builder.build();
    }

    @Override
    protected void setViewListener() {
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BMXGroup.Member member = mAdapter.getItem(position);
                if (member == null) {
                    return;
                }
                long mId = member.getMUid();
                if (!mAdapter.getShowCheck() && mId == MessageConfig.MEMBER_ADD) {
                    ChatGroupListMemberActivity.startGroupMemberListActivity(
                            ChatGroupAdminActivity.this, mGroupId, true, CHOOSE_ADMIN_CODE);
                    return;
                }
                if (mAdapter.getShowCheck() && mId != MessageConfig.MEMBER_ADD) {
                    if (!mSelected.containsKey(mId) || !mSelected.get(mId)) {
                        mSelected.put(mId, true);
                    } else {
                        mSelected.remove(mId);
                    }
                    mAdapter.notifyItemChanged(position);
                }
            }
        });
    }

    @Override
    protected BMXErrorCode initData(BMXGroupMemberList memberList, boolean forceRefresh) {
        return GroupManager.getInstance().getAdmins(mGroup, memberList, forceRefresh);
    }

    @Override
    protected void bindData(BMXGroupMemberList memberList) {
        List<BMXGroup.Member> members = new ArrayList<>();
        if (memberList != null && !memberList.isEmpty()) {
            for (int i = 0; i < memberList.size(); i++) {
                members.add(memberList.get(i));
            }
        }
        BMXGroup.Member add = new BMXGroup.Member(MessageConfig.MEMBER_ADD, "", 0);
        members.add(add);
        mAdapter.replaceList(members);
    }

    private void showRemoveAdmin() {
        final ListOfLongLong admin = new ListOfLongLong();
        for (Map.Entry<Long, Boolean> entry : mSelected.entrySet()) {
            if (entry.getValue()) {
                admin.add(entry.getKey());
            }
        }
        if (admin.isEmpty()) {
            return;
        }
        DialogUtils.getInstance().showEditDialog(this, "移除管理员", getString(R.string.confirm),
                getString(R.string.cancel), new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        removeAdmin(admin, content);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    private void showAddAdmin(final ListOfLongLong admin) {
        if (admin == null || admin.isEmpty()) {
            return;
        }
        DialogUtils.getInstance().showEditDialog(this, "添加管理员", getString(R.string.confirm),
                getString(R.string.cancel), new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        addAdmin(admin, content);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    private void removeAdmin(final ListOfLongLong admin, final String reson) {
        if (admin == null || admin.isEmpty()) {
            return;
        }
        showLoadingDialog(true);
        Observable.just(admin).map(new Func1<ListOfLongLong, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(ListOfLongLong listOfLongLong) {
                return GroupManager.getInstance().removeAdmins(mGroup, admin, reson);
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
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        init();
                    }
                });
    }

    private void addAdmin(final ListOfLongLong admin, final String reson) {
        if (admin == null || admin.isEmpty()) {
            return;
        }
        showLoadingDialog(true);
        Observable.just(admin).map(new Func1<ListOfLongLong, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(ListOfLongLong listOfLongLong) {
                return GroupManager.getInstance().addAdmins(mGroup, admin, reson);
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
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        init();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_ADMIN_CODE && resultCode == RESULT_OK && data != null) {
            List<Long> chooseList = (List<Long>)data
                    .getSerializableExtra(ChatGroupListMemberActivity.CHOOSE_DATA);
            if (chooseList != null && chooseList.size() > 0) {
                ListOfLongLong admin = new ListOfLongLong();
                for (Long id : chooseList) {
                    admin.add(id);
                }
                showAddAdmin(admin);
            }
        }
    }
}
