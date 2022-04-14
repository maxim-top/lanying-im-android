
package top.maxim.im.scan.utils;


import android.app.Activity;
import android.text.TextUtils;

import top.maxim.im.R;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.scan.view.ScanResultActivity;

/**
 * Description:扫描结果处理工具类
 */

public class ScanResultUtil {

    /**
     * 处理扫描后的结果
     *
     * @param scanMessage 扫描信息
     */
    public void dealScanResult(Activity activity, String scanMessage) {
        if (TextUtils.isEmpty(scanMessage)) {
            ToastUtil.showTextViewPrompt(activity.getString(R.string.qr_code_not_recognized));
            activity.finish();
            return;
        }
        ScanResultActivity.openScanResult(activity, scanMessage);
        activity.finish();
    }
}
