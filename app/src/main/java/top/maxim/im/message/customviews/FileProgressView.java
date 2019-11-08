
package top.maxim.im.message.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import top.maxim.im.R;

/**
 * Description : 文件上传进度条
 */
public class FileProgressView extends View {

    /**
     * 进度条
     */
    private Paint mProgressPaint;

    /* 当前进度 */
    private long mProgress;

    private int mAutoProgress;

    public FileProgressView(Context paramContext) {
        this(paramContext, null);
    }

    public FileProgressView(Context paramContext, AttributeSet paramAttributeSet) {
        this(paramContext, paramAttributeSet, -1);
    }

    public FileProgressView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        init();
    }

    private void init() {
        setBackgroundColor(getResources().getColor(R.color.c1));
        mProgressPaint = new Paint();
        mProgressPaint.setColor(getResources().getColor(R.color.c2));
        mProgressPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int width = getMeasuredWidth(), height = getMeasuredHeight();
        int left = width, right = 0;
        /** 最长进度 */
        long mMaxDuration;
        long maxDuration = mMaxDuration = 100;
        boolean hasOutDuration = false;
        hasOutDuration = mProgress > mMaxDuration;
        if (hasOutDuration)
            maxDuration = mProgress;

        right = (int)(mProgress * 1.0F / maxDuration * width);

        // 画进度
        canvas.drawRect(0, 0.0F, right, height, mProgressPaint);

    }

    public void setCurrent(long progress) {
        mProgress = progress;
        postInvalidate();
    }

    public void autoProgress() {
        setVisibility(VISIBLE);
        post(new Runnable() {
            @Override
            public void run() {
                mAutoProgress += 5;
                setCurrent(mAutoProgress);
                if (mAutoProgress <= 100) {
                    postDelayed(this, 10);
                } else {
                    removeCallbacks(this);
                    setVisibility(INVISIBLE);
                }
            }
        });
    }
}
