
package top.maxim.im.common.utils;

import android.os.Environment;

/**
 * Description : 文件常量 Created by Mango on 2018/11/06.
 */

public class FileConfig {

    // 应用文件名
    private static String APP_DIR_NAME = "maxIM";

    // 应用文件绝对路径
    public static String DIR_APP_NAME = AppContextUtils.getAppContext().getExternalFilesDir(null).getPath()
            + "/" + APP_DIR_NAME;

    // 应用缓存文件绝对路径
    public static String DIR_APP_CACHE = DIR_APP_NAME + "/cache";

    // 应用相册绝对路径
    public static String DIR_APP_CACHE_CAMERA = DIR_APP_NAME + "/camera";

    // 应用音频绝对路径
    public static String DIR_APP_CACHE_VOICE = DIR_APP_NAME + "/voice";

    // 应用音频绝对路径
    public static String DIR_APP_CACHE_VIDEO = DIR_APP_NAME + "/video";

    // 应用log绝对路径
    public static String DIR_APP_CRASH_LOG = DIR_APP_NAME + "/log";

    // 应用下载绝对路径
    public static String DIR_APP_DOWNLOAD = DIR_APP_NAME + "/download";
}
