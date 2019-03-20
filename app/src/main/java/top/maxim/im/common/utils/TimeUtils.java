
package top.maxim.im.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Description : 时间工具类 Created by Mango on 2018/11/06.
 */
public final class TimeUtils {

    private static final String DEFAULT_PATTERN = "yyyy年MM月dd日 HH:mm";

    public static final String FORMAT_HOUR_MINUTE = "HH:mm";

    public static final String FORMAT_MONTH_DAY = "MM月dd日";

    public static final String FORMAT_MONTH_DAY_HOUR_MINUTE = "MM月dd日 HH:mm";

    public static final String FORMAT_YEAR_MONTH = "yyyy年MM";

    public static final String FORMAT_YEAR_MONTH_DAY = "yyyy年MM月dd日";

    private TimeUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 时间戳转化
     * 
     * @param time
     * @return String
     */
    public static String millis2String(long time) {
        return new SimpleDateFormat(DEFAULT_PATTERN, Locale.getDefault()).format(new Date(time));
    }

    /**
     * 时间戳转化
     * 
     * @param time
     * @return String
     */
    public static String day2String(long time) {
        return new SimpleDateFormat(FORMAT_YEAR_MONTH_DAY, Locale.getDefault())
                .format(new Date(time));
    }
}
