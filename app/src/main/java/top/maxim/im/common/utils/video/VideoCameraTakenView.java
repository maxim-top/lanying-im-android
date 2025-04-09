
package top.maxim.im.common.utils.video;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import top.maxim.im.common.utils.ScreenUtils;

public class VideoCameraTakenView extends View {

    private OnViewActionListener actionListener;

    private int innerRadiusMax, outerRadiusMax;

    private int innerRadiusMin, outerRadiusMin;

    private int innerRadius = 0;

    private int outerRadius = 0;

    private int innerStep = 0, outerStep = 0;

    private long progress = 0;

    private boolean isDown = false;

    private boolean isRecording = false, isShrink = false;

    private boolean isRecordStart = false;

    private long startRecordTime;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private RectF arcRect = new RectF();

    private Path arcPath = new Path();

    /**
     * 结束录制
     */
    private Runnable recordEndRunnable = new Runnable() {
        @Override
        public void run() {
            isRecording = false;
            if (actionListener != null) {
                actionListener.onStopRecord();
            }
        }
    };

    /**
     * 计算进度
     */
    private Runnable scaleRunnable = new Runnable() {
        @Override
        public void run() {
            int radius1 = innerRadius;
            int radius2 = outerRadius;
            if (isDown) {
                if (radius1 > innerRadiusMin) {
                    radius1 -= innerStep;
                    if (radius1 < innerRadiusMin) {
                        radius1 = innerRadiusMin;
                    }
                }
                if (radius2 < outerRadiusMax) {
                    radius2 += outerStep;
                    if (radius2 > outerRadiusMax) {
                        radius2 = outerRadiusMax;
                    }
                }

                if (radius1 != innerRadiusMin || radius2 != outerRadiusMax) {
                    post(this);
                }
            } else {
                if (radius1 < innerRadiusMax) {
                    radius1 += innerStep;
                    if (radius1 > innerRadiusMax) {
                        radius1 = innerRadiusMax;
                    }
                }
                if (radius2 > outerRadiusMin) {
                    radius2 -= outerStep;
                    if (radius2 < outerRadiusMin) {
                        radius2 = outerRadiusMin;
                    }
                }

                if (radius1 != innerRadiusMax || radius2 != outerRadiusMin) {
                    post(this);
                } else {
                    isShrink = false;
                    // 录制和拍照
                    if (!isRecordStart) {
                        removeCallbacks(recordStartRunnable);
                        if (actionListener != null) {
                            actionListener.onTakePicture();
                        }
                    } else {
                        if (isRecording) {
                            // post(recordEndRunnable);
                            isRecording = false;
                            if (actionListener != null) {
                                actionListener.onStopRecord();
                            }
                        }
                    }
                }
            }
            innerRadius = radius1;
            outerRadius = radius2;
            invalidate();
        }
    };

    /**
     * 开始录制
     */
    private Runnable recordStartRunnable = new Runnable() {
        @Override
        public void run() {
            post(scaleRunnable);
            isRecordStart = true;
            // invalidate();
            if (actionListener != null) {
                isRecording = actionListener.onStartRecord();
            }
            startRecordTime = System.currentTimeMillis();
        }
    };

    public VideoCameraTakenView(Context context) {
        this(context, null);
    }

    public VideoCameraTakenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAlpha(255);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(ScreenUtils.dp2px(5f));
        progressPaint.setColor(Color.parseColor("#DB0033"));

        innerRadiusMax = ScreenUtils.dp2px(27);
        innerRadiusMin = ScreenUtils.dp2px(18.5f);
        outerRadiusMax = ScreenUtils.dp2px(54.5f);
        outerRadiusMin = ScreenUtils.dp2px(35f);
        innerRadius = innerRadiusMax;
        outerRadius = outerRadiusMin;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = outerRadiusMax * 2;
        setMeasuredDimension(width, width);
        int gap = ScreenUtils.dp2px(5f) / 2;
        arcRect.set(gap, gap, width - gap, width - gap);
        arcPath.reset();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        paint.setColor(Color.parseColor("#7FFFFFFF"));
        canvas.drawCircle(centerX, centerY, outerRadius, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerY, innerRadius, paint);
        if (!isShrink && isRecording && actionListener != null
                && actionListener.getRecordDuration() != 0) {
            progress = 360 * (System.currentTimeMillis() - startRecordTime)
                    / actionListener.getRecordDuration();
            canvas.drawArc(arcRect, -90, progress, false, progressPaint);
            // arcPath.arcTo(arcRect,-90,progress);
            // canvas.drawPath(arcPath,progressPaint);
            if (progress >= 360) {
                post(recordEndRunnable);
            } else {
                invalidate();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isRecording || isShrink) {
                    return false;
                }
                // 处理动画
                isDown = true;
                isRecordStart = false;
                progress = 0;
                calculateStep();

                postDelayed(recordStartRunnable, 400);
                break;
            // case MotionEvent.ACTION_MOVE:
            //
            // break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // 处理动画
                isDown = false;
                isShrink = true;
                progress = 0;
                calculateStep();
                removeCallbacks(scaleRunnable);
                post(scaleRunnable);

                break;
            default:
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void calculateStep() {
        if (isDown) {
            innerStep = (innerRadius - innerRadiusMin) / 7;
            outerStep = (outerRadiusMax - outerRadius) / 7;
        } else {
            innerStep = (innerRadiusMax - innerRadius) / 7;
            outerStep = (outerRadius - outerRadiusMin) / 7;
        }

        if (innerStep < 1) {
            innerStep = 1;
        }
        if (outerStep < 1) {
            outerStep = 1;
        }
    }

    public void setActionListener(OnViewActionListener actionListener) {
        this.actionListener = actionListener;
    }

    interface OnViewActionListener {

        boolean onStartRecord();

        void onTakePicture();

        void onStopRecord();

        long getRecordDuration();
    }
}
