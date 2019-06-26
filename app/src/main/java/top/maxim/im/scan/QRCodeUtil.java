
package top.maxim.im.scan;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;

import top.maxim.im.common.utils.AppContextUtils;

public final class QRCodeUtil {

    private static final String TAG = QRCodeUtil.class.getSimpleName();

    private static final int BLACK = 0xFF000000;

    private static final int WHITE = 0xFFFFFFFF;

    public static Bitmap getQrcodeBitmap(String msg, int width, int height, int margin,
            Bitmap logo) {

        QRCodeWriter qw = new QRCodeWriter();
        try {
            HashMap<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.MARGIN, margin);
            hints.put(EncodeHintType.ERROR_CORRECTION,
                    logo == null ? ErrorCorrectionLevel.L : ErrorCorrectionLevel.H);

            BitMatrix matrix = qw.encode(msg, BarcodeFormat.QR_CODE, width, height, hints);
            return toBitmap(matrix, logo);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    public static String decodeQrcode(Bitmap bmp) {
        String contents = null;

        int[] intArray = new int[bmp.getWidth() * bmp.getHeight()];
        // copy pixel data from the Bitmap into the 'intArray' array
        bmp.getPixels(intArray, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bmp.getWidth(), bmp.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Reader reader = new MultiFormatReader();// use this otherwise
                                                // ChecksumException
        try {
            Result result = reader.decode(bitmap);
            contents = result.getText();
            // byte[] rawBytes = result.getRawBytes();
            // BarcodeFormat format = result.getBarcodeFormat();
            // ResultPoint[] points = result.getResultPoints();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return contents;

    }

    private static Bitmap toBitmap(BitMatrix matrix, Bitmap logo) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if (bm != null) {
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixels[y * width + x] = matrix.get(x, y) ? BLACK : WHITE;
                }
            }
            bm.setPixels(pixels, 0, width, 0, 0, width, height);
            if (logo != null) {
                Canvas cvs = new Canvas(bm);
                int left = bm.getWidth() / 2 - bm.getWidth() / 10;
                int right = left + bm.getWidth() / 5;
                int top = bm.getHeight() / 2 - bm.getHeight() / 10;
                int bottom = top + bm.getHeight() / 5;
                cvs.drawBitmap(logo, null, new Rect(left, top, right, bottom),
                        new Paint(Paint.ANTI_ALIAS_FLAG));
            }
        }
        return bm;
    }

    public static Drawable getQrcodeBitmap(String msg, int margin, Bitmap logo) {

        QRCodeWriter qw = new QRCodeWriter();
        try {
            HashMap<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.MARGIN, margin);
            hints.put(EncodeHintType.ERROR_CORRECTION,
                    logo == null ? ErrorCorrectionLevel.L : ErrorCorrectionLevel.H);

            BitMatrix matrix = qw.encode(msg, BarcodeFormat.QR_CODE, 10, 10, hints);
            int height = matrix.getHeight();
            int width = matrix.getWidth();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < width; y++) {
                    bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            if (logo != null) {
                Canvas cvs = new Canvas(bmp);
                int left = bmp.getWidth() / 2 - bmp.getWidth() / 10;
                int right = left + bmp.getWidth() / 5;
                int top = bmp.getHeight() / 2 - bmp.getHeight() / 10;
                int bottom = top + bmp.getHeight() / 5;
                cvs.drawBitmap(logo, null, new Rect(left, top, right, bottom),
                        new Paint(Paint.ANTI_ALIAS_FLAG));
            }

            BitmapDrawable qrCodeDrawable = new BitmapDrawable(
                    AppContextUtils.getAppContext().getResources(), bmp);
            qrCodeDrawable.setFilterBitmap(false);
            return qrCodeDrawable;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }
}
