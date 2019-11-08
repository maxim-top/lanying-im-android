
package top.maxim.im.common.utils.video;

import android.graphics.Bitmap;

interface IRecorder2 {

    /**
     * 开始录制
     *
     * @param wrapper 相机参数
     * @param screenWidth
     * @param screenHeight
     * @param orient 方向 1 up,2 left ,3 down,4 right
     * @return
     */
    boolean startRecord(CameraWrapper wrapper, int screenWidth, int screenHeight, int orient);

    boolean isRecording();

    /**
     * 结束录制
     */
    String endRecord(CameraWrapper wrapper);

    /**
     * 捕获图片
     */
    void takePicture(CameraWrapper wrapper, ITakePictureCallback callback, int orient);

    /**
     * 录制的最大时间
     *
     * @return millis
     */
    long getMaxDuration();

    interface ITakePictureCallback {
        /**
         * 生成图片
         *
         * @param bmp
         */
        void onPicture(Bitmap bmp);
    }
}
