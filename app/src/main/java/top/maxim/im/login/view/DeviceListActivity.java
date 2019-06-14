
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import im.floo.floolib.BMXDevice;
import im.floo.floolib.BMXDeviceList;
import im.floo.floolib.BMXErrorCode;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;
import top.maxim.im.common.view.recyclerview.RecyclerWithHFAdapter;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : 多设备列表 Created by Mango on 2018/11/06
 */
public class DeviceListActivity extends BaseTitleActivity {

    private RecyclerView mRecycler;

    private DeviceAdapter mAdapter;

    private BMXDeviceList deviceList = new BMXDeviceList();

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
        View view = View.inflate(this, R.layout.fragment_contact, null);
        mRecycler = view.findViewById(R.id.contact_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.addItemDecoration(new DividerItemDecoration(this, R.color.guide_divider));
        mAdapter = new DeviceAdapter(this);
        mRecycler.setAdapter(mAdapter);
        return view;
    }

    private void deleteDevice(final int deviceSn) {
        showLoadingDialog(true);
        Observable.just("").map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return UserManager.getInstance().deleteDevice(deviceSn);
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
                        String error = e != null ? e.getMessage() : "网络错误";
                        ToastUtil.showTextViewPrompt(error);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        init();
                    }
                });
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        init();
    }

    protected void init() {
        if (deviceList != null && !deviceList.isEmpty()) {
            deviceList.clear();
        }
        showLoadingDialog(true);
        Observable.just("").map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return UserManager.getInstance().getDeviceList(deviceList);
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
                        String error = e != null ? e.getMessage() : "网络错误";
                        ToastUtil.showTextViewPrompt(error);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        bindData();
                    }
                });
    }

    private void bindData() {
        List<BMXDevice> devices = new ArrayList<>();
        for (int i = 0; i < deviceList.size(); i++) {
            devices.add(deviceList.get(i));
        }
        mAdapter.replaceList(devices);
    }

    /**
     * 展示设备adapter
     */
    protected class DeviceAdapter extends RecyclerWithHFAdapter<BMXDevice> {

        public DeviceAdapter(Context context) {
            super(context);
        }

        @Override
        protected int onCreateViewById(int viewType) {
            return R.layout.item_device_view;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            TextView title = holder.findViewById(R.id.tv_device);
            TextView quit = holder.findViewById(R.id.tv_quit);

            final BMXDevice device = getItem(position);
            if (device == null) {
                return;
            }
            // 退出
            quit.setOnClickListener(v ->  
            deleteDevice(device.deviceSN()));
            long platform = device.platform();
            // 当前设备没有退出按钮
            boolean isCurrent = device.isCurrentDevice();
            quit.setVisibility(isCurrent ? View.GONE : View.VISIBLE);
            String deviceTitle = "";
            if (platform == MessageConfig.DeviceType.IOS) {
                deviceTitle = "iOS";
            } else if (platform == MessageConfig.DeviceType.Android) {
                deviceTitle = "Android";
            } else if (platform == MessageConfig.DeviceType.WIN) {
                deviceTitle = "Windows";
            } else if (platform == MessageConfig.DeviceType.MAC) {
                deviceTitle = "Mac";
            } else if (platform == MessageConfig.DeviceType.LINUX) {
                deviceTitle = "Linux";
            } else if (platform == MessageConfig.DeviceType.WEB) {
                deviceTitle = "Web";
            }
            title.setText(deviceTitle);
        }
    }
}
