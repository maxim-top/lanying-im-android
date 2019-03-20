
package top.maxim.im.common.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import top.maxim.im.R;

/**
 * Description : 头像imageView Created by Mango on 2018/11/06.
 */
public class ShapeImageView extends AppCompatImageView {

    public interface Shape {
        int CIRCLE = 1; // 圆形

        int RECTANGLE = 2; // 矩形

        int ROUNDRECTANGLE = 3;// 圆角矩形

        int SQUARE = 4; // 正方形

        int POLYGON = 5; // 多边形，边数为3=三角形,边数为4=棱形
    }

    private static final int DEFAULT_BORDER_WIDTH = 4;

    private boolean mInvalidated = true;

    private boolean mReBuildShader = true;

    private int mBorderColor = Color.BLUE;

    private int mBorderWidth;

    private int mShapeType = Shape.CIRCLE;// defaut

    private int mPolygonSides = 6;// defaut hexagon if mShapeType = Shape.POLYGON;

    private float mRx = 24f;

    private float mRy = 24f;

    private int mResource;

    private Paint mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mShaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Path mMaskPath = new Path();

    private RectF mRectF;

    private Matrix mMatrix;

    public ShapeImageView(Context context) {
        this(context, null);
    }

    public ShapeImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShapeImageView);
            mShapeType = a.getInt(R.styleable.ShapeImageView_shape, mShapeType);
            mRx = a.getFloat(R.styleable.ShapeImageView_radius_x, mRx);
            mRy = a.getFloat(R.styleable.ShapeImageView_radius_y, mRy);
            mPolygonSides = a.getInt(R.styleable.ShapeImageView_sides, mPolygonSides);
            a.recycle();
        }

        mShaderPaint.setFilterBitmap(false);
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (ScaleType.FIT_XY == scaleType) {
            scaleType = ScaleType.CENTER_CROP;
        }
        super.setScaleType(scaleType);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isInEditMode()) {
            if (mInvalidated) {
                mInvalidated = false;
                createMask(getMeasuredWidth(), getMeasuredHeight());
            }

            if (mReBuildShader) {
                mReBuildShader = false;
                createShader();
            }

            // avoid black backgroud on first call onDraw without Drawable
            if (null != mShaderPaint.getShader()) {
                canvas.drawPath(mMaskPath, mShaderPaint);
                if (mBorderWidth > 0) {
                    mMaskPaint.setStyle(Paint.Style.STROKE);
                    mMaskPaint.setColor(mBorderColor);
                    mMaskPaint.setStrokeWidth(mBorderWidth);
                    canvas.drawPath(mMaskPath, mMaskPaint);
                }
            }
        } else {
            super.onDraw(canvas);
        }
    }

    private void createShader() {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            Bitmap bitmap = drawableToBitmap(drawable);
            if (null != bitmap) {
                BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP,
                        Shader.TileMode.CLAMP);
                shader.setLocalMatrix(getImageMatrix());
                mShaderPaint.setShader(shader);
            }
        }
    }

    private void createMask(int width, int height) {
        mMaskPath.reset();
        mMaskPaint.setStyle(Paint.Style.FILL);
        int offset = mBorderWidth / 2;
        switch (mShapeType) {
            case Shape.CIRCLE:
                float center = Math.min(width, height) / 2f;
                float radius = center;
                radius -= offset;// avoid stroke out of bounds
                mMaskPath.addCircle(center, center, radius, Path.Direction.CW);
                break;
            case Shape.RECTANGLE:
                mMaskPath.addRect(offset, offset, width - offset, height - offset,
                        Path.Direction.CW);
                break;
            case Shape.ROUNDRECTANGLE:
                if (mRectF == null) {
                    mRectF = new RectF();
                }
                mRectF.set(offset, offset, width - offset, height - offset);
                mMaskPath.addRoundRect(mRectF, mRx, mRy, Path.Direction.CW);
                break;
            case Shape.SQUARE:
                int length = Math.min(width, height) - offset;
                mMaskPath.addRect(offset, offset, length, length, Path.Direction.CW);
                break;
            case Shape.POLYGON:
                createPolygonPath(width, height, mPolygonSides);
                break;
        }
    }

    private void createPolygonPath(int width, int height, int sides) {
        sides = Math.abs(sides);
        float radius, centerX, centerY;
        radius = centerX = centerY = Math.min(width, height) / 2;
        radius -= mBorderWidth / 2;// avoid stroke out of bounds
        float offsetAngle = 0;
        offsetAngle = (float)(Math.PI * offsetAngle / 180);
        for (int i = 0; i < sides; i++) {
            float x = (float)(centerX + radius * Math.cos(offsetAngle));
            float y = (float)(centerY + radius * Math.sin(offsetAngle));
            offsetAngle += 2 * Math.PI / sides;
            if (i == 0) {
                mMaskPath.moveTo(x, y);
            } else {
                mMaskPath.lineTo(x, y);
            }
        }
        mMaskPath.close();

        // Reset Polygon direction
        if (sides % 2 != 0) {
            if (mMatrix == null) {
                mMatrix = new Matrix();
            } else {
                mMatrix.reset();
            }
            mMatrix.postRotate(-90, centerX, centerY);
            mMaskPath.transform(mMatrix);
        }

    }

    public void changeShapeType(int type) {
        changeShapeType(type, 0);
    }

    /**
     * @param sides used for polygon
     */
    public void changeShapeType(int type, int sides) {
        if (Shape.POLYGON == type && mPolygonSides != sides) {
            mInvalidated = true;
            mPolygonSides = sides < 3 ? 6 : sides;
        } else if (mShapeType != type) {
            mInvalidated = true;
        }
        mShapeType = type;
        invalidate();
    }

    /**
     * use{@link ShapeImageView#setBorderColor(int)}
     */
    @Deprecated
    public void setSelectedColorRes(int resId) {
        setBorderColor(getResources().getColor(resId));
    }

    /**
     * use{@link ShapeImageView#setBorderColor(int)}
     */
    @Deprecated
    public void setNomalColorRes(int colorRssId) {
        setBorderColor(getResources().getColor(colorRssId));
    }

    /**
     * use{@link ShapeImageView#setBorderColor(int)}
     */
    @Deprecated
    public void setFocusColor(int colorValue) {
        setBorderColor(colorValue);
    }

    /**
     * use{@link ShapeImageView#setFrameStrokeWidth(int)}
     */
    @Deprecated
    public void setFocused(boolean isFocus) {
        setFrameStrokeWidth(DEFAULT_BORDER_WIDTH);
    }

    public void setBorderColor(int color) {
        mBorderColor = color;
    }

    public void setFrameStrokeWidth(int width) {
        mBorderWidth = width;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        mResource = 0;
        super.setImageDrawable(fromDrawable(drawable));
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        if (mResource != resId) {
            mResource = resId;
            setImageDrawable(resolveResource());
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        setImageDrawable(getDrawable());
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        setImageDrawable(new BitmapDrawable(getResources(), bm));
    }

    private Drawable resolveResource() {
        Resources rsrc = getResources();
        if (rsrc == null) {
            return null;
        }

        Drawable d = null;
        if (mResource != 0) {
            try {
                d = rsrc.getDrawable(mResource);
            } catch (Exception e) {
                // Don't try again.
                mResource = 0;
            }
        }
        return fromDrawable(d);
    }

    private Drawable fromDrawable(Drawable drawable) {
        mReBuildShader = true;
        if (drawable != null) {
            if (drawable instanceof BitmapDrawable) {
                return drawable;
            } else if (drawable instanceof LayerDrawable) {
                LayerDrawable ld = (LayerDrawable)drawable;
                drawable = ld.getDrawable(0);
            } else if (drawable instanceof StateListDrawable) {
                StateListDrawable stateListDrawable = (StateListDrawable)drawable;
                drawable = stateListDrawable.getCurrent();
            }

            if (!(drawable instanceof BitmapDrawable)) {
                // try to get a bitmap from the drawable
                Bitmap bm = drawableToBitmap(drawable);
                if (bm != null) {
                    drawable = new BitmapDrawable(getResources(), bm);
                }
            }
        }
        return drawable;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap;
        try {
            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
            } else {
                int width = Math.max(drawable.getIntrinsicWidth(), 2);
                int height = Math.max(drawable.getIntrinsicHeight(), 2);
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            }
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
            bitmap = null;
        }
        return bitmap;
    }
}
