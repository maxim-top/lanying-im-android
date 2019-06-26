
package top.maxim.im.scan;

import android.os.Handler;

import com.google.zxing.Result;

import top.maxim.im.scan.camera.CameraManager;

public interface QrcodeScanner {

    int decode = 1;

    int decode_failed = 2;

    int decode_succeeded = 3;

    int quit = 4;

    int restart_preview = 5;

    CameraManager getCameraManager();

    Handler getHandler();

    void handleDecode(Result rawText);

    void bitmapBright(int bright);

}
