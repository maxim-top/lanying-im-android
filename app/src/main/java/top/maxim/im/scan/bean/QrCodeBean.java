
package top.maxim.im.scan.bean;

import top.maxim.im.common.bean.BaseBean;

/**
 * Description : 二维码bean Created by mango on 2019-10-30.
 * 二维码数据格式示例
 * {
 *  "source" : "app"
 *  "action" : "roster"
 *  "info": {
 *  }
 * }
 *
 * 1.source类型说明 |  行为类型说明
 * app :  profile
 * app :  group
 * app :  login
 *
 * console : login
 * console : upload_device_token
 * console : app
 *
 * 2. 业务场景说明：（功能，二维码数据内容字段定义）
 * 加好友  uid
 * 加群    group_id  info
 * 通过后台登录  app_name app_id uid username
 * 通过后台获取app_id  app_name app_id
 * 上传devicetoken到后台   platform_type info
 *
 *
 * platform_type: 1 苹果 2 华为 3 小米 4 魅族 5 VIVO  6 OPPO
 */
public class QrCodeBean extends BaseBean {

    private String resource;

    private String action;

    private String info;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
