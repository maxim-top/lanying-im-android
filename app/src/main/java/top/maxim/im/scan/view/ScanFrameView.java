
package top.maxim.im.scan.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import top.maxim.im.R;
import top.maxim.im.common.utils.ScreenUtils;

public class ScanFrameView extends View {

    // 扫描框高度
    private int scanRectHeight;

    private Paint mPaint;

    private Rect mFrameRect;

    private Rect top, bottom, left, right;

    private Bitmap mBorderBitmap;

    public ScanFrameView(Context context) {
        this(context, null);
    }

    public ScanFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 先画内部矩形 int left, int top, int right, int bottom
        // mPaint.setColor(Color.BLUE);
        // mPaint.setStyle(Paint.Style.STROKE);
        // canvas.drawRect(mFrameRect,mPaint);
        // 小米8 SE中，通过ScreenUtil.heightPixels获取到的高度比实际高度要小，故此处通过getHeight获取实际高度
        int screenHeight = getHeight();
        // 画灰色的阴影部分
        if (top == null) {
            top = new Rect(0, 0, ScreenUtils.widthPixels, mFrameRect.top);
        }
        canvas.drawRect(top, mPaint);// 上部分

        if (bottom == null) {
            bottom = new Rect(0, mFrameRect.bottom, ScreenUtils.widthPixels, screenHeight);
        }
        canvas.drawRect(bottom, mPaint);// 下部分

        if (left == null) {
            left = new Rect(0, mFrameRect.top, mFrameRect.left, mFrameRect.bottom);
        }
        canvas.drawRect(left, mPaint);// 左边部分

        if (right == null) {
            right = new Rect(mFrameRect.right, mFrameRect.top, ScreenUtils.widthPixels,
                    mFrameRect.bottom);
        }
        canvas.drawRect(right, mPaint);// 右部分

        // 画扫描框四个角
        if (mBorderBitmap != null && !mBorderBitmap.isRecycled()) {
            canvas.drawBitmap(mBorderBitmap, null, mFrameRect, mPaint);
        }
    }

    private void init() {
        // 初始化画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.parseColor("#99000000"));
        mPaint.setStyle(Paint.Style.FILL);

        // 计算扫描框位置及大小（依据UI图中扫描框距左右上的间距来进行确定其大小）
        int leftOffset = ScreenUtils.dp2px(58);
        int rightOffset = ScreenUtils.widthPixels - leftOffset;
        int topOffset = ScreenUtils.dp2px(121);
        if (ScreenUtils.heightPixels < 1920) {
            topOffset = (int)(topOffset * 0.8);
        }
        // 如果是sdk版本大于4.4，设置沉浸式之后需要加上状态栏的高度
        int extraHeight = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            extraHeight = ScreenUtils.getStatusBarHeight();
        }
        scanRectHeight = rightOffset - leftOffset;
        mFrameRect = new Rect(leftOffset, topOffset + extraHeight, rightOffset,
                topOffset + scanRectHeight + extraHeight);
        mBorderBitmap = getCornerBitmap(getResources().getDrawable(R.drawable.bg_scan_border));
    }

    /**
     * 获取扫一扫四个角的Bitmap
     */
    private Bitmap getCornerBitmap(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    public int getFrameHeight() {
        return scanRectHeight == 0 ? ScreenUtils.widthPixels - 2 * ScreenUtils.dp2px(58)
                : scanRectHeight;
    }

    public void onDestroy() {
        if (mBorderBitmap != null) {
            mBorderBitmap.recycle();
            mBorderBitmap = null;
        }
    }

    public Rect getFrameRect() {
        return mFrameRect;
    }
}
