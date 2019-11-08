
package top.maxim.im.message.view;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupSharedFileList;
import im.floo.floolib.FileProgressListener;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.bean.FileBean;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.TimeUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonCustomDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;
import top.maxim.im.common.view.recyclerview.RecyclerWithHFAdapter;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : 群共享列表 Created by Mango on 2018/11/25.
 */
public class ChatGroupShareActivity extends BaseTitleActivity {

    protected RecyclerView mGvGroupMember;

    protected ChatGroupShareAdapter mAdapter;

    protected long mGroupId;

    private boolean isEdit = false;

    protected BMXGroup mGroup = new BMXGroup();

    protected BMXGroupSharedFileList shareList = new BMXGroupSharedFileList();

    protected Map<BMXGroup.SharedFile, Boolean> mSelected = new HashMap<>();

    protected Map<BMXGroup.SharedFile, Boolean> mDownload = new HashMap<>();

    private static final int CHOOSE_FILE_CODE = 1000;

    public static void startGroupShareActivity(Activity context, long groupId) {
        Intent intent = new Intent(context, ChatGroupShareActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.group_share);
        builder.setRightText(R.string.edit, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEdit = !isEdit;
                if (isEdit) {
                    mHeader.setRightText(getString(R.string.confirm));
                } else {
                    removeShare();
                    mHeader.setRightText(getString(R.string.edit));
                }
                mAdapter.setShowCheck(isEdit);
                mAdapter.notifyDataSetChanged();
            }
        });
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
        View view = View.inflate(this, R.layout.chat_group_list_member_view, null);
        mGvGroupMember = view.findViewById(R.id.gv_chat_group_member);
        mGvGroupMember.setLayoutManager(new LinearLayoutManager(this));
        mGvGroupMember.addItemDecoration(new DividerItemDecoration(this, R.color.guide_divider));
        mGvGroupMember.setAdapter(mAdapter = new ChatGroupShareAdapter(this));
        buildFooterView();
        return view;
    }

    private void buildFooterView() {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        View addView = buildAddFooterView(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseFileActivity.openChooseFileActivity(ChatGroupShareActivity.this,
                        CHOOSE_FILE_CODE);
            }
        });
        ll.addView(addView);
        mAdapter.addFooterView(ll);
    }

    /**
     * 设置添加view
     */
    protected View buildAddFooterView(View.OnClickListener listener) {
        View view = View.inflate(this, R.layout.item_group_list_member, null);
        ShapeImageView icon = view.findViewById(R.id.img_icon);
        TextView tvName = view.findViewById(R.id.txt_name);
        tvName.setText("添加");
        icon.setImageResource(R.drawable.default_add_icon);
        CheckBox checkBox = view.findViewById(R.id.cb_choice);
        view.setOnClickListener(listener);
        return view;
    }

    @Override
    protected void setViewListener() {
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BMXGroup.SharedFile file = mAdapter.getItem(position);
                if (file == null) {
                    return;
                }
                if (!mAdapter.getShowCheck()) {
                    openFile(file);
                    return;
                }
                boolean isCheck = mSelected.containsKey(file);
                if (isCheck) {
                    mSelected.remove(file);
                } else {
                    mSelected.clear();
                    mSelected.put(file, true);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void openFile(final BMXGroup.SharedFile file) {
        if (file == null) {
            return;
        }
        if (!TextUtils.isEmpty(file.getMPath()) && new File(file.getMPath()).exists()) {
            // 文件存在
            openFilePreView(file.getMPath());
            mDownload.remove(file);
            return;
        }
        // 文件不存在 下载
        mDownload.put(file, true);
        mAdapter.notifyDataSetChanged();
        Observable.just(file).map(new Func1<BMXGroup.SharedFile, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXGroup.SharedFile file) {
                return GroupManager.getInstance().downloadSharedFile(mGroup, file, new FileProgressListener(){
                    @Override
                    public int onProgressChange(String percent) {
                        Log.i("ChatGroupShareActivity", "onProgressChange:"+ mGroup.groupId() + "-" + percent);
                        return 0;
                    }
                });
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

                    }

                    @Override
                    public void onError(Throwable e) {
                        mDownload.remove(file);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        mDownload.remove(file);
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * 文件预览
     *
     * @param path 路径
     */
    private void openFilePreView(String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = null;
        File file = new File(path);
        if (TextUtils.isEmpty(path) || !file.exists()) {
            ToastUtil.showTextViewPrompt(getResources().getString(R.string.chat_file_not_exit));
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                uri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", file);
            } else {
                uri = Uri.fromFile(file);
            }
        } catch (Exception e) {
            ToastUtil.showTextViewPrompt(getResources().getString(R.string.chat_file_not_exit));
            return;
        }

        intent.setData(uri);
        ComponentName componentName = intent.resolveActivity(getPackageManager());
        if (componentName != null) {
            startActivity(intent);
        } else {
            ToastUtil.showTextViewPrompt(getResources().getString(R.string.chat_file_not_open));
        }
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mGroupId = intent.getLongExtra(MessageConfig.CHAT_ID, 0);
            GroupManager.getInstance().search(mGroupId, mGroup, false);
        }
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        init();
    }

    protected void init() {
        if (mGroup == null) {
            return;
        }
        showLoadingDialog(true);
        Observable.just(mGroup).map(new Func1<BMXGroup, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXGroup group) {
                return initData(true);
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
                        initData(false);
                        bindData();
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        bindData();
                    }
                });
    }

    protected BMXErrorCode initData(boolean forceRefresh) {
        return GroupManager.getInstance().getSharedFilesList(mGroup, shareList, forceRefresh);
    }

    protected void bindData() {
        List<BMXGroup.SharedFile> files = new ArrayList<>();
        for (int i = 0; i < shareList.size(); i++) {
            files.add(shareList.get(i));
        }
        mAdapter.replaceList(files);
    }

    private void removeShare() {
        BMXGroup.SharedFile file = null;
        for (Map.Entry<BMXGroup.SharedFile, Boolean> entry : mSelected.entrySet()) {
            file = entry.getKey();
            if (file != null) {
                break;
            }
        }
        if (file == null) {
            return;
        }
        showLoadingDialog(true);
        Observable.just(file).map(new Func1<BMXGroup.SharedFile, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXGroup.SharedFile file1) {
                return GroupManager.getInstance().removeSharedFile(mGroup, file1);
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

    private void addShare(String filePath, final String displayName, final String extensionName) {
        if (TextUtils.isEmpty(filePath) || !new File(filePath).exists()) {
            return;
        }
        showLoadingDialog(true);
        Observable.just(filePath).map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return GroupManager.getInstance().uploadSharedFile(mGroup, s, displayName,
                        extensionName, new FileProgressListener(){
                            @Override
                            public int onProgressChange(String percent) {
                                Log.i("ChatGroupShareActivity", "onProgressChange:"+ mGroup.groupId() + "-" + percent);
                                return 0;
                            }
                        });
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

    /**
     * 设置上传文件
     */
    private void showSetShareInfo(final String filePath, final String suffix) {
        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams editP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editP.setMargins(ScreenUtils.dp2px(10), ScreenUtils.dp2px(5), ScreenUtils.dp2px(10),
                ScreenUtils.dp2px(5));
        // 展示名称
        TextView name = new TextView(this);
        name.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        name.setTextColor(getResources().getColor(R.color.color_black));
        name.setBackgroundColor(getResources().getColor(R.color.color_white));
        name.setText("展示名称");
        ll.addView(name, textP);

        final EditText editName = new EditText(this);
        editName.setBackgroundResource(R.drawable.common_edit_corner_bg);
        editName.setPadding(ScreenUtils.dp2px(5), 0, ScreenUtils.dp2px(5), 0);
        editName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        editName.setTextColor(getResources().getColor(R.color.color_black));
        editName.setMinHeight(ScreenUtils.dp2px(40));
        ll.addView(editName, editP);

        DialogUtils.getInstance().showCustomDialog(this, ll, getString(R.string.group_share_info),
                getString(R.string.confirm), getString(R.string.cancel),
                new CommonCustomDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener() {
                        String name = editName.getEditableText().toString().trim();
                        addShare(filePath, name, suffix);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_FILE_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                List<FileBean> beans = (List<FileBean>)data
                        .getSerializableExtra(ChooseFileActivity.CHOOSE_FILE_DATA);
                if (beans == null || beans.isEmpty()) {
                    return;
                }
                showSetShareInfo(beans.get(0).getPath(), beans.get(0).getSuffix());
            }
        }
    }

    /**
     * 展示共享文件adapter
     */
    protected class ChatGroupShareAdapter extends RecyclerWithHFAdapter<BMXGroup.SharedFile> {

        private boolean mIsShowCheck;

        public ChatGroupShareAdapter(Context context) {
            super(context);
        }

        public void setShowCheck(boolean showCheck) {
            mIsShowCheck = showCheck;
        }

        public boolean getShowCheck() {
            return mIsShowCheck;
        }

        @Override
        protected int onCreateViewById(int viewType) {
            return R.layout.item_file_view;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            CheckBox cbChoose = holder.findViewById(R.id.cb_doc_file_choice);
            ImageView ivDocIcon = holder.findViewById(R.id.iv_doc_icon);
            TextView tvDocTitle = holder.findViewById(R.id.tv_doc_title);
            TextView tvDocSize = holder.findViewById(R.id.tv_doc_size);
            TextView tvDocTime = holder.findViewById(R.id.tv_doc_create_time);

            BMXGroup.SharedFile file = getItem(position);
            if (file == null) {
                return;
            }
            if (mIsShowCheck) {
                cbChoose.setVisibility(View.VISIBLE);
                boolean isCheck = mSelected.containsKey(file);
                cbChoose.setChecked(isCheck);
            } else {
                cbChoose.setVisibility(View.GONE);
            }

            boolean isDownload = mDownload.containsKey(file);
            tvDocTitle.setText(file.getMDisplayName());
            if (isDownload) {
                tvDocSize.setText("下载中");
            } else {
                String size = Formatter.formatFileSize(mContext, file.getMSize());
                tvDocSize.setText(size);
            }
            String time = TimeUtils.millis2String(file.getMCreateTime());
            tvDocTime.setText(!TextUtils.isEmpty(time) ? time : "");
            ivDocIcon.setImageResource(R.drawable.chat_file_default_icon);
        }
    }

}
