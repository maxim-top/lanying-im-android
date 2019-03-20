
package top.maxim.im.common.bean;

/**
 * Description : 查看大图bean
 */
public class PhotoViewBean extends BaseBean {

    /**
     * 原图或者压缩图的路径
     */
    private String localPath;

    /**
     * 图片缩略图地址
     */
    private String thumbLocalPath;

    /**
     * 图片网络地址
     */
    private String httpUrl;

    /**
     * 图片缩略图地址
     */
    private String thumbHttpUrl;

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getThumbLocalPath() {
        return thumbLocalPath;
    }

    public void setThumbLocalPath(String thumbLocalPath) {
        this.thumbLocalPath = thumbLocalPath;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
    }

    public String getThumbHttpUrl() {
        return thumbHttpUrl;
    }

    public void setThumbHttpUrl(String thumbHttpUrl) {
        this.thumbHttpUrl = thumbHttpUrl;
    }
}
