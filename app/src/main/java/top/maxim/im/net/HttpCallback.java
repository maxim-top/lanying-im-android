
package top.maxim.im.net;

/**
 * 网络代理回调
 */
public interface HttpCallback<T> {

    /**
     * 正确返回
     *
     * @param result 具体实例
     */
    void onResponse(T result);

    /**
     * 错误返回
     *
     * @param errorCode 错误码
     * @param errorMsg 错误描述
     * @param t 异常
     */
    void onFailure(int errorCode, String errorMsg, Throwable t);
}
