
package top.maxim.im.contact.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import top.maxim.im.R;
import top.maxim.im.common.base.BaseTitleFragment;
import top.maxim.im.common.bean.RightMenuBean;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.RightMenuPopWindow;
import top.maxim.im.common.view.ViewPagerFixed;
import top.maxim.im.group.view.GroupListActivity;
import top.maxim.im.scan.view.ScannerActivity;

/**
 * Description : 通讯录 Created by Mango on 2018/11/06
 */
public class AllContactFragment extends BaseTitleFragment {

    private TabLayout mTabLayout;

    private ViewPagerFixed mViewPager;

    private int mIndex;

    private Fragment[] mFragments;

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
        mFragments = new Fragment[] {
                new ContactFragment(), new GroupListActivity()
        };
        mTabTitles = new String[] {
                "好友", "群组"
        };
        PagerAdapter pagerAdapter = new MyViewPagerAdapter(
                getActivity().getSupportFragmentManager());
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
    public void onDestroyView() {
        setNull(mTabLayout);
        super.onDestroyView();
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
