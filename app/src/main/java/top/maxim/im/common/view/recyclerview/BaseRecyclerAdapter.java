
package top.maxim.im.common.view.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Description : 基础adapter Created by Mango on 2018/11/05.
 */
public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> {

    /**
     * 上下文
     */
    protected Context mContext;

    /**
     * 数据
     */
    private List<T> mList = new ArrayList();

    /**
     * 点击
     */
    private AdapterView.OnItemClickListener onItemClickListener;

    /**
     * 长按点击
     */
    private AdapterView.OnItemLongClickListener onItemLongClickListener;

    public BaseRecyclerAdapter(Context context) {
        mContext = context;
    }

    public BaseRecyclerAdapter(Context context, List<T> list) {
        this(context);
        if (list != null) {
            mList.addAll(list);
        }
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    /**
     * 获取数据
     * 
     * @param position 位置
     * @return t
     */
    public T getItem(int position) {
        if (position >= 0 && mList.size() > position) {
            return mList.get(position);
        }
        return null;
    }

    public List<T> getList() {
        return mList;
    }

    /**
     * 添加数据
     * 
     * @param t 数据
     * @param position 位置
     */
    public void addItem(T t, int position) {
        if (t != null) {
            mList.add(position, t);
            notifyItemInserted(position);
        }
    }

    /**
     * 添加数据
     *
     * @param list 数据
     * @param position 位置
     */
    public void addList(List<T> list, int position) {
        if (list != null && list.size() > 0) {
            mList.addAll(position, list);
            notifyDataSetChanged();
        }
    }

    /**
     * 添加列表到底部
     * 
     * @param list 数据
     */
    public void addListAtEnd(List<T> list) {
        if (list != null && list.size() > 0) {
            mList.addAll(list);
            notifyItemRangeInserted(mList.size() - 1, list.size());
        }
    }

    /**
     * 添加底部刷新全部
     * 
     * @param list 数据
     */
    public void addListAtEndAndNotify(List<T> list) {
        if (list != null && list.size() > 0) {
            mList.addAll(list);
            notifyDataSetChanged();
        }
    }

    /**
     * 添加数据到底部
     * 
     * @param t 数据
     */
    public void addListBeanAtEnd(T t) {
        if (t != null) {
            mList.add(t);
            notifyItemInserted(mList.size() - 1);
        }
    }

    /**
     * 添加列表到头部
     * 
     * @param list 数据
     */
    public void addListAtStart(List<T> list) {
        if (list != null && list.size() > 0) {
            mList.addAll(0, list);
            notifyDataSetChanged();
        }
    }

    /**
     * 添加列表到头部
     * 
     * @param t 数据
     */
    public void addListBeanAtStart(T t) {
        if (t != null) {
            mList.add(0, t);
            notifyItemInserted(0);
        }
    }

    public void remove(int position) {
        if (position >= 0 && mList != null && position <= mList.size()) {
            mList.remove(position);
            notifyDataSetChanged();
        }
    }

    public void removeAll() {
        if (mList != null) {
            mList.clear();
            notifyDataSetChanged();
        }
    }

    public void replaceItem(T t, int position) {
        if (t != null) {
            mList.set(position, t);
            notifyItemChanged(position);
        }
    }

    public void replaceList(List<T> list) {
        mList.clear();
        if (list != null) {
            mList.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(
            AdapterView.OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public AdapterView.OnItemLongClickListener getOnItemLongClickListener() {
        return onItemLongClickListener;
    }

    protected abstract int onCreateViewById(int viewType);

    protected abstract void onBindHolder(BaseViewHolder holder, int position);

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BaseViewHolder(
                LayoutInflater.from(mContext).inflate(onCreateViewById(viewType), parent, false));
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, int position) {
        onBindHolder(holder, position);
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onItemClickListener.onItemClick(null, v, holder.getLayoutPosition(),
                            holder.getItemId());
                }
            });
        }
        if (onItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemLongClickListener.onItemLongClick(null, v, holder.getLayoutPosition(),
                            holder.getItemId());
                    return false;
                }
            });
        }
    }
}
