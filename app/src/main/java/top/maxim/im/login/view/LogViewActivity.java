
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.utils.FileConfig;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CustomDialog;
import top.maxim.im.common.view.Header;
import top.maxim.im.filebrowser.FileBrowserActivity;

/**
 * Description : 日志查看 Created by Mango on 2018/11/21.
 */
public class LogViewActivity extends BaseTitleActivity {
    
    private static final String LOG_NAME = "floo.log";
    
    private TextView mTvLog;
    
    /* 日志路径 */
    private String mLogPath;

    private String mAppId;

    public static void openLogView(Context context) {
        openLogView(context, "");
    }

    public static void openLogView(Context context, String appId) {
        Intent intent = new Intent(context, LogViewActivity.class);
        intent.putExtra(CommonConfig.CHANGE_APP_ID, appId);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.log_info);
        builder.setRightIcon(R.drawable.icon_more, v -> {
            showSaveLog();
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
        View view = View.inflate(this, R.layout.activity_log_view, null);
        mTvLog = view.findViewById(R.id.tv_log);
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mAppId = intent.getStringExtra(CommonConfig.CHANGE_APP_ID);
        }
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        showLoadingDialog(true);
        Observable.just(LOG_NAME).map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                return readTxtFromFilePath(s);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtil.showTextViewPrompt("获取日志错误");
                    }

                    @Override
                    public void onNext(String s) {
                        mTvLog.setText(s);
                    }
                });
    }

    /**
     * 获取日志路径
     * 
     * @return File
     */
    private String getLogPath() {
        String appPath = AppContextUtils.getAppContext().getFilesDir().getPath();
        String appId = TextUtils.isEmpty(mAppId) ? SharePreferenceUtils.getInstance().getAppId() : mAppId;
        String path = appPath + "/data_dir/" + appId + "/flooLog/";
        return path;
    }

    /**
     * 读取日志
     * @param filename  日志名称
     * @return String
     */
    private String readTxtFromFilePath(String filename) {
        // 判断是否有读取权限
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return "";
        }
        mLogPath = getLogPath() + filename;
        if (!new File(mLogPath).exists()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        // 打开文件输入流
        try {
            FileInputStream input = new FileInputStream(mLogPath);
            byte[] temp = new byte[1024];

            int len = 0;
            // 读取文件内容:
            while ((len = input.read(temp)) > 0) {
                sb.append(new String(temp, 0, len));
            }
            // 关闭输入流
            input.close();
        } catch (IOException e) {
            Log.e("readTxtFromFilePath", "readTxtFromFilePath");
            e.printStackTrace();
        }
        return sb.toString();
    }

    private void showSaveLog() {
        final CustomDialog dialog = new CustomDialog();
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // 保存
        TextView save = new TextView(this);
        save.setPadding(ScreenUtils.dp2px(15), 0, ScreenUtils.dp2px(15), 0);
        save.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        save.setTextColor(getResources().getColor(R.color.color_black));
        save.setBackgroundColor((getResources().getColor(R.color.color_white)));
        save.setText(getString(R.string.save_log));
        save.setOnClickListener(v -> {
            //保存的路径
            String appId = SharePreferenceUtils.getInstance().getAppId();
            File savePath = new File(FileConfig.DIR_APP_CRASH_LOG + "/" + appId);
            if (!savePath.exists()) {
                savePath.mkdirs();
            }
            FileBrowserActivity.copySdcardFile(mLogPath, savePath + "/" + LOG_NAME);
            dialog.dismiss();
        });
        ll.addView(save, params);

        dialog.setCustomView(ll);
        dialog.showDialog(this);
    }

}
