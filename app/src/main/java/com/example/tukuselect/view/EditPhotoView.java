package com.example.tukuselect.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by ChenR on 2017/8/17.
 */

public class EditPhotoView extends ImageView {

    private int mViewWidth;
    private int mViewHeight;

    public EditPhotoView(Context context) {
        this(context, null);
    }

    public EditPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
