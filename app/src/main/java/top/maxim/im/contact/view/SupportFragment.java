
package top.maxim.im.contact.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import im.floo.floolib.BMXRosterItem;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.common.base.BaseTitleFragment;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;
import top.maxim.im.common.view.recyclerview.RecyclerWithHFAdapter;
import top.maxim.im.contact.bean.SupportBean;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.net.HttpResponseCallback;

/**
 * Description : 支持 Created by Mango on 2018/11/06
 */
public class SupportFragment extends BaseTitleFragment {

    private RecyclerView mRecycler;

    private SupportAdapter mAdapter;

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(getActivity(), headerContainer);
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        hideHeader();
        View view = View.inflate(getActivity(), R.layout.fragment_contact, null);
        mRecycler = view.findViewById(R.id.contact_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycler
                .addItemDecoration(new DividerItemDecoration(getActivity(), R.color.guide_divider));
        mAdapter = new SupportAdapter(getActivity());
        mRecycler.setAdapter(mAdapter);
        return view;
    }

    @Override
    protected void setViewListener() {
        mAdapter.setOnItemClickListener((parent, view, position, id) -> {
            SupportBean bean = mAdapter.getItem(position);
            if (bean != null) {
                RosterDetailActivity.openRosterDetail(getActivity(), bean.getUser_id());
            }
        });
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        initSupport();
    }

    @Override
    public void onShow() {
        if (mContentView != null) {
            initSupport();
        }
    }

    private void initSupport() {
        AppManager.getInstance().getTokenByName(SharePreferenceUtils.getInstance().getUserName(),
                SharePreferenceUtils.getInstance().getUserPwd(),
                new HttpResponseCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        AppManager.getInstance().getSupportStaff(result,
                                new HttpResponseCallback<List<SupportBean>>() {

                                    @Override
                                    public void onResponse(List<SupportBean> result) {
                                        mAdapter.replaceList(result);
                                    }

                                    @Override
                                    public void onFailure(int errorCode, String errorMsg,
                                            Throwable t) {
                                    }
                                });
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {

                    }
                });
    }

    @Override
    public void onDestroyView() {
        setNull(mRecycler);
        super.onDestroyView();
    }

    private class SupportAdapter extends RecyclerWithHFAdapter<SupportBean> {

        private ImageRequestConfig mConfig;

        public SupportAdapter(Context context) {
            super(context);
            mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
                    .showImageForEmptyUri(R.drawable.default_avatar_icon)
                    .showImageOnFail(R.drawable.default_avatar_icon).cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .showImageOnLoading(R.drawable.default_avatar_icon).build();
        }

        @Override
        protected int onCreateViewById(int viewType) {
            return R.layout.item_contact_view;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            SupportBean bean = getItem(position);
            ShapeImageView avatar = holder.findViewById(R.id.contact_avatar);
            TextView title = holder.findViewById(R.id.contact_title);
            TextView sticky = holder.findViewById(R.id.contact_sticky);
            sticky.setVisibility(View.GONE);
            if (bean == null) {
                return;
            }
            String userName = "", nickName = "";
            userName = bean.getUsername();
            nickName = bean.getNickname();
            if (!TextUtils.isEmpty(nickName)) {
                title.setText(nickName);
            } else if (!TextUtils.isEmpty(userName)) {
                title.setText(userName);
            } else {
                title.setText("");
            }

            BMXRosterItem rosterItem = RosterFetcher.getFetcher().getRoster(bean.getUser_id());
            ChatUtils.getInstance().showRosterAvatar(rosterItem, avatar, mConfig);
        }
    }
}
