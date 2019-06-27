
package top.maxim.im.scan.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.Result;

import java.io.IOException;
import java.util.List;

import top.maxim.im.R;
import top.maxim.im.common.base.BaseActivity;
import top.maxim.im.common.provider.CommonProvider;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.utils.permissions.PermissionsConstant;
import top.maxim.im.common.view.LoadingDialog;
import top.maxim.im.scan.QrcodeScanner;
import top.maxim.im.scan.ScanActivityHandler;
import top.maxim.im.scan.camera.CameraManager;
import top.maxim.im.scan.contract.ScannerContract;
import top.maxim.im.scan.presenter.ScannerPresenter;

/**
 * Description :扫一扫界面
 */
public class ScannerActivity extends BaseActivity implements QrcodeScanner, ScannerContract.View,
        SurfaceHolder.Callback, View.OnClickListener {
    private ScanFrameView mScanFrameView;

    private ImageView mScanLine;

    private TextView mNoticeTextView;

    private CameraManager mCameraManager;

    private TranslateAnimation translateAnimation;

    private SurfaceHolder mHolder;

    private ScanActivityHandler mHandler;

    private boolean isInitCamera = false;

    private boolean isCameraSurfaceCreate;

    private ScannerContract.Presenter mPresenter;

    private boolean isFirstShowDialog = true;

    private LoadingDialog mLoadingDialog;

    public static void openScan(Context context) {
        Intent intent = new Intent(context, ScannerActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        View view = View.inflate(this, R.layout.activity_scan, null);
        setContentView(view);
        CameraManager.init(getApplication());
        setStatusBar();
        initView();
        adjustView();
        mPresenter = new ScannerPresenter(this);
    }

    // 设置沉浸式状态栏
    private void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 初始化view，添加监听事件
     */
    private void initView() {
        SurfaceView cameraSurfaceView = findViewById(R.id.sv_scan_camera);
        mHolder = cameraSurfaceView.getHolder();
        mScanFrameView = findViewById(R.id.v_scan_frame_view);
        mScanLine = findViewById(R.id.scan_line);
        mNoticeTextView = findViewById(R.id.tv_scan_notice);
        findViewById(R.id.iv_scan_back).setOnClickListener(this);
        findViewById(R.id.ll_scan_qrcode_album).setOnClickListener(this);
        mHolder.addCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 再次进入权限判断
        if (!hasPermission(PermissionsConstant.CAMERA)) {
            return;
        }

        if (mHandler == null) {
            if (isCameraSurfaceCreate) {
                initCamera();
            } else {
                mHolder.addCallback(this);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mHandler != null) {
            mHandler.quitSynchronously();
            mHandler = null;
        }
        if (mCameraManager != null) {
            mCameraManager.closeDriver();
        }
        mCameraManager = null;
        if (!isCameraSurfaceCreate) {
            mHolder.removeCallback(this);
        }
        isFirstShowDialog = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPresenter.dealBackData(requestCode, resultCode, data);
    }

    @Override
    public void onPermissionGranted(List<String> permissions) {
        if (permissions == null || permissions.size() == 0) {
            return;
        }
        for (String permission : permissions) {
            switch (permission) {
                case PermissionsConstant.READ_STORAGE:
                    // 读SD权限
                    if (hasPermission(PermissionsConstant.WRITE_STORAGE)) {
                        // 如果有读写权限都有 则直接操作
                        mPresenter.openGalley();
                    } else {
                        requestPermissions(PermissionsConstant.WRITE_STORAGE);
                    }
                    break;
                case PermissionsConstant.WRITE_STORAGE:
                    // 写SD权限 如果有读写权限都有 则直接操作
                    mPresenter.openGalley();
                    break;
                case PermissionsConstant.CAMERA:
                    if (mHandler == null) {
                        if (!isInitCamera && isCameraSurfaceCreate) {
                            initCamera();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onPermissionDenied(List<String> permissions) {
        if (permissions == null || permissions.size() == 0) {
            return;
        }
        for (String permission : permissions) {
            switch (permission) {
                case PermissionsConstant.READ_STORAGE:
                case PermissionsConstant.WRITE_STORAGE:
                    // 读写SD权限拒绝
                    CommonProvider.openAppPermission(this);
                    break;
                case PermissionsConstant.CAMERA:
                    // 拒绝
                    showOpenPermissionDialog();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public CameraManager getCameraManager() {
        return mCameraManager;
    }

    @Override
    public Handler getHandler() {
        return mHandler;
    }

    @Override
    public void handleDecode(Result rawText) {
        mPresenter.dealResult(rawText);
    }

    @Override
    public void bitmapBright(int bright) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isCameraSurfaceCreate = true;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!hasPermission(PermissionsConstant.CAMERA)) {
                requestPermissions(PermissionsConstant.CAMERA);
            } else {
                if (!isInitCamera) {
                    initCamera();
                }
            }
        } else {
            if (!isInitCamera) {
                initCamera();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isCameraSurfaceCreate = false;
        isInitCamera = false;
    }

    @Override
    public void showDialog(String title, String btnString) {
    }

    @Override
    public void setPresenter(ScannerContract.Presenter presenter) {

    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_scan_back) {
            finish();
        } else if (i == R.id.ll_scan_qrcode_album) {
            // 进入相册
            // 选择相册 需要SD卡读写权限
            if (hasPermission(PermissionsConstant.READ_STORAGE,
                    PermissionsConstant.WRITE_STORAGE)) {
                mPresenter.openGalley();
            } else {
                requestPermissions(PermissionsConstant.READ_STORAGE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.quitSynchronously();
        }
        mHandler = null;
        if (mScanFrameView != null) {
            mScanFrameView.onDestroy();
        }
        mScanFrameView = null;
        if (mPresenter != null) {
            mPresenter.onDestroyPresenter();
        }
        mPresenter = null;
        if (translateAnimation != null) {
            translateAnimation.cancel();
        }
        if (mCameraManager != null) {
            mCameraManager.closeDriver();
            mCameraManager = null;
        }
        if (mHolder != null) {
            mHolder.removeCallback(this);
        }
        isInitCamera = false;
    }

    /**
     * 展示权限开启对话框
     */
    private void showOpenPermissionDialog() {
        if (!isFinishing()) {
            DialogUtils.getInstance().showDialog(this,
                    getResources().getString(R.string.permission_title),
                    getResources().getString(R.string.permission_prompt),
                    getResources().getString(R.string.permission_setting),
                    getResources().getString(R.string.cancel), new CommonDialog.OnDialogListener() {

                        @Override
                        public void onConfirmListener() {
                            CommonProvider.openSystemSetting(ScannerActivity.this);
                        }

                        @Override
                        public void onCancelListener() {
                            ToastUtil.showTextViewPrompt(R.string.not_has_sd_permission);
                        }
                    });
        }
    }

    /**
     * 初始化相机，同时加载布局
     */
    private void initCamera() {
        isInitCamera = true;
        mCameraManager = new CameraManager(getApplication());
        if (mCameraManager.isOpen()) {
            return;
        }

        try {
            if (!mCameraManager.isCanOpenCamera() && isFirstShowDialog) {
                isFirstShowDialog = false;
                if (translateAnimation != null) {
                    translateAnimation.cancel();
                }
                mScanFrameView.setVisibility(View.GONE);
                showOpenPermissionDialog();
                return;
            }
            mCameraManager.openDriver(mHolder, getRotationDegrees());
            mCameraManager.setCameraDisplayOrientation(getRotationDegrees(), 0);
            // Creating the handler starts the preview, which can also throw
            // a
            // RuntimeException.
            if (mHandler == null) {
                mHandler = new ScanActivityHandler(ScannerActivity.this, null, "utf-8",
                        mCameraManager);
            }
        } catch (IOException ioe) {
            Log.e(getClass().getSimpleName(), ioe.getMessage());
        } catch (RuntimeException e) {
            Log.e(getClass().getSimpleName(),
                    "Barcode Scanner has seen crashes in the wild of this variety:\n"
                            + "// java.?lang.?RuntimeException: Fail to connect to camera service");
        }

    }

    private int getRotationDegrees() {

        int degrees = 0;
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                break;

        }
        return degrees;
    }

    /**
     * 调整个别view控件的位置
     */
    private void adjustView() {
        Rect rect = mScanFrameView.getFrameRect();
        RelativeLayout.LayoutParams lineLayoutParams = (RelativeLayout.LayoutParams)mScanLine
                .getLayoutParams();
        lineLayoutParams.topMargin = rect.top;
        lineLayoutParams.leftMargin = rect.left;
        lineLayoutParams.width = rect.width();
        mScanLine.setLayoutParams(lineLayoutParams);

        // 提示文本距离顶部距离,对屏幕高的分辩率小于1920的机型，距上距离进行缩放
        RelativeLayout.LayoutParams noticeLayoutParams = (RelativeLayout.LayoutParams)mNoticeTextView
                .getLayoutParams();
        noticeLayoutParams.topMargin = ScreenUtils.heightPixels < 1920
                ? rect.bottom + (int)(ScreenUtils.dp2px(28) * 0.8)
                : rect.bottom + ScreenUtils.dp2px(28);
        mNoticeTextView.setLayoutParams(noticeLayoutParams);

        scanLineStartAnim();
    }

    private void scanLineStartAnim() {
        mScanLine.setVisibility(View.VISIBLE);
        // 减9是因为扫描线图片在竖直方向上像素为9，防止超出扫描框
        translateAnimation = new TranslateAnimation(mScanLine.getX(), mScanLine.getX(),
                mScanLine.getY(), mScanFrameView.getFrameHeight() - 9);
        translateAnimation.setDuration(2000);
        translateAnimation.setInterpolator(new LinearInterpolator());
        translateAnimation.setRepeatCount(-1);
        mScanLine.startAnimation(translateAnimation);
    }

    @Override
    public void showLoadingDialog(boolean cancelable) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this);
        } else if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
        mLoadingDialog.setCancelable(cancelable);
        mLoadingDialog.show();
    }

    @Override
    public void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }
}
