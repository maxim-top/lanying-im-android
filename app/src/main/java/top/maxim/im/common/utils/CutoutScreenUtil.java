package top.maxim.im.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CutoutScreenUtil {

    private static final String TAG = CutoutScreenUtil.class.getName();
    private static final int FLAG_NOTCH_SUPPORT_HW = 0x00010000;
    private static final int FLAG_NOTCH_SUPPORT_VIVO = 0x00000020;


    public static boolean isCutoutScreen(Context context) {
        if (checkHuaWei(context)) {
            return true;
        } else if (checkVivo(context)) {
            return true;
        } else if (checkMiUI(context)) {
            return true;
        } else if (checkOppo(context)) {
            return true;
        }

        return false;
    }
    private static boolean checkOppo(Context context) {
        try {
            return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
        } catch (Exception e) {
            Log.e(TAG, "checkOppo notchScreen exception");
        }
        return false;
    }

    private static boolean checkMiUI(Context context) {

        int result = 0;
        try {
            ClassLoader classLoader = context.getClassLoader();
            @SuppressLint("PrivateApi")
            @SuppressWarnings("rawtypes")
            Class systemProperties = classLoader.loadClass("android.os.SystemProperties");
            //参数类型
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = int.class;
            Method getInt = systemProperties.getMethod("getInt", paramTypes);
            //参数
            Object[] params = new Object[2];
            params[0] = "ro.miui.notch";
            params[1] = 0;
            result = (Integer)getInt.invoke(systemProperties, params);
            return result == 1;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean checkHuaWei(Context context) {

        boolean ret = false;

        try {

            ClassLoader cl = context.getClassLoader();

            Class hwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");

            Method get = hwNotchSizeUtil.getMethod("hasNotchInScreen");

            ret = (boolean)get.invoke(hwNotchSizeUtil);

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "hasNotchInScreen ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "hasNotchInScreen NoSuchMethodException");
        } catch (Exception e) {
            Log.e(TAG, "hasNotchInScreen Exception");

        }
        return ret;
    }

    private static boolean checkVivo(Context context) {

        boolean ret;
        try {
            ClassLoader cl = context.getClassLoader();
            @SuppressLint("PrivateApi")
            Class ftFeature = cl.loadClass("android.util.FtFeature");
            Method isFeatureSupport = ftFeature.getMethod("isFeatureSupport");
            ret = (boolean)isFeatureSupport.invoke(ftFeature, FLAG_NOTCH_SUPPORT_VIVO);
        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }

    public static int[] getNotchSize(Context context) {

        int[] ret = new int[] {0, 0};

        try {

            ClassLoader cl = context.getClassLoader();

            Class hwnotchsizeutil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");

            Method get = hwnotchsizeutil.getMethod("getNotchSize");

            ret = (int[])get.invoke(hwnotchsizeutil);

        } catch (ClassNotFoundException e) {

            Log.e("test", "getNotchSize ClassNotFoundException");

        } catch (NoSuchMethodException e) {

            Log.e("test", "getNotchSize NoSuchMethodException");

        } catch (Exception e) {

            Log.e("test", "getNotchSize Exception");

        }
        return ret;
    }

    public static void setFullScreenWindowLayoutInDisplayCutout(Window window) {
        if (window == null) {
            return;
        }
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        try {
            Class layoutParamsExCls = Class.forName("com.huawei.android.view.LayoutParamsEx");
            Constructor con = layoutParamsExCls.getConstructor(WindowManager.LayoutParams.class);
            Object layoutParamsExObj = con.newInstance(layoutParams);
            Method method = layoutParamsExCls.getMethod("addHwFlags", int.class);
            method.invoke(layoutParamsExObj, FLAG_NOTCH_SUPPORT_HW);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "hw add notch screen flag api error");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "hw add notch screen flag api error");
        } catch (IllegalAccessException e) {
            Log.e(TAG, "hw add notch screen flag api error");
        } catch (InstantiationException e) {
            Log.e(TAG, "hw add notch screen flag api error");
        } catch (InvocationTargetException e) {
            Log.e(TAG, "hw add notch screen flag api error");
        } catch (Exception e) {
            Log.e(TAG, "other Exception");
        }
    }

    public static void setNotFullScreenWindowLayoutInDisplayCutout(Window window) {
        if (window == null) {
            return;
        }
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        try {
            Class layoutParamsExCls = Class.forName("com.huawei.android.view.LayoutParamsEx");
            Constructor con = layoutParamsExCls.getConstructor(WindowManager.LayoutParams.class);
            Object layoutParamsExObj = con.newInstance(layoutParams);
            Method method = layoutParamsExCls.getMethod("clearHwFlags", int.class);
            method.invoke(layoutParamsExObj, FLAG_NOTCH_SUPPORT_HW);
            Log.e(TAG, "............clear");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "hw clear notch screen flag api error");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "hw clear notch screen flag api error");
        } catch (IllegalAccessException e) {
            Log.e(TAG, "hw clear notch screen flag api error");
        } catch (InstantiationException e) {
            Log.e(TAG, "hw clear notch screen flag api error");
        } catch (InvocationTargetException e) {
            Log.e(TAG, "hw clear notch screen flag api error");
        } catch (Exception e) {
            Log.e(TAG, "other Exception");
        }
    }
}

