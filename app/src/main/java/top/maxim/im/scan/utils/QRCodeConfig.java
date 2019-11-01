
package top.maxim.im.scan.utils;

/**
 * Description : 二维码常量 Created by mango on 2019-11-01.
 */
public interface QRCodeConfig {

    // source
    interface SOURCE {
        String APP = "app";

        String CONSOLE = "console";
    }

    // source
    interface ACTION {

        String PROFILE = "profile";

        String GROUP = "group";

        String LOGIN = "login";

        String UPLOAD_DEVICE_TOKEN = "upload_device_token";

        String APP = "app";
    }
}
