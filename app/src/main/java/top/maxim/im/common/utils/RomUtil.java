package top.maxim.im.common.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.UUID;

public class RomUtil {

    public static final String ROM_MIUI = "MIUI";
    public static final String ROM_EMUI = "EMUI";
    public static final String ROM_FLYME = "FLYME";
    public static final String ROM_OPPO = "OPPO";
    public static final String ROM_VIVO = "VIVO";
    public static final String ROM_QIKU = "QIKU";

    private static final String KEY_VERSION_MIUI = "ro.miui.ui.version.name";
    private static final String KEY_VERSION_EMUI = "ro.build.version.emui";
    private static final String KEY_VERSION_OPPO = "ro.build.version.opporom";
    private static final String KEY_VERSION_VIVO = "ro.vivo.os.version";

    private static String sName;
    private static String sVersion;

    private RomUtil() {
    }

    public static boolean isHuawei() {
        return check(ROM_EMUI);
    }

    public static boolean isXiaomi() {
        return check(ROM_MIUI);
    }

    public static boolean isVivo() {
        return check(ROM_VIVO);
    }

    public static boolean isOppo() {
        return check(ROM_OPPO);
    }

    public static boolean isFlyme() {
        return check(ROM_FLYME);
    }

    public static boolean is360() {
        return check(ROM_QIKU) || check("360");
    }

    public static String getName() {
        if (sName == null) {
            check("");
        }
        return sName;
    }

    public static String getVersion() {
        if (sVersion == null) {
            check("");
        }
        return sVersion;
    }

    public static boolean check(String rom) {
        if (sName != null) {
            return sName.equals(rom);
        }

        if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_MIUI))) {
            sName = ROM_MIUI;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_EMUI))) {
            sName = ROM_EMUI;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_OPPO))) {
            sName = ROM_OPPO;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_VIVO))) {
            sName = ROM_VIVO;
        } else {
            sVersion = Build.DISPLAY;
            if (sVersion.toUpperCase().contains(ROM_FLYME)) {
                sName = ROM_FLYME;
            } else {
                sVersion = Build.UNKNOWN;
                sName = Build.MANUFACTURER.toUpperCase();
            }
        }
        return sName.equals(rom);
    }

    public static String getProp(String name) {
        String line = null;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + name);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            Log.e("Unable to read prop " + name, ex.getMessage());
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

    /**
     * 获取设备唯一标识
     */
    public static String getDeviceId() {
        String deviceId2 = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) AppContextUtils.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                String deviceId1;
                if (Build.VERSION.SDK_INT >= 26) {
                    deviceId1 = telephonyManager.getImei();
                } else {
                    deviceId1 = telephonyManager.getDeviceId();
                }
                if (!TextUtils.isEmpty(deviceId1)) {
                    return deviceId1;
                }
                deviceId1 = Settings.Secure.getString(AppContextUtils.getAppContext().getContentResolver(), "android_id");
                if (!TextUtils.isEmpty(deviceId1) && !"9774d56d682e549c".equals(deviceId1)) {
                    deviceId1 = UUID.nameUUIDFromBytes(deviceId1.getBytes("utf8")).toString();
                } else {
                    deviceId1 = getOperatorBySlot(telephonyManager, "getDeviceIdGemini", 0);
                    deviceId2 = getOperatorBySlot(telephonyManager, "getDeviceIdGemini", 1);
                }
                if (TextUtils.isEmpty(deviceId1) && TextUtils.isEmpty(deviceId2)) {
                    deviceId1 = getOperatorBySlot(telephonyManager, "getDeviceId", 0);
                    deviceId2 = getOperatorBySlot(telephonyManager, "getDeviceId", 1);
                }
                if (!TextUtils.isEmpty(deviceId1)) {
                    return deviceId1;
                }
                if (!TextUtils.isEmpty(deviceId2)) {
                    return deviceId2;
                }
                if (!TextUtils.isEmpty(deviceId1) && !TextUtils.equals(deviceId1, "000000000000000")) {
                    return deviceId1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String uuid = UUID.randomUUID().toString();
        if (Build.VERSION.SDK_INT < 27 && Build.SERIAL != null) {
            uuid = Build.SERIAL;
        }
        return uuid;
    }

    private static String getOperatorBySlot(TelephonyManager telephony, String predictedMethodName,
                                     int slotID) {
        if (telephony == null) {
            return null;
        }
        String inumeric = null;
        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
            if (telephonyClass != null) {
                Class<?>[] parameter = new Class[1];
                parameter[0] = int.class;

                Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);
                if (getSimID != null) {
                    Object[] obParameter = new Object[1];
                    obParameter[0] = slotID;

                    Object ob_phone = getSimID.invoke(telephony, obParameter);
                    if (ob_phone != null) {
                        inumeric = ob_phone.toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inumeric;
    }
}