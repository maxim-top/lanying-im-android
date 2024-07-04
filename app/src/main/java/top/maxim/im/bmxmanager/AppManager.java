
package top.maxim.im.bmxmanager;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import top.maxim.im.common.utils.GsonParameterizedType;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.contact.bean.SupportBean;
import top.maxim.im.net.DefaultOkhttpClient;
import top.maxim.im.net.HttpCallback;
import top.maxim.im.net.HttpClient;
import top.maxim.im.net.HttpResponseCallback;

/**
 * Description : 用户 Created by Mango on 2018/12/2.
 */
public class AppManager {

    private static final String mBaseUrl = "https://api.maximtop.com/app/";

    private static final String TAG = AppManager.class.getSimpleName();

    private static final AppManager sInstance = new AppManager();

    private DefaultOkhttpClient mClient;

    private Gson mGson;

    public static AppManager getInstance() {
        return sInstance;
    }

    private AppManager() {
        mClient = new DefaultOkhttpClient();
        mGson = new Gson();
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

        Map<String, String> header = new HashMap<>();
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        mClient.call(HttpClient.Method.POST, mBaseUrl + "token_id", params, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        String token = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                String data = jsonObject.getString("data");
                                jsonObject = new JSONObject(data);
                                if (jsonObject.has("token")) {
                                    token = jsonObject.getString("token");
                                    SharePreferenceUtils.getInstance().putToken(token);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code == 200) {
                            // 成功
                            if (callback != null) {
                                callback.onCallResponse(token);
                            }
                        } else {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
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
       getTokenByNameFromServer(name, pwd, callback);
    }

    /**
     * 根据userName获取token
     *
     * @param name
     * @param pwd
     * @param callback
     */
    public void getTokenByNameFromServer(String name, String pwd, HttpResponseCallback<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("password", pwd);

        Map<String, String> header = new HashMap<>();
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        mClient.call(HttpClient.Method.POST, mBaseUrl + "token", params, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        String token = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                String data = jsonObject.getString("data");
                                jsonObject = new JSONObject(data);
                                if (jsonObject.has("token")) {
                                    token = jsonObject.getString("token");
                                    SharePreferenceUtils.getInstance().putToken(token);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code == 200) {
                            // 成功
                            if (callback != null) {
                                callback.onCallResponse(token);
                            }
                        } else {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
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
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
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
                        int code = -1;
                        String error = "";
                        String qrInfo = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                String data = jsonObject.getString("data");
                                jsonObject = new JSONObject(data);
                                if (jsonObject.has("qr_info")) {
                                    qrInfo = jsonObject.getString("qr_info");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code == 200) {
                            // 成功
                            if (callback != null) {
                                callback.onCallResponse(qrInfo);
                            }
                        } else {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 加入群
     */
    public void groupInvite(String token, String qrInfo, HttpResponseCallback<Boolean> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("qr_info", qrInfo);
        Map<String, String> header = new HashMap<>();
        header.put("access-token", token);
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        mClient.call(HttpClient.Method.POST, mBaseUrl + "qrcode/group_invite", params, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                boolean data = jsonObject.getBoolean("data");
                                if (callback != null) {
                                    callback.onCallResponse(data);
                                }
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code != 200) {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 获取手机验证码
     *
     * @param mobile 手机号
     */
    public void captchaSMS(String mobile, HttpResponseCallback<Boolean> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("mobile", mobile);
        Map<String, String> header = new HashMap<>();
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        mClient.call(HttpClient.Method.GET, mBaseUrl + "captcha/sms", params, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                boolean data = jsonObject.getBoolean("data");
                                if (callback != null) {
                                    callback.onCallResponse(data);
                                }
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code != 200) {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 根据手机号 验证码获取用户信息
     *
     * @param mobile 手机号
     */
    public void getInfoPwdByCaptcha(String mobile, String captcha,
            HttpResponseCallback<String> callback) {
        Map<String, String> header = new HashMap<>();
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        Map<String, String> params = new HashMap<>();
        params.put("mobile", mobile);
        params.put("captcha", captcha);
        mClient.call(HttpClient.Method.GET, mBaseUrl + "user/info_pwd", params, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                String data = jsonObject.getString("data");
                                if (callback != null) {
                                    callback.onCallResponse(data);
                                }
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code != 200) {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 微信登录
     *
     * @param code 微信返会code
     */
    public void weChatLogin(String code, HttpResponseCallback<String> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        Map<String, String> header = new HashMap<>();
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        mClient.call(HttpClient.Method.GET, mBaseUrl + "wechat_login_android", params, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        String data = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                data = jsonObject.getString("data");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code == 200) {
                            // 成功
                            if (callback != null) {
                                callback.onCallResponse(data);
                            }
                        } else {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 绑定openId
     */
    public void bindOpenId(String token, String openId, HttpResponseCallback<Boolean> callback) {
        Map<String, String> header = new HashMap<>();
        header.put("access-token", token);
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        Map<String, String> params = new HashMap<>();
        if (!TextUtils.isEmpty(openId)) {
            params.put("open_id", openId);
        }
        params.put("type", "2");
        mClient.call(HttpClient.Method.POST, mBaseUrl + "wechat/bind", params, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                boolean data = jsonObject.getBoolean("data");
                                if (callback != null) {
                                    callback.onCallResponse(data);
                                }
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code != 200) {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 解绑定openId
     */
    public void unBindOpenId(String token, HttpResponseCallback<Boolean> callback) {
        Map<String, String> header = new HashMap<>();
        header.put("access-token", token);
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        mClient.call(HttpClient.Method.POST, mBaseUrl + "wechat/unbind", null, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                boolean data = jsonObject.getBoolean("data");
                                if (callback != null) {
                                    callback.onCallResponse(data);
                                }
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code != 200) {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 解绑定openId
     */
    public void isBind(String token, HttpResponseCallback<Boolean> callback) {
        Map<String, String> header = new HashMap<>();
        header.put("access-token", token);
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        mClient.call(HttpClient.Method.GET, mBaseUrl + "wechat/is_bind", null, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                boolean data = jsonObject.getBoolean("data");
                                if (callback != null) {
                                    callback.onCallResponse(data);
                                }
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code != 200) {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 获取支持列表
     */
    public void getSupportStaff(String token, HttpResponseCallback<List<SupportBean>> callback) {
        Map<String, String> header = new HashMap<>();
        header.put("access-token", token);
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        mClient.call(HttpClient.Method.GET, mBaseUrl + "support_staff", null, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        parseListResult(200, result, callback, SupportBean.class);
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 绑定手机号
     */
    public void mobileBind(String token, String mobile, String captcha,
            HttpResponseCallback<Boolean> callback) {
        Map<String, String> header = new HashMap<>();
        header.put("access-token", token);
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        Map<String, String> params = new HashMap<>();
        params.put("mobile", mobile);
        params.put("captcha", captcha);
        mClient.call(HttpClient.Method.POST, mBaseUrl + "user/mobile_bind", params, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                boolean data = jsonObject.getBoolean("data");
                                if (callback != null) {
                                    callback.onCallResponse(data);
                                }
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code != 200) {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 使用签名绑定手机号
     */
    public void mobileBindBySign(String token, String mobile, String sign,
            HttpResponseCallback<Boolean> callback) {
        Map<String, String> header = new HashMap<>();
        header.put("access-token", token);
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        Map<String, String> params = new HashMap<>();
        params.put("mobile", mobile);
        params.put("sign", sign);
        mClient.call(HttpClient.Method.POST, mBaseUrl + "user/mobile_bind_with_sign", params,
                header, new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                boolean data = jsonObject.getBoolean("data");
                                if (callback != null) {
                                    callback.onCallResponse(data);
                                }
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code != 200) {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 更换手机号
     */
    public void mobileChange(String token, String mobile, String captcha, String sign,
            HttpResponseCallback<Boolean> callback) {
        Map<String, String> header = new HashMap<>();
        header.put("access-token", token);
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        Map<String, String> params = new HashMap<>();
        params.put("mobile", mobile);
        params.put("captcha", captcha);
        params.put("sign", sign);
        mClient.call(HttpClient.Method.POST, mBaseUrl + "user/mobile_change", params, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                boolean data = jsonObject.getBoolean("data");
                                if (callback != null) {
                                    callback.onCallResponse(data);
                                }
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code != 200) {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 修改密码
     */
    public void pwdChange(String token, String newPwd, String newPwdCheck, String sign,
            HttpResponseCallback<Boolean> callback) {
        Map<String, String> header = new HashMap<>();
        header.put("access-token", token);
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        Map<String, String> params = new HashMap<>();
        params.put("new_password", newPwd);
        params.put("new_password_check", newPwdCheck);
        params.put("sign", sign);
        mClient.call(HttpClient.Method.POST, mBaseUrl + "pwd_change", params, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                boolean data = jsonObject.getBoolean("data");
                                if (callback != null) {
                                    callback.onCallResponse(data);
                                }
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code != 200) {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 更换手机号 根据旧手机获取签名
     */
    public void mobilePrechangeByMobile(String token, String mobile, String captcha,
            HttpResponseCallback<String> callback) {
        Map<String, String> header = new HashMap<>();
        header.put("access-token", token);
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        Map<String, String> params = new HashMap<>();
        params.put("mobile", mobile);
        params.put("captcha", captcha);
        mClient.call(HttpClient.Method.POST, mBaseUrl + "user/mobile_prechange_by_mobile", params,
                header, new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        String sign = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                String data = jsonObject.getString("data");
                                jsonObject = new JSONObject(data);
                                if (jsonObject.has("sign")) {
                                    sign = jsonObject.getString("sign");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code == 200) {
                            // 成功
                            if (callback != null) {
                                callback.onCallResponse(sign);
                            }
                        } else {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 更换手机号 根据旧手机获取签名
     */
    public void mobilePrechangeByPwd(String token, String password, HttpResponseCallback<String> callback) {
        Map<String, String> header = new HashMap<>();
        header.put("access-token", token);
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        Map<String, String> params = new HashMap<>();
        params.put("password", password);
        mClient.call(HttpClient.Method.POST, mBaseUrl + "user/mobile_prechange_by_password", params,
                header, new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        String sign = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                String data = jsonObject.getString("data");
                                jsonObject = new JSONObject(data);
                                if (jsonObject.has("sign")) {
                                    sign = jsonObject.getString("sign");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code == 200) {
                            // 成功
                            if (callback != null) {
                                callback.onCallResponse(sign);
                            }
                        } else {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 更换手机号 根据旧手机获取签名
     */
    public void allowLoginWithQrCode(String token, String qrcode, HttpResponseCallback<String> callback) {
        Map<String, String> header = new HashMap<>();
        header.put("access-token", token);
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        Map<String, String> params = new HashMap<>();
        params.put("allow", "1");
        params.put("qr_code", qrcode);
        mClient.call(HttpClient.Method.GET, mBaseUrl + "allow", params,
                header, new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        String data = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                data = jsonObject.getString("data");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code == 200) {
                            // 成功
                            if (callback != null) {
                                callback.onCallResponse(data);
                            }
                        } else {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 更换手机号 根据旧手机获取签名
     */
    public void getLanyingLinkInfo(String link, HttpResponseCallback<String> callback) {
        Map<String, String> header = new HashMap<>();
        Map<String, String> params = new HashMap<>();
        params.put("link", link);
        mClient.call(HttpClient.Method.GET, "https://lanying.link/" + "info", params,
                header, new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        String data = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                data = jsonObject.getString("data");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code == 200) {
                            // 成功
                            if (callback != null) {
                                callback.onCallResponse(data);
                            }
                        } else {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 更换手机号 根据旧手机获取签名
     */
    public void getSecretInfo(String code, HttpResponseCallback<String> callback) {
        Map<String, String> header = new HashMap<>();
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        mClient.call(HttpClient.Method.GET, mBaseUrl + "secret_info", params,
                header, new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        String data = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                data = jsonObject.getString("data");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code == 200) {
                            // 成功
                            if (callback != null) {
                                callback.onCallResponse(data);
                            }
                        } else {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 检查用户名
     */
    public void checkName(String userName, HttpResponseCallback<Boolean> callback) {
        Map<String, String> header = new HashMap<>();
        header.put("app_id", SharePreferenceUtils.getInstance().getAppId());
        Map<String, String> params = new HashMap<>();
        params.put("username", userName);
        mClient.call(HttpClient.Method.GET, mBaseUrl + "user/name_check", params, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                if (callback != null) {
                                    callback.onCallResponse(jsonObject.getBoolean("data"));
                                }
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code != 200) {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 绑定证书名和用户名id
     */
    public void notifierBind(String token, String deviceToken, String appId, String userId,
            String notifierName, HttpResponseCallback<Boolean> callback) {
        Map<String, String> header = new HashMap<>();
        header.put("access-token", token);
        header.put("access-app_id", appId);
        header.put("app_id", appId);
        Map<String, String> params = new HashMap<>();
        params.put("app_id", appId);
        params.put("user_id", userId);
        params.put("notifier_name", notifierName);
        params.put("device_token", deviceToken);
        mClient.call(HttpClient.Method.POST, "https://butler.maximtop.com/notifier/bind", params,
                header, new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                boolean data = jsonObject.getBoolean("data");
                                if (callback != null) {
                                    callback.onCallResponse(data);
                                }
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code != 200) {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 绑定证书名和用户名id
     */
    public void uploadPushInfo(String token, String deviceToken, String appId,
            HttpResponseCallback<Boolean> callback) {
        Map<String, String> header = new HashMap<>();
        header.put("access-token", token);
        header.put("access-app_id", appId);
        header.put("app_id", appId);
        Map<String, String> params = new HashMap<>();
        params.put("uuid", UUID.randomUUID().toString());
        params.put("device_token", deviceToken);
        mClient.call(HttpClient.Method.POST,
                "https://butler.maximtop.com/notifier/upload_push_info", params, header,
                new HttpCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        if (TextUtils.isEmpty(result)) {
                            if (callback != null) {
                                callback.onCallFailure(-1, "", new Throwable());
                            }
                            return;
                        }
                        int code = -1;
                        String error = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("code")) {
                                code = jsonObject.getInt("code");
                            }
                            if (jsonObject.has("message")) {
                                error = jsonObject.getString("message");
                            }
                            if (jsonObject.has("data")) {
                                boolean data = jsonObject.getBoolean("data");
                                if (callback != null) {
                                    callback.onCallResponse(data);
                                }
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code != 200) {
                            // 失败
                            if (callback != null) {
                                callback.onCallFailure(code, error, new Throwable(error));
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        if (callback != null) {
                            callback.onCallFailure(errorCode, errorMsg, new Throwable());
                        }
                    }
                });
    }

    /**
     * 解析返回结果
     * 
     * @param result
     */
    private <T> void parseResult(String result, HttpResponseCallback<T> callback, Class<T> clazz,
            String key) {
        if (TextUtils.isEmpty(result)) {
            if (callback != null) {
                callback.onCallFailure(-1, "", new Throwable());
            }
            return;
        }
        int code = -1;
        String error = "";
        T t = null;
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.has("code")) {
                code = jsonObject.getInt("code");
            }
            if (jsonObject.has("message")) {
                error = jsonObject.getString("message");
            }
            if (jsonObject.has(key)) {
                t = mGson.fromJson(jsonObject.getString(key), clazz);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (code == 200) {
            // 成功
            if (callback != null) {
                callback.onCallResponse(t);
            }
        } else {
            // 失败
            if (callback != null) {
                callback.onCallFailure(code, error, new Throwable(error));
            }
        }
    }

    /**
     * 解析返回结果
     *
     * @param result
     */
    <T> void parseListResult(int successCode, String result, HttpResponseCallback<List<T>> callback,
            Class<T> clazz) {
        if (TextUtils.isEmpty(result)) {
            if (callback != null) {
                callback.onCallFailure(-1, "", new Throwable());
            }
            return;
        }
        int code = -1;
        String error = "";
        List<T> t = null;
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.has("code")) {
                code = jsonObject.getInt("code");
            }
            if (jsonObject.has("message")) {
                error = jsonObject.getString("message");
            }
            if (jsonObject.has("data")) {
                t = mGson.fromJson(jsonObject.getString("data"), new GsonParameterizedType(clazz));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (code == successCode) {
            // 成功
            if (callback != null) {
                callback.onCallResponse(t);
            }
        } else {
            // 失败
            if (callback != null) {
                callback.onCallFailure(code, error, new Throwable(error));
            }
        }
    }
}
