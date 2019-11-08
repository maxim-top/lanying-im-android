/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.maxim.im.scan;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.ByteArrayOutputStream;

final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final QrcodeScanner activity;

    private boolean running = true;

    private MultiFormatReader multiFormatReader;

    DecodeHandler(QrcodeScanner activity) {
        multiFormatReader = new MultiFormatReader();
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        switch (message.what) {
            case QrcodeScanner.decode:
                if (message.obj != null) {
                    decode((byte[])message.obj, message.arg1, message.arg2);
                }
                break;
            case QrcodeScanner.quit:
                running = false;
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it
     * took. For efficiency, reuse the same reader objects from one decode to
     * the next.
     *
     * @param data The YUV preview frame.
     * @param width The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {

        long start = System.currentTimeMillis();
        Result rawResult = null;

        int length = data.length;
        byte[] rotatedData = new byte[length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ( x + y * width >= length){
                    int len = x + y * width >= length ? length-1:x + y * width;
                    if(x * height + height - y - 1 >=length){
                        rotatedData[x * height + height - y - 1 >=length ? length -1 :x * height + height - y - 1] = data[len];
                    }else{
                        rotatedData[x * height + height - y - 1 ] = data[len];
                    }
                }else{
                    if(x * height + height - y - 1 >=length){
                        rotatedData[x * height + height - y - 1 >=length ? length -1 :x * height + height - y - 1] = data[x + y * width];
                    }else{
                        rotatedData[x * height + height - y - 1 ] = data[x + y * width];
                    }
                }
            }
        }
        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;
        data = rotatedData;
        /*************************************/
        PlanarYUVLuminanceSource source = null;
        if(activity != null && activity.getCameraManager() != null){
            // 构造基于平面的YUV亮度源，即包含二维码区域的数据源
            source = activity.getCameraManager()
                    .buildLuminanceSource(data, width, height);
        }

        if (source != null) {
            // 构造二值图像比特流，使用HybridBinarizer算法解析数据源
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                // 采用MultiFormatReader解析图像，可以解析多种数据格式
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
                Log.d(TAG, "Found barcode in " +re.getMessage());
            } finally {
                multiFormatReader.reset();
            }
        }

        Handler handler = activity != null? activity.getHandler() : null;
        Bundle bundle = new Bundle();
        if (rawResult != null) {
            // Don't log the barcode contents for security.
            long end = System.currentTimeMillis();
            Log.d(TAG, "Found barcode in " + (end - start) + " ms" + "---rawResult"+rawResult);
            if (handler != null && source != null) {
                Message message = Message.obtain(handler,
                        QrcodeScanner.decode_succeeded, rawResult);
                bundleThumbnail(source, bundle);
                message.setData(bundle);
                message.sendToTarget();
            }
        } else {
            if (handler != null && source != null) {
                Message message = Message.obtain(handler, QrcodeScanner.decode_failed);
                message.arg1 = getBright(yuv2Bitmap(source)) ;
                message.sendToTarget();
            }
        }
    }

    private static void bundleThumbnail(PlanarYUVLuminanceSource source,
                                        Bundle bundle) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height,
                Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        bundle.putByteArray(DecodeThread.BARCODE_BITMAP, out.toByteArray());
        bundle.putFloat(DecodeThread.BARCODE_SCALED_FACTOR, (float) width
                / source.getWidth());
    }

    private  Bitmap yuv2Bitmap(PlanarYUVLuminanceSource source) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height,
                Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        return bitmap;
    }


    /**
     * 判断图片的亮度
     * @param bm bitmap
     * @return 图片的亮度
     */
    private int getBright(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        int r, g, b;
        int count = 0;
        int bright = 0;
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                count++;
                int localTemp = bm.getPixel(i, j);
                r = (localTemp | 0xff00ffff) >> 16 & 0x00ff;
                g = (localTemp | 0xffff00ff) >> 8 & 0x0000ff;
                b = (localTemp | 0xffffff00) & 0x0000ff;
                bright = (int) (bright + 0.299 * r + 0.587 * g + 0.114 * b);
            }
        }
        return count == 0 ? 0 : bright / count;
    }
}
