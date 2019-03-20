
package top.maxim.im.message.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;

/**
 * Description : 聊天页面recyclerView滑动监听 Created by mango
 */
public abstract class ChatRecyclerScrollListener extends OnScrollListener {

    /* recyclerView管理 */
    private LinearLayoutManager mLayoutManager;

    /* 是否可以加载 */
    private boolean mIsLoading;

    /* 是否可以上拉加载 */
    private boolean mIsUpLoading;

    public ChatRecyclerScrollListener(LinearLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        // SCROLL_STATE_IDLE 停止滑动
        // SCROLL_STATE_DRAGGING 手滑动
        // SCROLL_STATE_SETTLING 松开惯性滑动

        // 只有在可加载 recyclerView停止滚动 当前是第一条数据时候才启动加载
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            if (mIsLoading && mLayoutManager.findFirstVisibleItemPosition() == 0) {
                // 拉取标志置为false 防止在拉取过程中触发
                mIsLoading = false;
                // 向下滑动
                // 首先将第一条数据滚动到完全展示
                mLayoutManager.scrollToPositionWithOffset(0, 0);
                // 计算当前第一条数据偏移量 在加载完成后要做相应的偏移量 保证不会有晃动的现象
                int offset = mLayoutManager.getChildAt(0).getHeight();
                onLoadPullDown(offset);
            } else if (mIsUpLoading && mLayoutManager
                    .findLastVisibleItemPosition() == mLayoutManager.getItemCount() - 1) {
                // 拉取标志置为false 防止在拉取过程中触发
                mIsUpLoading = false;
                // 向上滑动
                // 计算当前最后数据偏移量 在加载完成后要做相应的偏移量 保证不会有晃动的现象
                onLoadPullUp(0);
            }
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
    }

    /**
     * 下拉数据 拉取历史消息
     * 
     * @param offset 拉取的历史消息最后一条偏移量
     */
    protected void onLoadPullDown(int offset) {

    }

    /**
     * 上拉数据 拉取最新消息
     */
    protected void onLoadPullUp(int offset) {

    }

    /**
     * 还原加载
     */
    public final void resetLoadStatus() {
        mIsLoading = true;
    }

    /**
     * 关闭加载
     */
    public final void closeLoading() {
        mIsLoading = false;
    }

    /**
     * 还原加载
     */
    public final void resetUpLoadStatus() {
        mIsUpLoading = true;
    }

    /**
     * 关闭加载
     */
    public final void closeUpLoading() {
        mIsUpLoading = false;
    }
}
