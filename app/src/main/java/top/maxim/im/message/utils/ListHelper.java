
package top.maxim.im.message.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Description : recyclerView数据帮助类 Created by Mango on 2018/11/18.
 */
public class ListHelper<T> {

    static final String TAG = ListHelper.class.getSimpleName();

    RecyclerView mView;

    RecyclerView.Adapter mListAdapter;

    LinearLayoutManager mLayoutManager;

    List<T> mList = new ArrayList<>();

    public ListHelper(RecyclerView view, RecyclerView.Adapter adapter) {
        if (view == null || adapter == null) {
            Log.i(TAG, "MessageListView or adapter cannot be null");
            return;
        }
        mView = view;
        mListAdapter = adapter;
        mLayoutManager = (LinearLayoutManager)view.getLayoutManager();
    }

    /**
     * 添加数据
     *
     * @param t 数据
     */
    void add(T t) {
        if (t != null) {
            mList.add(t);
            mListAdapter.notifyItemInserted(mList.size() - 1);
        }
    }

    /**
     * 添加数据
     *
     * @param t 数据
     */
    void add(int position, T t) {
        if (t != null) {
            mList.add(position, t);
            mListAdapter.notifyItemInserted(position);
        }
    }

    /**
     * 添加数据列表
     *
     * @param list 列表
     */
    void add(List<T> list) {
        if (list != null && list.size() > 0) {
            mList.addAll(list);
            mListAdapter.notifyItemRangeInserted(mList.size() - 1, list.size());
        }
    }

    /**
     * 添加数据列表
     *
     * @param index 插入的位置
     * @param list 列表
     */
    void add(int index, List<T> list) {
        if (list != null && list.size() > 0) {
            mList.addAll(index, list);
            mListAdapter.notifyItemRangeInserted(index, list.size());
        }
    }

    /**
     * 获取数据列表
     * 
     * @return List<T>
     */
    public List<T> getList() {
        return mList;
    }

    /**
     * 获取数据
     * 
     * @return List<T>
     */
    T getDataByPosition(int position) {
        if (mList != null && mList.size() > 0) {
            if (mList.size() > position) {
                return mList.get(position);
            }
        }
        return null;
    }

    /**
     * 第一条数据
     * 
     * @return T
     */
    T getFirstData() {
        T bean = null;
        if (mList != null && mList.size() > 0) {
            bean = mList.get(0);
        }
        return bean;
    }

    /**
     * 最后一条数据
     * 
     * @return T
     */
    public T getLastData() {
        T bean = null;
        if (mList != null && mList.size() > 0) {
            bean = mList.get(mList.size() - 1);
        }
        return bean;
    }

    /**
     * 删除
     * 
     * @param i 数据位置
     */
    void remove(int i) {
        mList.remove(i);
        mListAdapter.notifyDataSetChanged();
    }

    /**
     * 删除
     *
     * @param t 消息体
     */
    void remove(T t) {
        if (t != null) {
            mList.remove(t);
            mListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 删除
     *
     * @param beans 消息集合
     */
    public void remove(List<T> beans) {
        if (beans != null && mList.removeAll(beans)) {
            mListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 清除当前列表消息
     */
    void clear() {
        mList.clear();
        mListAdapter.notifyDataSetChanged();
    }

    /**
     * 更新消息状态
     * 
     * @param position 位置
     * @param t 数据
     */
    void update(int position, T t) {
        mList.set(position, t);
        notifyAdapterItem(position);
    }

    /**
     * 滚动到某个位置
     */
    public void setSelection(int position) {
        if (position < 0) {
            position = 0;
        }
        mView.scrollToPosition(position);
    }

    /**
     * 滑动到某个位置 带有滑动效果
     */
    public void scrollSelection(int position) {
        if (position < 0) {
            position = 0;
        }
        mView.smoothScrollToPosition(position);
    }

    /**
     * 滑动到某个位置 带有滑动效果
     */
    public void scrollSelection(int position, int offset) {
        if (position < 0) {
            position = 0;
        }
        // 次滑动不会触发recyclerView的滑动监听
        mLayoutManager.scrollToPositionWithOffset(position, offset);
    }

    public void updateListView() {
        if (mListAdapter != null) {
            mListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 数据总数
     * 
     * @return int
     */
    public int getCount() {
        return mList != null ? mList.size() : 0;
    }

    /**
     * 更新整个view
     */
    void notifyAdapter() {
        mListAdapter.notifyDataSetChanged();
    }

    /**
     * 更新单个view
     */
    void notifyAdapterItem(int position) {
        mListAdapter.notifyItemChanged(position);
    }

    /**
     * 获取当前页面第一条消息的position
     *
     * @return int
     */
    public int getFirstVisiblePosition() {
        return mLayoutManager.findFirstVisibleItemPosition();
    }

    /**
     * 获取当前页面最后一条消息的position
     *
     * @return int
     */
    public int getLastVisiblePosition() {
        return mLayoutManager.findLastVisibleItemPosition();
    }

}
