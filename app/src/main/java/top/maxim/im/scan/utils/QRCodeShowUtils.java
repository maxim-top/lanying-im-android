
package top.maxim.im.scan.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import top.maxim.im.R;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.common.utils.FileUtils;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.scan.QRCodeUtil;
import top.maxim.im.scan.bean.QrCodeBean;

/**
 * Description :二维码utils
 */
public class QRCodeShowUtils {

    public static final String TAG = QRCodeShowUtils.class.getSimpleName();

    /**
     * 设置整体布局，及二维码图片的宽高
     *
     * @param linearLayout 整体布局（外层布局）
     * @param qrImage 二维码布局
     */
    public static void setQRCodeWidth(LinearLayout linearLayout, ImageView qrImage) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        int distance25 = ScreenUtils.dp2px(25);
        // 设置上，左右边距
        params.setMargins(distance25, distance25, distance25, 0);
        linearLayout.setLayoutParams(params);
        // 设置下边距
        linearLayout.setPadding(0, 0, 0, distance25);
        linearLayout.requestFocus();
        linearLayout.setFocusable(true);
        // 获取屏幕信息 ,设定二维码的宽高
        int distance = ScreenUtils.widthPixels - ScreenUtils.dp2px(180);
        LinearLayout.LayoutParams ps = new LinearLayout.LayoutParams(distance, distance);
        ps.topMargin = distance25;
        ps.bottomMargin = ScreenUtils.dp2px(20);
        ps.gravity = Gravity.CENTER;
        qrImage.setLayoutParams(ps);
    }

    /**
     * 本地生成Roster二维码url
     *
     * @param rosterId
     */
    public static String generateRosterQRCode(String rosterId) {
        QrCodeBean bean = new QrCodeBean();
        bean.setSource(QRCodeConfig.SOURCE.APP);
        bean.setAction(QRCodeConfig.ACTION.PROFILE);
        Map<String, String> map = new HashMap<>();
        map.put("uid", rosterId);
        bean.setInfo(map);
        return new Gson().toJson(bean);
    }

    /**
     * 本地生成群组二维码url
     *
     * @param groupId 群组id
     */
    public static String generateGroupQRCode(String groupId, String qr_info) {
        QrCodeBean bean = new QrCodeBean();
        bean.setSource(QRCodeConfig.SOURCE.APP);
        bean.setAction(QRCodeConfig.ACTION.GROUP);
        Map<String, String> map = new HashMap<>();
        map.put("group_id", groupId);
        map.put("info", qr_info);
        bean.setInfo(map);
        return new Gson().toJson(bean);
    }

    /**
     * 根据url 生成图片
     * 
     * @param codeUrl
     * @return Drawable
     */
    public static Drawable generateDrawable(String codeUrl) {
        return QRCodeUtil.getQrcodeBitmap(codeUrl, 0, null);
    }

    /**
     * 将本地图片文件转换成可解码二维码的 Bitmap。为了避免图片太大，这里对图片进行了压缩。
     *
     * @param path 本地图片文件路径
     */
    public static Bitmap getDecodeAbleBitmap(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);// 获取这个图片的宽和高
            // 计算缩放比
            int be = (int)(options.outHeight / (float)200);
            int ys = options.outHeight % 200;// 求余数
            float fe = ys / (float)200;
            if (fe >= 0.5)
                be = be + 1;
            if (be <= 0)
                be = 1;
            options.inSampleSize = be;

            // 重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 识别本地图片
     * 
     * @param scanBitmap 识别的图片
     * @return 解析结果
     */
    public static String decodeBitmap(Bitmap scanBitmap) {
        if (scanBitmap == null)
            return null;
        int width = scanBitmap.getWidth();
        int height = scanBitmap.getHeight();
        int[] data = new int[(int)(height * width * 1.5)];
        scanBitmap.getPixels(data, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            Result result = reader.decode(bitmap1, null);
            if (result != null && !TextUtils.isEmpty(result.getText()))
                return result.getText();
            return "";
        } catch (NotFoundException | FormatException | ChecksumException e) {
            e.printStackTrace();
            Result result = getQrCode(scanBitmap);
            if (result != null && !TextUtils.isEmpty(result.getText()))
                return result.getText();
            return "";

        } finally {
            // if (!scanBitmap.isRecycled()) {
            // scanBitmap.recycle();
            // }
        }
        // return null;
    }

    /**
     * 识别是否是二维码 如果是返回识别后的字符串
     * 
     * @param loadBitmap 图片加载完的bitmap
     * @return String
     */
    private static Result getQrCode(Bitmap loadBitmap) {

        if (loadBitmap == null) {
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); // 设置二维码内容的编码

        // 防止图片bitmap过大 无法是被二维码 所以进行压缩处理
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        loadBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false; // 获取新的大小
        int sampleSize = (int)(loadBitmap.getWidth() / (float)200);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap scanBitmap = BitmapFactory.decodeStream(isBm, null, options);
        if (scanBitmap == null) {
            return null;
        }
        int width = scanBitmap.getWidth();
        int height = scanBitmap.getHeight();
        int[] data = new int[height * width];
        scanBitmap.getPixels(data, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);
        } catch (NotFoundException | FormatException | ChecksumException e) {
            e.printStackTrace();
        } finally {
            if (!scanBitmap.isRecycled()) {
                scanBitmap.recycle();
            }
        }
        return null;
    }

    // 截图
    public static Bitmap takeScreenShot(View mView) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(mView.getWidth(), mView.getHeight(),
                    Bitmap.Config.RGB_565);
            if (bitmap == null) {
                return null;
            }
            Canvas canvas = new Canvas(bitmap);
            mView.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "takeScreenShot: " + e.getMessage());
        }
        return null;
    }

    /**
     * 保存图片
     */
    public static void saveImageToGallery(Context context, Bitmap bitmap) {
        File file = new File(getPath());
        if (!file.exists()) {
            if (!file.mkdirs()) {
                ToastUtil.showTextViewPrompt(context.getString(R.string.failed_to_save));
                return;
            }
        }

        if (!FileUtils.checkSDCard()) {
            ToastUtil.showTextViewPrompt("sdcard 空间不足");
            return;
        }

        File fileName = new File(file, System.currentTimeMillis() + ".jpg");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        ToastUtil.showTextViewPrompt(context.getString(R.string.saved_to_system_album));
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + fileName.getAbsolutePath())));
    }

    /**
     * 获取截图图片存储的路径名
     */
    private static String getPath() {
        String packageName = AppContextUtils.getPackageName(AppContextUtils.getAppContext());
        String[] str = packageName.split("\\.");
        String dirName = str[str.length - 1];
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + dirName + File.separator + "camera";
    }

}
