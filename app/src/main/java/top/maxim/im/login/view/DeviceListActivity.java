
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import im.floo.floolib.BMXDevice;
import im.floo.floolib.BMXDeviceList;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;
import top.maxim.im.common.view.recyclerview.RecyclerWithHFAdapter;
import top.maxim.im.common.view.recyclerview.SwipeRecyclerViewHelper;

/**
 * Description : 多设备列表 Created by Mango on 2018/11/06
 */
public class DeviceListActivity extends BaseTitleActivity {

    private RecyclerView mRecycler;

    private DeviceListAdapter mAdapter;

    private BMXDeviceList deviceList = new BMXDeviceList();
    private List<BMXDevice> mDatas;

    public static void startDeviceActivity(Context context) {
        Intent intent = new Intent(context, DeviceListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.device_list);
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
        init();
        View view = View.inflate(this, R.layout.fragment_contact, null);
        mRecycler = view.findViewById(R.id.contact_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.addItemDecoration(new DividerItemDecoration(this, R.color.guide_divider));
        return view;
    }

    private void deleteDevice(final int deviceSn) {
        showLoadingDialog(true);
        UserManager.getInstance().deleteDevice(deviceSn, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                init();
            } else {
                dismissLoadingDialog();
                String error = bmxErrorCode != null ? bmxErrorCode.name() : getString(R.string.network_error);
                ToastUtil.showTextViewPrompt(error);
            }
        });
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
    }

    protected void init() {
        mDatas = new ArrayList<>();
        if (deviceList != null && !deviceList.isEmpty()) {
            deviceList.clear();
        }
        showLoadingDialog(true);
        UserManager.getInstance().getDeviceList((bmxErrorCode, bmxDeviceList) -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                deviceList = bmxDeviceList;
                bindData();
                mAdapter = new DeviceListAdapter(this, mDatas);
                mRecycler.setAdapter(mAdapter);
            } else {
                String error = bmxErrorCode != null ? bmxErrorCode.name() : getString(R.string.network_error);
                ToastUtil.showTextViewPrompt(error);
            }
        });
    }

    private void bindData() {
        for (int i = 0; i < deviceList.size(); i++) {
            mDatas.add(deviceList.get(i));
        }
    }

    public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.RemindViewHolder>
            implements SwipeRecyclerViewHelper.Callback {

        private Context context;
        private List<BMXDevice> mDatas = new ArrayList<BMXDevice>();

        private RecyclerView mRecyclerView;

        public DeviceListAdapter(Context context, List<BMXDevice> mDatas) {
            this.context = context;
            this.mDatas = mDatas;
        }

        @Override
        public DeviceListAdapter.RemindViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_view, parent, false);
            return new DeviceListAdapter.RemindViewHolder(view);
        }

        /**
         * 将recyclerView绑定Slide事件
         *
         * @param recyclerView
         */
        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            mRecyclerView = recyclerView;
            mRecyclerView.addOnItemTouchListener(new SwipeRecyclerViewHelper(mRecyclerView.getContext(), this));
        }

        @Override
        public void onBindViewHolder(final DeviceListAdapter.RemindViewHolder holder, int position) {
            TextView tvDeviceSN = holder.itemView.findViewById(R.id.tv_device_sn);
            TextView tvDeviceAgent = holder.itemView.findViewById(R.id.tv_device_agent);
            TextView quit = holder.itemView.findViewById(R.id.tv_quit);

            final BMXDevice device = mDatas.get(holder.getAdapterPosition());
            if (device == null) {
                return;
            }

            // 退出
            quit.setOnClickListener(v -> deleteDevice(device.deviceSN()));
            // 当前设备没有退出按钮
            boolean isCurrent = device.isCurrentDevice();
            quit.setVisibility(isCurrent ? View.GONE : View.VISIBLE);

            tvDeviceSN.setText(context.getString(R.string.device_serial_number) + device.deviceSN());
            tvDeviceAgent.setText(TextUtils.isEmpty(device.userAgent()) ? "" : device.userAgent());
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        /**
         * 此方法用来计算水平方向移动的距离
         *
         * @param holder
         * @return
         */
        @Override
        public int getHorizontalRange(RecyclerView.ViewHolder holder) {
            if (holder.itemView instanceof LinearLayout) {
                return 250;
            }
            return 0;
        }

        @Override
        public RecyclerView.ViewHolder getChildViewHolder(View childView) {
            return mRecyclerView.getChildViewHolder(childView);
        }

        @Override
        public View findTargetView(float x, float y) {
            return mRecyclerView.findChildViewUnder(x, y);
        }

        /**
         * 自定义的ViewHolder
         */
        public class RemindViewHolder extends RecyclerView.ViewHolder {

            public RemindViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

}
