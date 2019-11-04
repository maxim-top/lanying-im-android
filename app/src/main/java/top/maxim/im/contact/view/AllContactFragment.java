
package top.maxim.im.contact.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupService;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.ListOfLongLong;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.base.BaseTitleFragment;
import top.maxim.im.common.bean.RightMenuBean;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonCustomDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ItemLineSwitch;
import top.maxim.im.common.view.RightMenuPopWindow;
import top.maxim.im.common.view.ViewPagerFixed;
import top.maxim.im.group.view.GroupListActivity;
import top.maxim.im.message.view.ChatBaseActivity;
import top.maxim.im.message.view.ChatGroupListMemberActivity;
import top.maxim.im.scan.view.ScannerActivity;

/**
 * Description : 通讯录 Created by Mango on 2018/11/06
 */
public class AllContactFragment extends BaseTitleFragment {

    private TabLayout mTabLayout;

    private ViewPagerFixed mViewPager;

    private int mIndex;

    private ContactFragment mContactFragment;

    private GroupListActivity mGroupFragment;

    private SupportFragment mSupportFragment;

    private BaseTitleFragment[] mFragments;

    private String[] mTabTitles;

    public @interface RightMenuFlag {

        int CREATE_GROUP = 1;

        int ADD_CONTACT = 2;

        int SCAN = 3;
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(getActivity(), headerContainer);
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        hideHeader();
        View view = View.inflate(getActivity(), R.layout.fragment_all_contact, null);
        mViewPager = view.findViewById(R.id.contact_view_pager);
        mTabLayout = view.findViewById(R.id.tablayout);
        mFragments = new BaseTitleFragment[] {
                mContactFragment = new ContactFragment(), mGroupFragment = new GroupListActivity(),
                mSupportFragment = new SupportFragment()
        };
        mTabTitles = new String[] {
                "好友", "群组", "支持"
        };
        PagerAdapter pagerAdapter = new MyViewPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(pagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(mIndex).select();

        for (int i = 0; i < mFragments.length; i++) {
            // 获取每一个tab对象
            TabLayout.Tab tabAt = mTabLayout.getTabAt(i);
            // 将每一个条目设置我们自定义的视图
            tabAt.setCustomView(R.layout.tab_contact_text);
            TextView textView = tabAt.getCustomView().findViewById(R.id.tv_tab);
            // 默认选中第一个
            updateTabView(tabAt, mIndex == i);
            textView.setText(mTabTitles[i]);// 设置tab上的文字
        }
        initTabLayoutClick();
        view.findViewById(R.id.iv_contact_add).setOnClickListener(v -> onNavigationAddClick(v));
        return view;
    }

    /**
     * 设置tableLayout的点击监听事件
     */
    private void initTabLayoutClick() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 定义方法，判断是否选中
                updateTabView(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 定义方法，判断是否选中
                updateTabView(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    /**
     * 用来改变tabLayout选中后的字体大小及颜色
     * 
     * @param tab
     * @param isSelect
     */
    private void updateTabView(TabLayout.Tab tab, boolean isSelect) {
        // 找到自定义视图的控件ID
        TextView textView = tab.getCustomView().findViewById(R.id.tv_tab);
        View indicator = tab.getCustomView().findViewById(R.id.tab_item_indicator);
        if (isSelect) {
            textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            indicator.setVisibility(View.VISIBLE);
        } else {
            textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            indicator.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 点击跳转打开 app helper
     */
    private void onNavigationAddClick(View view) {
        Context context = getContext();
        if (view == null || context == null) {
            return;
        }
        // 右侧功能菜单
        RightMenuPopWindow rightMenuPopWindow = new RightMenuPopWindow(getActivity(), view);
        rightMenuPopWindow.setMenuData(buildMenu());
        rightMenuPopWindow.setOnMenuClickListener(bean -> {
            if (bean == null) {
                return;
            }
            switch (bean.getFlag()) {
                case RightMenuFlag.ADD_CONTACT:
                    ContactSearchActivity.openRosterSearch(getActivity());
                    break;
                case RightMenuFlag.CREATE_GROUP:
                    RosterChooseActivity.startRosterListActivity(getActivity(), true, true,
                            GroupListActivity.CHOOSE_MEMBER_CODE);
                    break;
                case RightMenuFlag.SCAN:
                    ScannerActivity.openScan(getActivity());
                    break;
                default:
                    break;
            }
        });
        rightMenuPopWindow.showMenu();
    }

    private List<RightMenuBean> buildMenu() {
        List<RightMenuBean> beans = new ArrayList<>();
        // 添加好友
        RightMenuBean add = new RightMenuBean();
        add.setFlag(RightMenuFlag.ADD_CONTACT);
        add.setTitle(getString(R.string.pop_add_friend));
        beans.add(add);
        // 创建群组
        RightMenuBean createGroup = new RightMenuBean();
        createGroup.setFlag(RightMenuFlag.CREATE_GROUP);
        createGroup.setTitle(getString(R.string.pop_create_group));
        beans.add(createGroup);
        // 扫一扫
        RightMenuBean scan = new RightMenuBean();
        scan.setFlag(RightMenuFlag.SCAN);
        scan.setTitle(getString(R.string.pop_scan));
        beans.add(scan);
        return beans;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFragments != null) {
            mFragments[mIndex].onShow();
        }
    }

    @Override
    public void onDestroyView() {
        setNull(mTabLayout);
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GroupListActivity.CHOOSE_MEMBER_CODE && resultCode == Activity.RESULT_OK
                && data != null) {
            List<Long> chooseList = (List<Long>)data
                    .getSerializableExtra(ChatGroupListMemberActivity.CHOOSE_DATA);
            ListOfLongLong members = new ListOfLongLong();
            if (chooseList != null && chooseList.size() > 0) {
                for (Long id : chooseList) {
                    members.add(id);
                }
            }
            showCreateGroup(members);
        }
    }

    /**
     * 创建群聊
     */
    private void showCreateGroup(final ListOfLongLong members) {
        final LinearLayout ll = new LinearLayout(getActivity());
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams editP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editP.setMargins(ScreenUtils.dp2px(10), ScreenUtils.dp2px(5), ScreenUtils.dp2px(10),
                ScreenUtils.dp2px(5));
        // 名称
        TextView name = new TextView(getActivity());
        name.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        name.setTextColor(getResources().getColor(R.color.color_black));
        name.setBackgroundColor(getResources().getColor(R.color.color_white));
        name.setText(getString(R.string.group_name));
        ll.addView(name, textP);

        final EditText editName = new EditText(getActivity());
        editName.setBackgroundResource(R.drawable.common_edit_corner_bg);
        editName.setPadding(ScreenUtils.dp2px(5), 0, ScreenUtils.dp2px(5), 0);
        editName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        editName.setTextColor(getResources().getColor(R.color.color_black));
        editName.setMinHeight(ScreenUtils.dp2px(40));
        ll.addView(editName, editP);

        // 描述
        TextView desc = new TextView(getActivity());
        desc.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        desc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        desc.setTextColor(getResources().getColor(R.color.color_black));
        desc.setBackgroundColor(getResources().getColor(R.color.color_white));
        desc.setText(getString(R.string.group_desc));
        ll.addView(desc, textP);

        final EditText editDesc = new EditText(getActivity());
        editDesc.setBackgroundResource(R.drawable.common_edit_corner_bg);
        editDesc.setPadding(ScreenUtils.dp2px(5), 0, ScreenUtils.dp2px(5), 0);
        editDesc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        editDesc.setTextColor(getResources().getColor(R.color.color_black));
        editDesc.setMinHeight(ScreenUtils.dp2px(40));
        ll.addView(editDesc, editP);

        // 公开
        final ItemLineSwitch.Builder isPublic = new ItemLineSwitch.Builder(getActivity())
                .setLeftText("是否公开").setMarginTop(ScreenUtils.dp2px(15))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {

                    }
                });
        ll.addView(isPublic.build(), textP);

        DialogUtils.getInstance().showCustomDialog(getActivity(), ll,
                getString(R.string.create_group), getString(R.string.confirm),
                getString(R.string.cancel), new CommonCustomDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener() {
                        String name = editName.getEditableText().toString().trim();
                        String desc = editDesc.getEditableText().toString().trim();
                        boolean publicCheckStatus = isPublic.getCheckStatus();
                        createGroup(members, name, desc, publicCheckStatus);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 创建群聊
     */
    private void createGroup(ListOfLongLong members, String name, String desc,
            boolean publicCheckStatus) {
        if (TextUtils.isEmpty(name)) {
            ToastUtil.showTextViewPrompt("群聊名称不能为空");
            return;
        }
        BMXGroupService.CreateGroupOptions options = new BMXGroupService.CreateGroupOptions(name,
                desc, publicCheckStatus);
        options.setMMembers(members);
        final BMXGroup group = new BMXGroup();
        showLoadingDialog(true);
        Observable.just(options).map(new Func1<BMXGroupService.CreateGroupOptions, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXGroupService.CreateGroupOptions createGroupOptions) {
                return GroupManager.getInstance().create(createGroupOptions, group);
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
                        String error = e != null ? e.getMessage() : "创建失败";
                        ToastUtil.showTextViewPrompt(error);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        if (mGroupFragment != null) {
                            mGroupFragment.onShow();
                        }
                        ChatBaseActivity.startChatActivity(getActivity(),
                                BMXMessage.MessageType.Group, group.groupId());
                    }
                });
    }

    final class MyViewPagerAdapter extends FragmentPagerAdapter {

        private MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitles[position];
        }

    }
}
