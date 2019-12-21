
package top.maxim.im.common.utils;

import android.text.TextUtils;
import android.util.LongSparseArray;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.bean.UserBean;
import top.maxim.im.push.PushClientMgr;
import top.maxim.im.push.PushUtils;

/**
 * Description : 聊天工具类 Created by Mango on 2018/11/18.
 */
public class CommonUtils {

    private static CommonUtils mInstance;

    private static final String TAG = "CommonUtils";

    private CommonUtils() {
    }

    public static CommonUtils getInstance() {
        if (mInstance == null) {
            synchronized (CommonUtils.class) {
                if (mInstance == null) {
                    mInstance = new CommonUtils();
                }
            }
        }
        return mInstance;
    }

    public void addUser(UserBean bean) {
        if (bean == null) {
            return;
        }
        // 添加登陆账号缓存
        LongSparseArray<String> map = null;
        Gson gson = new Gson();
        String loginUserData = SharePreferenceUtils.getInstance().getLoginUserData();
        if (!TextUtils.isEmpty(loginUserData)) {
            map = gson.fromJson(loginUserData, LongSparseArray.class);
        }
        if (map == null) {
            map = new LongSparseArray<>();
        }
        map.put(bean.getUserId(), gson.toJson(bean));
        SharePreferenceUtils.getInstance().putLoginUserData(gson.toJson(map));

        String userName = bean.getUserName();
        long userId = bean.getUserId();
        String pwd = bean.getUserPwd();
        SharePreferenceUtils.getInstance().putUserId(userId);
        SharePreferenceUtils.getInstance().putUserName(userName);
        SharePreferenceUtils.getInstance().putUserPwd(pwd);
    }

    public void logout() {
        SharePreferenceUtils.getInstance().putLoginStatus(false);
        SharePreferenceUtils.getInstance().putUserId(-1);
        SharePreferenceUtils.getInstance().putUserName("");
        SharePreferenceUtils.getInstance().putUserPwd("");
        SharePreferenceUtils.getInstance().putToken("");
        SharePreferenceUtils.getInstance().putAppId("");
        UserManager.getInstance().changeAppId(SharePreferenceUtils.getInstance().getAppId());
        PushClientMgr.getManager().unRegister();
        PushUtils.getInstance().unregisterActivityListener(AppContextUtils.getApplication());
    }

    public void removeAccount(long id) {
        if (id <= 0) {
            return;
        }
        String loginUserData = SharePreferenceUtils.getInstance().getLoginUserData();
        Gson gson = new Gson();
        if (!TextUtils.isEmpty(loginUserData)) {
            LongSparseArray<String> map = gson.fromJson(loginUserData, LongSparseArray.class);
            String data = "";
            if (map != null && map.size() > 0) {
                map.remove(id);
                if (map.size() > 0) {
                    data = gson.toJson(map);
                }
            }
            SharePreferenceUtils.getInstance().putLoginUserData(data);
        }
    }

    public List<UserBean> getLoginUsers() {
        String loginUserData = SharePreferenceUtils.getInstance().getLoginUserData();
        List<UserBean> beans = new ArrayList<>();
        Gson gson = new Gson();
        if (!TextUtils.isEmpty(loginUserData)) {
            LongSparseArray<String> map = gson.fromJson(loginUserData, LongSparseArray.class);
            if (map != null && map.size() > 0) {
                for (int i = 0; i < map.size(); i++) {
                    String userData = map.valueAt(i);
                    if (!TextUtils.isEmpty(userData)) {
                        beans.add(gson.fromJson(userData, UserBean.class));
                    }
                }
            }
        }
        if (!beans.isEmpty()) {
            Collections.sort(beans, new Comparator<UserBean>() {
                @Override
                public int compare(UserBean o1, UserBean o2) {
                    return o1.getTimestamp() > o2.getTimestamp() ? -1 : 0;
                }
            });
        }
        return beans;
    }
}
