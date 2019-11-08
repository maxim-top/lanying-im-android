
package top.maxim.im.net;

import android.text.TextUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DefaultOkhttpClient implements HttpClient {

    private static final String PARAMSEN_CODING = "UTF-8";

    private static final String PROTOCOL_CONTENT_TYPE = "application/json; charset=utf-8";

    private final OkHttpClient mHttpClient;

    public DefaultOkhttpClient() {
        mHttpClient = new OkHttpClient.Builder().writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).connectTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true).build();
    }

    @Override
    public void call(int method, String url, Map<String, String> params,
            HttpCallback<String> callback) {
        call(method, url, params, null, callback);
    }

    @Override
    public void call(int method, String url, Map<String, String> params, Map<String, String> header,
            final HttpCallback<String> callback) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Request.Builder builder = new Request.Builder();
        setRequestParameters(builder, method, url, params);
        if (null != header) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                String value = entry.getValue();
                if (null == value) {
                    continue;
                }
                builder.addHeader(entry.getKey(), value);
            }
        }

        final Request request = builder.build();
        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (callback != null) {
                        callback.onResponse(null != body ? body.string() : "");
                    }
                } else {
                    if (callback != null) {
                        callback.onFailure(response.code(), response.message(), null);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                String error = e.getMessage();
                Throwable cause = e.getCause();
                if (null != cause) {
                    error = cause.toString();
                }
                if (null == error) {
                    error = "unknow error.";
                }

                callback.onFailure(-1, error, e);
            }
        });
    }

    private void setRequestParameters(Request.Builder builder, int method, String url,
            Map<String, String> params) {
        switch (method) {
            case Method.DEPRECATED_GET_OR_POST:
                byte[] postBody = getBody(params);
                if (postBody != null) {
                    builder.post(
                            RequestBody.create(MediaType.parse(PROTOCOL_CONTENT_TYPE), postBody));
                }
                break;
            case Method.GET:
                builder.get();
                break;
            case Method.DELETE:
                builder.delete();
                break;
            case Method.POST:
                builder.post(createRequestBody(params));
                break;
            case Method.PUT:
                builder.put(createRequestBody(params));
                break;
            case Method.HEAD:
                builder.head();
                break;
            case Method.OPTIONS:
                builder.method("OPTIONS", null);
                break;
            case Method.TRACE:
                builder.method("TRACE", null);
                break;
            case Method.PATCH:
                builder.patch(createRequestBody(params));
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }

        if (method == Method.PATCH || method == Method.POST || method == Method.PUT) {
            builder.addHeader("content-type", PROTOCOL_CONTENT_TYPE);
            builder.url(url);
        } else {
            builder.url(getUrl(url, params));
        }
    }

    private RequestBody createRequestBody(Map<String, String> params) {
        final byte[] body = getBody(params);
        if (body == null)
            return null;

        return RequestBody.create(MediaType.parse(PROTOCOL_CONTENT_TYPE), body);
    }

    private byte[] getBody(Map<String, String> params) {
        if (params != null && params.size() > 0) {
            return byteParameters(params);
        }
        return null;
    }

    private String getUrl(String url, Map<String, String> params) {
        if (params != null && params.size() > 0) {
            if (url.contains("?")) {
                return url + "&" + encodeParameters(params);
            }
            return url + "?" + encodeParameters(params);
        }
        return url;
    }

    private String encodeParameters(Map<String, String> params) {
        try {
            StringBuilder encodedParams = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), PARAMSEN_CODING));
                encodedParams.append('=');
                encodedParams.append(
                        URLEncoder.encode(String.valueOf(entry.getValue()), PARAMSEN_CODING));
                encodedParams.append('&');
            }
            return encodedParams.toString();
        } catch (Exception uee) {
            throw new RuntimeException("Encoding not supported: " + PARAMSEN_CODING, uee);
        }
    }

    private byte[] byteParameters(Map<String, String> params) {
        try {
            return new JSONObject(params).toString().getBytes(PARAMSEN_CODING);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + PARAMSEN_CODING, uee);
        }
    }
}
