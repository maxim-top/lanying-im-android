
package top.maxim.im.net;

import java.util.Map;

public interface HttpClient {

    /**
     * network caller.
     *
     * @param method @see {@link Method}
     * @param url url
     * @param params url params
     * @param callback callback
     */
    void call(int method, String url, Map<String, String> params,
            HttpCallback<String> callback);

    /**
     * network caller.
     *
     * @param method @see {@link Method}
     * @param url url
     * @param params url params
     * @param header url header
     * @param callback callback
     */
    void call(int method, String url, Map<String, String> params, Map<String, String> header,
            HttpCallback<String> callback);

    interface Method {
        int DEPRECATED_GET_OR_POST = -1;

        int GET = 0;

        int POST = 1;

        int PUT = 2;

        int DELETE = 3;

        int HEAD = 4;

        int OPTIONS = 5;

        int TRACE = 6;

        int PATCH = 7;
    }
}
