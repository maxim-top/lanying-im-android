
package top.maxim.im.message.view;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import java.util.Map;

import im.floo.floolib.BMXGroup;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.view.Header;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : 转移群主 Created by Mango on 2018/11/25.
 */
public class ChatGroupTransActivity extends ChatGroupListMemberActivity {

    public static void startGroupTransActivity(Context context, long groupId) {
        Intent intent = new Intent(context, ChatGroupTransActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, groupId);
        intent.putExtra(CHOOSE, true);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.group_trans);
        builder.setRightText(R.string.confirm, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long id = 0;
                for (Map.Entry<Long, Boolean> entry : mSelected.entrySet()) {
                    if (entry.getValue()) {
                        id = entry.getKey();
                        break;
                    }
                }
                transOwer(id);
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
                mSelected.clear();
                mSelected.put(mId, true);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void transOwer(long uid) {
        showLoadingDialog(true);
        GroupManager.getInstance().transferOwner(mGroup, uid, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                finish();
            } else {
                toastError(bmxErrorCode);
            }
        });
    }
}
