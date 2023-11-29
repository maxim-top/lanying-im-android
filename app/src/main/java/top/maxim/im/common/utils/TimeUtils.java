
package top.maxim.im.common.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.content.Context;
import android.os.Build;

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

    public static String millis2StringOnConversationList(Context context, long time) {
        String format = context.getString(R.string.format_yyyy_mm_dd);
        String ret = new SimpleDateFormat(format, Locale.getDefault()).format(new Date(time));

        Date date = new Date(time);
        Date today = new Date();

        Calendar calendarDate = Calendar.getInstance();
        calendarDate.setTime(date);
        Calendar calendarToday = Calendar.getInstance();
        calendarToday.setTime(today);

        // 今年
        if (calendarDate.get(Calendar.YEAR) == calendarToday.get(Calendar.YEAR)) {
            calendarDate.setTimeInMillis(time);

            calendarDate.set(Calendar.HOUR_OF_DAY, 0);
            calendarDate.set(Calendar.MINUTE, 0);
            calendarDate.set(Calendar.SECOND, 0);
            calendarDate.set(Calendar.MILLISECOND, 0);

            calendarToday.set(Calendar.HOUR_OF_DAY, 0);
            calendarToday.set(Calendar.MINUTE, 0);
            calendarToday.set(Calendar.SECOND, 0);
            calendarToday.set(Calendar.MILLISECOND, 0);

            long dateTime = calendarDate.getTimeInMillis();
            long todayTime = calendarToday.getTimeInMillis();

            // 计算日期差异
            long daysDifference = (todayTime - dateTime) / (1000 * 60 * 60 * 24);
            if(daysDifference < 7){
                if(daysDifference == 0){
                    format = "HH:mm";
                }else if(daysDifference == 1){
                    format = " HH:mm";
                }else {
                    format = "EEEE";
                }
            }else{
                format = "MM/dd";
            }

            Locale locale;
            String language = SharePreferenceUtils.getInstance().getAppLanguage();
            if (!language.isEmpty() && language.equals("en")){
                locale = Locale.ENGLISH;
            }else{
                //7.0以上和7.0以下获取系统语言方式
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    locale = context.getResources().getConfiguration().getLocales().get(0);
                } else {
                    locale = Locale.getDefault();
                }
            }
            if(daysDifference == 1){
                ret = context.getString(R.string.yesterday) + new SimpleDateFormat(format, locale).format(new Date(time));
            }else{
                ret = new SimpleDateFormat(format, locale).format(new Date(time));
            }
        }

        return ret;
    }
    public static String millis2StringOnMessageList(Context context, long time) {
        String format = context.getString(R.string.format_yyyy_mm_dd) + " HH:mm";
        String ret = new SimpleDateFormat(format, Locale.getDefault()).format(new Date(time));

        Date date = new Date(time);
        Date today = new Date();

        Calendar calendarDate = Calendar.getInstance();
        calendarDate.setTime(date);
        Calendar calendarToday = Calendar.getInstance();
        calendarToday.setTime(today);

        // 今年
        if (calendarDate.get(Calendar.YEAR) == calendarToday.get(Calendar.YEAR)) {
            calendarDate.setTimeInMillis(time);

            calendarDate.set(Calendar.HOUR_OF_DAY, 0);
            calendarDate.set(Calendar.MINUTE, 0);
            calendarDate.set(Calendar.SECOND, 0);
            calendarDate.set(Calendar.MILLISECOND, 0);

            calendarToday.set(Calendar.HOUR_OF_DAY, 0);
            calendarToday.set(Calendar.MINUTE, 0);
            calendarToday.set(Calendar.SECOND, 0);
            calendarToday.set(Calendar.MILLISECOND, 0);

            long dateTime = calendarDate.getTimeInMillis();
            long todayTime = calendarToday.getTimeInMillis();

            // 计算日期差异
            long daysDifference = (todayTime - dateTime) / (1000 * 60 * 60 * 24);
            if(daysDifference < 7){
                if(daysDifference == 0){
                    format = "HH:mm";
                }else if(daysDifference == 1){
                    format = " HH:mm";
                }else {
                    format = "EEEE HH:mm";
                }
            }else{
                format = "MM/dd HH:mm";
            }

            Locale locale;
            String language = SharePreferenceUtils.getInstance().getAppLanguage();
            if (!language.isEmpty() && language.equals("en")){
                locale = Locale.ENGLISH;
            }else{
                //7.0以上和7.0以下获取系统语言方式
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    locale = context.getResources().getConfiguration().getLocales().get(0);
                } else {
                    locale = Locale.getDefault();
                }
            }
            if(daysDifference == 1){
                ret = context.getString(R.string.yesterday) + new SimpleDateFormat(format, locale).format(new Date(time));
            }else{
                ret = new SimpleDateFormat(format, locale).format(new Date(time));
            }
        }

        return ret;
    }
}
