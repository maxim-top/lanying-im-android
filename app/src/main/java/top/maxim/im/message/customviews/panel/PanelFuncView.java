
package top.maxim.im.message.customviews.panel;

import android.content.Context;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import top.maxim.im.R;
import top.maxim.im.common.view.recyclerview.BaseRecyclerAdapter;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;

/**
 * Description : 功能panel Created by Mango on 2018/11/07.
 */
public class PanelFuncView implements IPanel {
    
    private Context mContext;

    private OnPanelItemListener mItemListener;

    private RecyclerView mRecyclerView;
    
    private FuncAdapter mAdapter;

    private List<FuncItem> items = new ArrayList<>();

    public PanelFuncView(Context context) {
        mContext = context;
        FuncItem photo = new FuncItem();
        photo.text = "相册";
        photo.resId = R.drawable.panel_normal_picture;
        items.add(photo);
        FuncItem picture = new FuncItem();
        picture.text = "拍照";
        picture.resId = R.drawable.panel_normal_camera;
        items.add(picture);
        FuncItem video = new FuncItem();
        video.text = "视频";
        video.resId = R.drawable.panel_normal_video;
        items.add(video);
        FuncItem file = new FuncItem();
        file.text = "文件";
        file.resId = R.drawable.panel_normal_file;
        items.add(file);
        FuncItem location = new FuncItem();
        location.text = "位置";
        location.resId = R.drawable.panel_normal_location;
        items.add(location);
    }

    @Override
    public View obtainView(OnPanelItemListener itemListener) {
        mItemListener = itemListener;
        View view = View.inflate(mContext, R.layout.panel_func, null);
        mRecyclerView = view.findViewById(R.id.rcy_panel_func);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 4));
        mRecyclerView.setAdapter(mAdapter = new FuncAdapter(mContext, items));
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FuncItem item = items.get(position);
                if (mItemListener != null) {
                    mItemListener.onPanelItemClick(PanelFactoryImp.TYPE_FUNCTION, item.text);
                }
            }
        });
        return view;
    }
    
    private class FuncAdapter extends BaseRecyclerAdapter<FuncItem> {

        public FuncAdapter(Context context, List<FuncItem> list) {
            super(context, list);
        }

        @Override
        protected int onCreateViewById(int viewType) {
            return R.layout.item_panel;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            ImageView icon = holder.findViewById(R.id.panel_view);
            TextView text = holder.findViewById(R.id.panel_text);
            FuncItem item = items.get(position);

            icon.setImageResource(item.resId);
            text.setText(item.text);
        }
    }
    
    private class FuncItem {
        String text;

        int resId;

    }
}
