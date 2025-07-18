
package top.maxim.im.message.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupMemberList;
import im.floo.floolib.BMXRosterItem;
import top.maxim.im.R;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.view.Header;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : 群聊成员@ Created by Mango on 2018/11/25.
 */
public class ChatGroupAtActivity extends ChatGroupListMemberActivity {

    private Map<String, String> atMap = new HashMap<>();

    public static void startGroupAtActivity(Context context, long groupId, int requestCode) {
        Intent intent = new Intent(context, ChatGroupAtActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, groupId);
        ((Activity)context).startActivityForResult(intent, requestCode);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.choose_group_member);
        builder.setRightText(R.string.confirm, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(CHOOSE_DATA, (Serializable) atMap);
                setResult(Activity.RESULT_OK, intent);
                finish();
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
    protected View onCreateView() {
        View view = super.onCreateView();
        mAdapter.setShowCheck(true);
        return view;
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
                if (!mAdapter.getShowCheck()) {
                    return;
                }
                if (member.getMUid() == MessageConfig.MEMBER_ADD) {
                    atMap.clear();
                    atMap.put("-1", getString(R.string.all_members));
                    Intent intent = new Intent();
                    intent.putExtra(CHOOSE_DATA, (Serializable) atMap);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                    return;
                }
                BMXRosterItem rosterItem = RosterFetcher.getFetcher().getRoster(member.getMUid());
                String name = CommonUtils.getRosterDisplayName(rosterItem);

                if (!mSelected.containsKey(mId) || !mSelected.get(mId)) {
                    mSelected.put(mId, true);
                    atMap.put(String.valueOf(mId), name);
                } else {
                    mSelected.remove(mId);
                    atMap.remove(String.valueOf(mId));
                }
                mAdapter.notifyItemChanged(position);
            }
        });
    }

    @Override
    protected void bindData(BMXGroupMemberList memberList, boolean upload) {
        List<BMXGroup.Member> members = new ArrayList<>();
        if (memberList != null && !memberList.isEmpty()) {
            for (int i = 0; i < memberList.size(); i++) {
                members.add(memberList.get(i));
            }
        }
        if (upload) {
            int count = mAdapter.getItemCount();
            mAdapter.addList(members, count > 1 ? count - 1 : 0);
        } else {
            BMXGroup.Member add = new BMXGroup.Member(MessageConfig.MEMBER_ADD, getString(R.string.all_members), 0);
            members.add(add);
            mAdapter.replaceList(members);
        }
    }
}
