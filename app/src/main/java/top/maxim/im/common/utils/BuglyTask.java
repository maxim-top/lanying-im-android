
package top.maxim.im.common.utils;

import android.app.Application;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Bugly 初始化
 */
public class BuglyTask {

    private static final BuglyTask sInstance = new BuglyTask();

    private BuglyTask() {

    }

    public static BuglyTask get() {
        return sInstance;
    }

    public void init(Application context) {
//        CrashReport.setIsDevelopmentDevice(context, BuildConfig.DEBUG);
//        // 设置是否为上报进程
//        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
//        String packageName = context.getPackageName();
//        String processName = getProcessName(android.os.Process.myPid());
//        strategy.setUploadProcess(
//                TextUtils.isEmpty(processName) || TextUtils.equals(processName, packageName));
//        // 设置延迟上传
//        strategy.setAppReportDelay(10_000);
//        // 初始化Bugly
//        CrashReport.initCrashReport(context, strategy);
    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
}
