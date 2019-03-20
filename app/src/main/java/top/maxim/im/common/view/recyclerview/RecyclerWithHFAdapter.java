
package top.maxim.im.common.view.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.List;

/**
 * Description : 带header footer的adapter Created by Mango on 2018/11/24.
 */
public abstract class RecyclerWithHFAdapter<T> extends BaseRecyclerAdapter<T> {

    protected int TYPE_NORMAL = 0;

    protected int TYPE_HEADER = -1;

    protected int TYPE_FOOTER = -2;

    private View mHeaderView;

    private View mFooterView;

    private View.OnClickListener mFooterViewClickListener;

    private View.OnClickListener mHeaderViewClickListener;

    private AdapterView.OnItemClickListener onItemClickListener;

    private AdapterView.OnItemLongClickListener onItemLongClickListener;

    public RecyclerWithHFAdapter(Context context) {
        super(context);
    }

    public RecyclerWithHFAdapter(Context context, List<T> list) {
        super(context, list);
    }

    /**
     * 添加footerView
     * 
     * @param footerView
     */
    public void addFooterView(View footerView) {
        mFooterView = footerView;
        mFooterView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /**
     * 添加headerView
     * 
     * @param headerView
     */
    public void addHeaderView(View headerView) {
        mHeaderView = headerView;
        mHeaderView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public int getItemCount() {
        int count = super.getItemCount();
        if (mHeaderView != null) {
            count++;
        }
        if (mFooterView != null) {
            count++;
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null && mFooterView == null) {
            return TYPE_NORMAL;
        }
        if (position == 0 && mHeaderView != null) {
            return TYPE_HEADER;
        }
        if (position == getItemCount() - 1 && mFooterView != null) {
            return TYPE_FOOTER;
        }
        return TYPE_NORMAL;
    }

    public void setFooterViewClickListener(View.OnClickListener onClickListener) {
        this.mFooterViewClickListener = onClickListener;
    }

    public void setHeaderViewClickListener(View.OnClickListener onClickListener) {
        this.mHeaderViewClickListener = onClickListener;
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

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER && mHeaderView != null) {
            return new BaseViewHolder(mHeaderView);
        }
        if (viewType == TYPE_FOOTER && mFooterView != null) {
            return new BaseViewHolder(mFooterView);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, final int position) {
        if ((getItemViewType(position) == TYPE_HEADER)
                || (getItemViewType(position) == TYPE_FOOTER)) {
            if (mHeaderView != null && mHeaderViewClickListener != null) {
                mHeaderView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mHeaderViewClickListener.onClick(view);
                    }
                });
                return;
            }
            if (mFooterView != null && mFooterViewClickListener != null) {
                mFooterView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mFooterViewClickListener.onClick(view);
                    }
                });
            }
            return;
        }
        final int pos = mHeaderView != null ? position - 1 : position;
        onBindHolder(holder, pos);
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onItemClickListener.onItemClick(null, v, pos, holder.getItemId());
                }
            });
        }
        if (onItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    onItemLongClickListener.onItemLongClick(null, v, pos, holder.getItemId());
                    return false;
                }
            });
        }
    }
}
