package com.example.tukuselect.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.tukuselect.R;

public class EditPicView extends View {
    private static final String TAG = "chenr";

    private static final int MAX_ZOOM = 6;

    private final int ACTION_MODE_DRAG = 0;
    private final int ACTION_MODE_ZOOM = 1;

    private Context mContext;

    private Paint mPaint_01;
    private Paint mPaint_02;
    private int mCropRadius = 240;
    private float density;
    private int width;
    private int height;
    private int left;
    private int top;
    private int maxDragDistX;
    private int maxDragDistY;
    private float mStartPointerDis;
    private float mScale;

    private int mBitmapActionMode = ACTION_MODE_DRAG;

    private PointF mDownPointF = new PointF();
    private PointF mCirclePointF = new PointF();

    private Bitmap mResourcesBitmap;
    private Bitmap mDisplayBitmap;
    private Bitmap mMaskLayerBitmap;

    private GestureDetector mGestureDetector;

    private GestureDetector.SimpleOnGestureListener mSimpleOnGesttureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(TAG, "onSingleTapUp: action --> " + e.getActionMasked());
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // 单击确认;
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // 双击;
            return true;
        }

    };

    public EditPicView(Context context) {
        this(context, null);
    }

    public EditPicView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditPicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EditPicView);
        int resourceId = typedArray.getResourceId(R.styleable.EditPicView_picSrc, R.drawable.pic_1);
        mResourcesBitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        typedArray.recycle();

        mScale = getBitmapScale(mResourcesBitmap);
        mDisplayBitmap = decodeBitmap(mResourcesBitmap, mScale);

        setClickable(true);

//        mGestureDetector = new GestureDetector(context, mOnGestureListener);
        mGestureDetector = new GestureDetector(context, mSimpleOnGesttureListener);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        density = metrics.density;

        mPaint_01 = new Paint();
        mPaint_02 = new Paint();

        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                width = getWidth();
                height = getHeight();
                createMaskLayer();
                invalidate();
                if (Build.VERSION.SDK_INT >= 16) {
                    EditPicView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    private float getBitmapScale(Bitmap bitmap) {
        int resWidth = bitmap.getWidth();
        int resHeight = bitmap.getHeight();

        float targetSize = mCropRadius * 2.0f;

        float zoom = 0;
        if (resHeight > resWidth)
            zoom = resWidth / targetSize;
        else
            zoom = resHeight / targetSize;
        return zoom;
    }

    private Bitmap decodeBitmap(Bitmap bitmap, float scale) {
        if (bitmap == null) return null;
        int resWidth = bitmap.getWidth();
        int resHeight = bitmap.getHeight();

//        int outWidth = (int) (resWidth / zoom);
//        int outHeight = (int) (resHeight / zoom);

        Matrix matrix = new Matrix();
        matrix.setScale(1 / scale, 1 / scale, 0.5f, 0.5f);

//        return ThumbnailUtils.extractThumbnail(bitmap, outWidth, outHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, resWidth, resHeight, matrix, false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getWidth();
        height = getHeight();

        createMaskLayer();
    }

    private void createMaskLayer() {
        if (width > 0 && height > 0) {
            mMaskLayerBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);

            Canvas canvas = new Canvas(mMaskLayerBitmap);
            canvas.drawColor(0x50000000);

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.TRANSPARENT);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));

            int cx = width / 2;
            int cy = height / 2;
            canvas.drawCircle(cx, cy, mCropRadius, paint);

            mCirclePointF.set(cx-mCropRadius, cy-mCropRadius);

            if (mDisplayBitmap.getHeight() == mCropRadius) {
                left = (width - mDisplayBitmap.getWidth()) / 2;
                top = (height - mCropRadius*2) / 2;
                maxDragDistY = 0;
                maxDragDistX = mDisplayBitmap.getWidth() - mCropRadius*2;
            } else {
                left = (width - mCropRadius*2) / 2;
                top = (height - mDisplayBitmap.getHeight()) / 2;
                maxDragDistX = 0;
                maxDragDistY = mDisplayBitmap.getHeight() - mCropRadius*2;
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mMaskLayerBitmap != null) {
            mPaint_01.setAntiAlias(true);
            canvas.drawBitmap(mDisplayBitmap, left, top, mPaint_01);
            canvas.save();

            mPaint_02.setAntiAlias(true);
            canvas.drawBitmap(mMaskLayerBitmap, 0 , 0, mPaint_02);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mBitmapActionMode = ACTION_MODE_DRAG;
                mDownPointF.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_MOVE:
                if (mBitmapActionMode == ACTION_MODE_DRAG) {
                    startDrag(event);
                } else if (mBitmapActionMode == ACTION_MODE_ZOOM) {
                    startZoom(event);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mBitmapActionMode = ACTION_MODE_ZOOM;
                mStartPointerDis = distance(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    private void startZoom(MotionEvent event) {
        if (event.getPointerCount() < 2) return;
        float moveDist = distance(event);

        if (moveDist > 10.0f) {
            float scale = moveDist / mStartPointerDis;
            mStartPointerDis = moveDist;
            PointF center = getCenterBitmap(event);
        }

    }

    private PointF getCenterBitmap(MotionEvent event) {
        float cx = (event.getX(0) + event.getX(1)) / 2.0f;
        float cy = (event.getY(0) + event.getY(1)) / 2.0f;

        if (cx < top) {
            cx = top;
        } else if (cx > top + mDisplayBitmap.getHeight()) {
            cx = top + mDisplayBitmap.getHeight();
        }

        return new PointF(cx, cy);
    }

    private float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void startDrag(MotionEvent event) {
        float moveX = event.getX();
        float moveY = event.getY();

        float distX = moveX - mDownPointF.x;
        float distY = moveY - mDownPointF.y;

        float movePointX = left + distX;
        float movePointY = top + distY;

        if (movePointX < mCirclePointF.x - maxDragDistX) {
            left = Math.round(mCirclePointF.x - maxDragDistX);
        } else if (movePointX > mCirclePointF.x) {
            left = Math.round(mCirclePointF.x);
        } else {
            left = Math.round(movePointX);
        }

        if (movePointY < mCirclePointF.y - maxDragDistY) {
            top = Math.round(mCirclePointF.y - maxDragDistY);
        } else if (movePointY > mCirclePointF.y) {
            top = Math.round(mCirclePointF.y);
        } else {
            top = Math.round(movePointY);
        }

        mDownPointF.set(moveX, moveY);
        invalidate();
    }


}
