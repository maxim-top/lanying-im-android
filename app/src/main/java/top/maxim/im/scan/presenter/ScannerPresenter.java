
package top.maxim.im.scan.presenter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.google.zxing.Result;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import top.maxim.im.R;
import top.maxim.im.common.utils.CameraUtils;
import top.maxim.im.common.utils.FileUtils;
import top.maxim.im.common.view.BMImageLoader;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.scan.contract.ScannerContract;
import top.maxim.im.scan.utils.QRCodeShowUtils;
import top.maxim.im.scan.utils.ScanResultUtil;

/**
 * Description :扫一扫 presenter
 */
public class ScannerPresenter implements ScannerContract.Presenter {

    private ScannerContract.View mView;

    private CompositeSubscription mSubscription;

    private ScanResultUtil mScanUtil;

    /* 相册 */
    private final int IMAGE_REQUEST = 1000;

    public ScannerPresenter(ScannerContract.View view) {
        mView = view;
        mSubscription = new CompositeSubscription();
        mScanUtil = new ScanResultUtil();
    }

    @Override
    public void openGalley() {
        CameraUtils.getInstance().takeGalley((Activity)mView.getContext(), IMAGE_REQUEST);
    }

    @Override
    public void dealResult(Result result) {
        if (mView == null) {
            return;
        }
        if (result == null || TextUtils.isEmpty(result.getText())) {
            mView.showDialog("未识别到二维码，请重试", mView.getContext().getString(R.string.confirm));
            return;
        }
        String str = result.getText();
        Activity activity = (Activity)mView.getContext();
        if (mScanUtil != null) {
            mScanUtil.dealScanResult(activity, str);
        }
    }

    @Override
    public void dealBackData(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IMAGE_REQUEST:
                // 相册
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String path = "";
                    try {
                        Uri selectedImage = data.getData(); // 获取系统返回的照片的Uri
                        path = FileUtils.getFilePathByUri(selectedImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (TextUtils.isEmpty(path)) {
                        return;
                    }
                    parsePhoto(path);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 解析相册照片
     */
    private void parsePhoto(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        mView.showLoadingDialog(true);
        ImageRequestConfig option = new ImageRequestConfig.Builder().cacheInMemory(false)
                .cacheOnDisk(false).build();
        BMImageLoader.getInstance().loadImage("file://" + path, option, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (mView != null) {
                    mView.dismissLoadingDialog();
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                Observable.just("").map(s -> new QRCodeShowUtils().decodeBitmap(loadedImage))
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<String>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                if (mView != null) {
                                    mView.dismissLoadingDialog();
                                }
                            }

                            @Override
                            public void onNext(String s) {
                                if (mView == null) {
                                    return;
                                }
                                mView.dismissLoadingDialog();
                                if (mScanUtil != null) {
                                    mScanUtil.dealScanResult((Activity)mView.getContext(), s);
                                }
                            }
                        });
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if (mView != null) {
                    mView.dismissLoadingDialog();
                }
            }
        });
    }

    @Override
    public void onDestroyPresenter() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
        mView = null;
    }
}
