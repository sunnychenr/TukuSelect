package com.example.tukuselect.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.tukuselect.utils.ImageUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EditPicView extends View {
    private static final String TAG = "chenr";

    private static final int MAX_SCALE = 2;

    private final int ACTION_MODE_DRAG = 0;
    private final int ACTION_MODE_ZOOM = 1;

    private final int TARGET_IMAGE_SIZE = 640;

    private Context mContext;

    private Paint mPaint_01;
    private Paint mPaint_02;
    private int mCropRadius = 240;
    private int mCropDiameter = mCropRadius*2;
    private float density;
    private int width;
    private int height;
    private int left;
    private int top;
    private int maxDragDistX;
    private int maxDragDistY;
    private float mStartPointerDis;
    private float mBaseScale;

    private int mBitmapActionMode = ACTION_MODE_DRAG;

    private PointF mDownPointF = new PointF();
    private PointF mCirclePointF = new PointF();
    private Matrix mMtrix = new Matrix();
    private Bitmap mResourcesBitmap;
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
            //checkDoubleTap(e);
            return true;
        }

        private void checkDoubleTap(MotionEvent e) {
            float [] value = new float[9];
            mMtrix.getValues(value);
            float sx = value[Matrix.MSCALE_X];

            if (sx > mBaseScale) {
                mMtrix.postScale(mBaseScale, mBaseScale);
            } else {
                float scale = (mBaseScale + MAX_SCALE) / 2.0f;
                mMtrix.postScale(scale, scale, width/2, height/2);
            }

            invalidate();
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
//        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EditPicView);
//        int resourceId = typedArray.getResourceId(R.styleable.EditPicView_picSrc, R.drawable.pic_1);
//        resolveBitmap(resourceId);
//        typedArray.recycle();

        //mBaseScale = getBitmapScale(mResourcesBitmap);
        //setMatrixScle();

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

    private void resolveBitmap(int resId) {
//        mResourcesBitmap = BitmapFactory.decodeResource(getResources(), resId);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resId, options);

        int inSampleSize = ImageUtil.calculateInSampleSize(options, TARGET_IMAGE_SIZE, TARGET_IMAGE_SIZE);

        Log.d(TAG, "inSampleSize: " + inSampleSize);

        int w = options.outWidth;
        int h = options.outHeight;

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        InputStream is = null;
        try {
            is = getContext().getAssets().open("img_4.jpg");
            BitmapFactory.decodeStream(is, null, o);
            int inSampleSize1 = ImageUtil.calculateInSampleSize(options, TARGET_IMAGE_SIZE, TARGET_IMAGE_SIZE);

            Log.d(TAG, "inSampleSize1: " + inSampleSize1);
            o.inJustDecodeBounds = false;
            o.inSampleSize = inSampleSize1;
            Bitmap bmp = BitmapFactory.decodeStream(is, null, o);
            if (bmp != null) {
                Log.d(TAG, "bmp ===> w: " + bmp.getWidth() + ",  h: " + bmp.getHeight() + ", size: " + (bmp.getByteCount()/1024.0/1024.0) + "mb");
                mResourcesBitmap = bmp;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
    }

    private void setMatrixScle() {
        mMtrix.reset();
        mMtrix.postScale(mBaseScale, mBaseScale, 0.5f, 0.5f);
    }

    private float getBitmapScale(Bitmap bitmap) {
        if (bitmap != null) {
            int resWidth = bitmap.getWidth();
            int resHeight = bitmap.getHeight();

            float targetSize = mCropRadius * 2.0f;

            float zoom = 0;
            if (resHeight > resWidth)
                zoom = resWidth / targetSize;
            else
                zoom = resHeight / targetSize;
            return 1 / zoom;
        }
        else
            return 1;
    }

//    private Bitmap decodeBitmap(Bitmap bitmap, float scale) {
//        if (bitmap == null) return null;
//        int resWidth = bitmap.getWidth();
//        int resHeight = bitmap.getHeight();
//
//        int outWidth = (int) (resWidth / zoom);
//        int outHeight = (int) (resHeight / zoom);
//
//        Matrix matrix = new Matrix();
//        matrix.setScale(1 / scale, 1 / scale, 0.5f, 0.5f);
//
//        return ThumbnailUtils.extractThumbnail(bitmap, outWidth, outHeight);
//        return Bitmap.createBitmap(bitmap, 0, 0, resWidth, resHeight, matrix, false);
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//        width = getWidth();
//        height = getHeight();

        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
//        Log.d(TAG, "onMeasure, size1: "+MeasureSpec.getSize(widthMeasureSpec)+",  size2: "+MeasureSpec.getSize(heightMeasureSpec));

        createMaskLayer();
    }

    private void createMaskLayer() {
        if (width > 0 && height > 0) {
            mMaskLayerBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

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

            initBitmapParams();
        }
    }

    private void initBitmapParams() {
        if (mResourcesBitmap != null) {
            float [] value = new float[9];
            mMtrix.getValues(value);
            float sx = value[Matrix.MSCALE_X];
            float sy = value[Matrix.MSCALE_Y];

            float sh = mResourcesBitmap.getHeight() * sx;
            float sw = mResourcesBitmap.getWidth() * sy;

            if ((int) sh == mCropDiameter) {
                left = (width - Math.round(sw)) / 2;
                top = (height - mCropDiameter) / 2;
                maxDragDistX = Math.round(sw) - mCropDiameter;
                maxDragDistY = 0;
            } else {
                left = (width - mCropDiameter) / 2;
                top = (height - Math.round(sh)) / 2;
                maxDragDistX = 0;
                maxDragDistY = Math.round(sh) - mCropDiameter;
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mResourcesBitmap != null) {
            mPaint_01.setAntiAlias(true);
            canvas.translate(left, top);
            canvas.drawBitmap(mResourcesBitmap, mMtrix, mPaint_01);
            canvas.save();
        }
        if (mMaskLayerBitmap != null) {
            mPaint_02.setAntiAlias(true);
            canvas.drawBitmap(mMaskLayerBitmap, -left , -top, mPaint_02);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mResourcesBitmap == null) {
            return super.onTouchEvent(event);
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mBitmapActionMode = ACTION_MODE_DRAG;
                mDownPointF.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                float [] value = new float[9];
                mMtrix.getValues(value);
                float sx = value[Matrix.MSCALE_X];
                float sy = value[Matrix.MSCALE_Y];

                float sh = mResourcesBitmap.getHeight() * sx;
                float sw = mResourcesBitmap.getWidth() * sy;
                maxDragDistX = Math.round(sw) - mCropDiameter;
                maxDragDistY = Math.round(sh) - mCropDiameter;
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
            float scale = (moveDist / mStartPointerDis);

            float [] value = new float[9];
            mMtrix.getValues(value);
            scale *= value[Matrix.MSCALE_X];

            if (scale <= getBitmapScale(mResourcesBitmap)) {
                scale = getBitmapScale(mResourcesBitmap);
            } else if (scale >= MAX_SCALE) {
                scale = MAX_SCALE;
            }

            int targetWidth = Math.round(mResourcesBitmap.getWidth() * scale);
            int targetHeight = Math.round(mResourcesBitmap.getHeight() * scale);

            int maxScaleX = Math.round(mCirclePointF.x) - targetWidth + mCropDiameter;
            if (left <= maxScaleX) {
                left = maxScaleX;
            }
            int maxScaleY = Math.round(mCirclePointF.y) - targetHeight + mCropDiameter;
            if (maxScaleY >= top) {
                top = maxScaleY;
            }

            mMtrix.reset();
            mMtrix.setScale(scale, scale);
            mStartPointerDis = moveDist;
            invalidate();
        }
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

    public void setImageUri (Uri uri) {
        if (uri != null) {
            String path = ImageUtil.getPath(getContext(), uri);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            options.inSampleSize = ImageUtil.calculateInSampleSize(options, TARGET_IMAGE_SIZE, TARGET_IMAGE_SIZE);;
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            if (bitmap != null) {
                mResourcesBitmap = bitmap;
                mBaseScale = getBitmapScale(bitmap);
                setMatrixScle();
                initBitmapParams();
            }


//            InputStream is = null;
//            try {
//                Log.d(TAG, "setImageUri: start set image");
//                is = getContext().getContentResolver().openInputStream(uri);
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//                BitmapFactory.decodeStream(is, null, options);
//                options.inSampleSize = ImageUtil.calculateInSampleSize(options, TARGET_IMAGE_SIZE, TARGET_IMAGE_SIZE);;
//                options.inJustDecodeBounds = false;
//
//                Bitmap bitmap = BitmapFactory.decodeStream(is, new Rect(), options);
//                Log.d(TAG, "bitmap: " + bitmap);
//                if (bitmap != null) {
//                    mResourcesBitmap = bitmap;
//                    mBaseScale = getBitmapScale(bitmap);
//                    setMatrixScle();
//                    initBitmapParams();
//                    Log.d(TAG, "setImageUri completed set image");
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } finally {
//                if (is != null) {
//                    try {
//                        is.close();
//                    } catch (IOException e) {
//
//                    }
//                }
//            }
        }
        invalidate();
    }

    public Bitmap resolveBitmap() {
        Canvas canvas = new Canvas(mResourcesBitmap);

        float [] value = new float[9];
        mMtrix.getValues(value);
        float scale = 1 /  value[Matrix.MSCALE_X];

        int sR = Math.round(mCropRadius * scale);
        int tempS1 = Math.abs(top);
        int tempS2 = Math.abs(left);
        int cropWidth = Math.round(tempS1 * scale);
        int cropHeight = Math.round(tempS2 * scale);
        int i = 123456789;

        return null;
    }

    public Bitmap getCustomPhoto () {
        if (mResourcesBitmap == null) {
            return null;
        }
        float [] value = new float[9];
        mMtrix.getValues(value);
        int sw = Math.round(mResourcesBitmap.getWidth() * value[Matrix.MSCALE_X]);
        int sh = Math.round(mResourcesBitmap.getHeight() * value[Matrix.MSCALE_Y]);

        Log.d(TAG, "scale bitmap width: " + sw + ", height: " + sh);

        Bitmap out = Bitmap.createBitmap(sw, sh, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);

        int srcLeft = Math.abs(left);
        int srcTop = Math.abs(top);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        canvas.drawARGB(0x00, 0x00, 0x00, 0x00);

        float cx = srcLeft + mCropRadius;
        float cy = srcTop + mCropRadius;
        canvas.drawCircle(cx, cy, mCropRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//        canvas.drawBitmap(mResourcesBitmap, mMtrix, paint);
        canvas.drawBitmap(Bitmap.createBitmap(mResourcesBitmap, 0, 0, sw, sh, mMtrix, false), 0, 0, paint);

        return out;
    }
}
