
package top.maxim.im.sdk.bean;

import android.util.SparseArray;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import im.floo.floolib.BMXMessage;

/**
 * Description : 消息体分类 Created by Mango on 2018/11/11.
 */
public final class MsgBodyHelper {

    private static Map<String, Integer> sStyle2ViewType = new HashMap<>();
    private static Map<Integer, BASE_VIEW_TYPE> sViewType2BaseViewType = new HashMap<>();
    private static Map<Integer, VIEW_STYLE> sViewType2ViewStyle = new HashMap<>();
    private static Set<Integer> sSystemViewType = new HashSet<>();

    public enum BASE_VIEW_TYPE {
        BASE_VIEW_TYPE_TEXT,
        BASE_VIEW_TYPE_IMAGE,
        BASE_VIEW_TYPE_VOICE,
        BASE_VIEW_TYPE_VIDEO,
        BASE_VIEW_TYPE_FILE,
        BASE_VIEW_TYPE_LOCATION,
    }

    public enum VIEW_STYLE {
        VIEW_STYLE_LEFT,
        VIEW_STYLE_RIGHT,
        VIEW_STYLE_EMERGENCY,
        VIEW_STYLE_CRITICAL,
        VIEW_STYLE_WARNING,
        VIEW_STYLE_NOTICE,
        VIEW_STYLE_INFO
    }


    public enum VIEW_TYPE {
        VIEW_TYPE_LEFT_TEXT,
        VIEW_TYPE_LEFT_IMAGE,
        VIEW_TYPE_LEFT_VOICE,
        VIEW_TYPE_LEFT_VIDEO,
        VIEW_TYPE_LEFT_FILE,
        VIEW_TYPE_LEFT_LOCATION,
        VIEW_TYPE_LEFT_COMMAND,
        VIEW_TYPE_LEFT_FORWARD,
        VIEW_TYPE_LEFT_RTC,
        VIEW_TYPE_RIGHT_TEXT,
        VIEW_TYPE_RIGHT_IMAGE,
        VIEW_TYPE_RIGHT_VOICE,
        VIEW_TYPE_RIGHT_VIDEO,
        VIEW_TYPE_RIGHT_FILE,
        VIEW_TYPE_RIGHT_LOCATION,
        VIEW_TYPE_RIGHT_COMMAND,
        VIEW_TYPE_RIGHT_FORWARD,
        VIEW_TYPE_RIGHT_RTC,
        VIEW_TYPE_EMERGENCY,
        VIEW_TYPE_CRITICAL,
        VIEW_TYPE_WARNING,
        VIEW_TYPE_NOTICE,
        VIEW_TYPE_INFO
    }


    static {
        sStyle2ViewType.put("emergency", VIEW_TYPE.VIEW_TYPE_EMERGENCY.ordinal());
        sStyle2ViewType.put("critical", VIEW_TYPE.VIEW_TYPE_CRITICAL.ordinal());
        sStyle2ViewType.put("warning", VIEW_TYPE.VIEW_TYPE_WARNING.ordinal());
        sStyle2ViewType.put("notice", VIEW_TYPE.VIEW_TYPE_NOTICE.ordinal());
        sStyle2ViewType.put("info", VIEW_TYPE.VIEW_TYPE_INFO.ordinal());

        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_LEFT_TEXT.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_TEXT);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_RIGHT_TEXT.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_TEXT);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_LEFT_IMAGE.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_IMAGE);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_RIGHT_IMAGE.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_IMAGE);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_LEFT_VOICE.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_VOICE);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_RIGHT_VOICE.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_VOICE);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_LEFT_VIDEO.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_VIDEO);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_RIGHT_VIDEO.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_VIDEO);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_LEFT_FILE.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_FILE);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_RIGHT_FILE.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_FILE);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_LEFT_LOCATION.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_LOCATION);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_RIGHT_LOCATION.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_LOCATION);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_LEFT_COMMAND.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_TEXT);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_RIGHT_COMMAND.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_TEXT);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_LEFT_FORWARD.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_TEXT);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_RIGHT_FORWARD.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_TEXT);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_LEFT_RTC.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_TEXT);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_RIGHT_RTC.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_TEXT);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_EMERGENCY.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_TEXT);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_CRITICAL.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_TEXT);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_WARNING.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_TEXT);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_NOTICE.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_TEXT);
        sViewType2BaseViewType.put(VIEW_TYPE.VIEW_TYPE_INFO.ordinal(), BASE_VIEW_TYPE.BASE_VIEW_TYPE_TEXT);

        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_LEFT_TEXT.ordinal(), VIEW_STYLE.VIEW_STYLE_LEFT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_RIGHT_TEXT.ordinal(), VIEW_STYLE.VIEW_STYLE_RIGHT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_LEFT_IMAGE.ordinal(), VIEW_STYLE.VIEW_STYLE_LEFT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_RIGHT_IMAGE.ordinal(), VIEW_STYLE.VIEW_STYLE_RIGHT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_LEFT_VOICE.ordinal(), VIEW_STYLE.VIEW_STYLE_LEFT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_RIGHT_VOICE.ordinal(), VIEW_STYLE.VIEW_STYLE_RIGHT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_LEFT_VIDEO.ordinal(), VIEW_STYLE.VIEW_STYLE_LEFT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_RIGHT_VIDEO.ordinal(), VIEW_STYLE.VIEW_STYLE_RIGHT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_LEFT_FILE.ordinal(), VIEW_STYLE.VIEW_STYLE_LEFT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_RIGHT_FILE.ordinal(), VIEW_STYLE.VIEW_STYLE_RIGHT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_LEFT_LOCATION.ordinal(), VIEW_STYLE.VIEW_STYLE_LEFT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_RIGHT_LOCATION.ordinal(), VIEW_STYLE.VIEW_STYLE_RIGHT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_LEFT_COMMAND.ordinal(), VIEW_STYLE.VIEW_STYLE_LEFT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_RIGHT_COMMAND.ordinal(), VIEW_STYLE.VIEW_STYLE_RIGHT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_LEFT_FORWARD.ordinal(), VIEW_STYLE.VIEW_STYLE_LEFT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_RIGHT_FORWARD.ordinal(), VIEW_STYLE.VIEW_STYLE_RIGHT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_LEFT_RTC.ordinal(), VIEW_STYLE.VIEW_STYLE_LEFT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_RIGHT_RTC.ordinal(), VIEW_STYLE.VIEW_STYLE_RIGHT);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_EMERGENCY.ordinal(), VIEW_STYLE.VIEW_STYLE_EMERGENCY);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_CRITICAL.ordinal(), VIEW_STYLE.VIEW_STYLE_CRITICAL);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_WARNING.ordinal(), VIEW_STYLE.VIEW_STYLE_WARNING);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_NOTICE.ordinal(), VIEW_STYLE.VIEW_STYLE_NOTICE);
        sViewType2ViewStyle.put(VIEW_TYPE.VIEW_TYPE_INFO.ordinal(), VIEW_STYLE.VIEW_STYLE_INFO);

        sSystemViewType.add(VIEW_TYPE.VIEW_TYPE_EMERGENCY.ordinal());
        sSystemViewType.add(VIEW_TYPE.VIEW_TYPE_CRITICAL.ordinal());
        sSystemViewType.add(VIEW_TYPE.VIEW_TYPE_WARNING.ordinal());
        sSystemViewType.add(VIEW_TYPE.VIEW_TYPE_NOTICE.ordinal());
        sSystemViewType.add(VIEW_TYPE.VIEW_TYPE_INFO.ordinal());
    }

    public static BASE_VIEW_TYPE getBaseViewType(int viewType) {
        return sViewType2BaseViewType.get(viewType);
    }

    public static VIEW_STYLE getViewStyle(int viewType) {
        return sViewType2ViewStyle.get(viewType);
    }

    public static boolean isLeftStyle(int viewType) {
        return sViewType2ViewStyle.get(viewType) == VIEW_STYLE.VIEW_STYLE_LEFT;
    }

    public static boolean isRightStyle(int viewType) {
        return sViewType2ViewStyle.get(viewType) == VIEW_STYLE.VIEW_STYLE_RIGHT;
    }

    public static int getViewTypeByContentType(BMXMessage.ContentType contentType, boolean isSend) {
        int viewType = VIEW_TYPE.VIEW_TYPE_RIGHT_TEXT.ordinal();
        if (!isSend){
            switch (contentType){
                case Text:
                    viewType = VIEW_TYPE.VIEW_TYPE_LEFT_TEXT.ordinal();
                    break;
                case Image:
                    viewType = VIEW_TYPE.VIEW_TYPE_LEFT_IMAGE.ordinal();
                    break;
                case Voice:
                    viewType = VIEW_TYPE.VIEW_TYPE_LEFT_VOICE.ordinal();
                    break;
                case Video:
                    viewType = VIEW_TYPE.VIEW_TYPE_LEFT_VIDEO.ordinal();
                    break;
                case File:
                    viewType = VIEW_TYPE.VIEW_TYPE_LEFT_FILE.ordinal();
                    break;
                case Location:
                    viewType = VIEW_TYPE.VIEW_TYPE_LEFT_LOCATION.ordinal();
                    break;
                case Command:
                    viewType = VIEW_TYPE.VIEW_TYPE_LEFT_COMMAND.ordinal();
                    break;
                case Forward:
                    viewType = VIEW_TYPE.VIEW_TYPE_LEFT_FORWARD.ordinal();
                    break;
                case RTC:
                    viewType = VIEW_TYPE.VIEW_TYPE_LEFT_RTC.ordinal();
                    break;
                default:
                    break;
            }
        } else {
            switch (contentType) {
                case Text:
                    viewType = VIEW_TYPE.VIEW_TYPE_RIGHT_TEXT.ordinal();
                    break;
                case Image:
                    viewType = VIEW_TYPE.VIEW_TYPE_RIGHT_IMAGE.ordinal();
                    break;
                case Voice:
                    viewType = VIEW_TYPE.VIEW_TYPE_RIGHT_VOICE.ordinal();
                    break;
                case Video:
                    viewType = VIEW_TYPE.VIEW_TYPE_RIGHT_VIDEO.ordinal();
                    break;
                case File:
                    viewType = VIEW_TYPE.VIEW_TYPE_RIGHT_FILE.ordinal();
                    break;
                case Location:
                    viewType = VIEW_TYPE.VIEW_TYPE_RIGHT_LOCATION.ordinal();
                    break;
                case Command:
                    viewType = VIEW_TYPE.VIEW_TYPE_RIGHT_COMMAND.ordinal();
                    break;
                case Forward:
                    viewType = VIEW_TYPE.VIEW_TYPE_RIGHT_FORWARD.ordinal();
                    break;
                case RTC:
                    viewType = VIEW_TYPE.VIEW_TYPE_RIGHT_RTC.ordinal();
                    break;
                default:
                    break;
            }
        }
        return viewType;
    }

    public static Integer getViewTypeByStyle(String style) {
        return sStyle2ViewType.get(style);
    }

    public static boolean isSystemViewType(int viewType) {
        return sSystemViewType.contains(viewType);
    }
}
