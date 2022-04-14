
package top.maxim.im.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.content.Context;
import top.maxim.im.R;

/**
 * Description : 时间工具类 Created by Mango on 2018/11/06.
 */
public final class TimeUtils {
    private TimeUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 时间戳转化
     * 
     * @param time
     * @return String
     */
    public static String millis2String(Context context, long time) {
        String FORMAT_YEAR_MONTH_DAY = context.getString(R.string.format_yyyy_mm_dd);
        String DEFAULT_PATTERN = FORMAT_YEAR_MONTH_DAY + " HH:mm";

        return new SimpleDateFormat(DEFAULT_PATTERN, Locale.getDefault()).format(new Date(time));
    }
}
