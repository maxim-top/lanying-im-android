
package top.maxim.im.scan.contract;

import android.content.Intent;

import com.google.zxing.Result;

import top.maxim.im.common.base.IBasePresenter;
import top.maxim.im.common.base.IBaseView;

/**
 * Description :扫一扫
 */
public interface ScannerContract {

    interface View extends IBaseView<Presenter> {
        /**
         * 展示对话框
         *
         * @param title 标题
         * @param btnString 按钮名称
         */
        void showDialog(String title, String btnString);

        void showLoadingDialog(boolean cancelable);

        void dismissLoadingDialog();
    }

    interface Presenter extends IBasePresenter<View> {

        /**
         * 进入相册
         */
        void openGalley();

        /**
         * 处理扫描结果
         *
         * @param result 扫描获取的数据
         */
        void dealResult(Result result);

        /**
         * 处理onActivity 返回值
         *
         * @param requestCode 请求码
         * @param resultCode 返回码
         * @param data 数据
         */
        void dealBackData(int requestCode, int resultCode, Intent data);
    }
}
