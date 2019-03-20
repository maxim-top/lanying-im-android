
package top.maxim.im.common.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Description : 屏幕工具类 Created by Mango on 2018/11/05.
 */
public class ScreenUtils {

    public static int widthPixels;

    public static int heightPixels;

    public static float density;

    public static void init(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        widthPixels = dm.widthPixels;
        heightPixels = dm.heightPixels;
        density = dm.density;
    }

    public static int dp2px(float dpValue) {
        return (int)(0.5F + dpValue * density);
    }

    public static int px2dp(float pxValue) {
        return (int)(0.5F + pxValue / density);
    }

    public static int sp2px(Context context, float spValue) {
        return (int)(0.5F + spValue * context.getResources().getDisplayMetrics().scaledDensity);
    }

    /**
     * 获取屏幕信息
     * 
     * @return int[]
     */
    public static int[] getScreenInfo() {
        int[] arrayOfInt = new int[2];
        arrayOfInt[0] = widthPixels;
        arrayOfInt[1] = heightPixels;
        return arrayOfInt;
    }

    /**
     * 获取状态栏高度
     * 
     * @return int
     */
    public static int getStatusBarHeight() {
        return Resources.getSystem().getDimensionPixelSize(
                Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android"));
    }
}
