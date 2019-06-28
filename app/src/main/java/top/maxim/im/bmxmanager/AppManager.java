
package top.maxim.im.bmxmanager;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.net.DefaultOkhttpClient;
import top.maxim.im.net.HttpCallback;
import top.maxim.im.net.HttpClient;
import top.maxim.im.net.HttpResponseCallback;

/**
 * Description : 用户 Created by Mango on 2018/12/2.
 */
public class AppManager {

    private static final String mBaseUrl = "http://39.104.4.219/app/";

    private static final String TAG = AppManager.class.getSimpleName();

    private static final AppManager sInstance = new AppManager();

    private DefaultOkhttpClient mClient;

    public static AppManager getInstance() {
        return sInstance;
    }

    private AppManager() {
        mClient = new DefaultOkhttpClient();
    }

    /**
     * 根据userId获取token
     * 
     * @param id
     * @param pwd
     * @param callback
     */
    public void getTokenById(long id, String pwd, HttpResponseCallback<String> callback) {
        String token = SharePreferenceUtils.getInstance().getToken();
        if (!TextUtils.isEmpty(token)) {
            if (callback != null) {
                callback.onCallResponse(token);
            }
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(id));
        params.put("password", pwd);
        mClient.call(HttpClient.Method.POST, mBaseUrl + "token_id", params,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("token")) {
                                String token = jsonObject.getString("token");
                                SharePreferenceUtils.getInstance().putToken(token);
                                if (callback != null) {
                                    callback.onCallResponse(token);
                                }
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (callback != null) {
                            callback.onCallFailure(-1, "", new Throwable());
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, t);
                        }
                    }
                });
    }

    /**
     * 根据userName获取token
     * 
     * @param name
     * @param pwd
     * @param callback
     */
    public void getTokenByName(String name, String pwd, HttpResponseCallback<String> callback) {
        String token = SharePreferenceUtils.getInstance().getToken();
        if (!TextUtils.isEmpty(token)) {
            if (callback != null) {
                callback.onCallResponse(token);
            }
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("password", pwd);
        mClient.call(HttpClient.Method.POST, mBaseUrl + "token", params,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("data")) {
                                String data = jsonObject.getString("data");
                                jsonObject = new JSONObject(data);
                                if (jsonObject.has("token")) {
                                    String tokenResult = jsonObject.getString("token");
                                    SharePreferenceUtils.getInstance().putToken(tokenResult);
                                    if (callback != null) {
                                        callback.onCallResponse(tokenResult);
                                    }
                                }
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (callback != null) {
                            callback.onCallFailure(-1, "", new Throwable());
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, t);
                        }
                    }
                });
    }

    /**
     * 获取群邀请的二维码信息
     * 
     * @param groupId 群id
     */
    public void getGroupSign(long groupId, String token, HttpResponseCallback<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("group_id", String.valueOf(groupId));
        Map<String, String> header = new HashMap<>();
        header.put("access-token", token);
        mClient.call(HttpClient.Method.GET, mBaseUrl + "qrcode/group_sign", params, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("data")) {
                                String data = jsonObject.getString("data");
                                jsonObject = new JSONObject(data);
                                if (jsonObject.has("qr_info")) {
                                    if (callback != null) {
                                        callback.onCallResponse(jsonObject.getString("qr_info"));
                                    }
                                }
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (callback != null) {
                            callback.onCallFailure(-1, "", new Throwable());
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(-1, "", new Throwable());
                        }
                    }
                });
    }

    /**
     * 加入群
     *
     */
    public void groupInvite(String token, String qrInfo, HttpResponseCallback<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("qr_info", qrInfo);
        Map<String, String> header = new HashMap<>();
        header.put("access-token", token);
        mClient.call(HttpClient.Method.POST, mBaseUrl + "qrcode/group_invite", params, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(-1, "", new Throwable());
                        }
                    }
                });
    }
}
