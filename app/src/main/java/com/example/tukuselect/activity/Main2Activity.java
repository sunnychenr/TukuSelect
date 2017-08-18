package com.example.tukuselect.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.tukuselect.R;
import com.example.tukuselect.utils.ConstantUtil;
import com.example.tukuselect.view.EditPicView;

/**
 * Created by ChenR on 2017/7/14.
 */

public class Main2Activity extends Activity implements View.OnClickListener {
    private static final String TAG = "chenr";

    private EditPicView epv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        epv = (EditPicView) findViewById(R.id.epv);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        epv.setImageUri(uri);

        findViewById(R.id.tv_ok).setOnClickListener(this);
        findViewById(R.id.tv_no).setOnClickListener(this);
    }

//    public void prefClickCanCel (View v) {
//        finish();
//    }
//
//    public void prefClickSure (View v) {
//        Bitmap photo = epv.getCustomPhoto();
//        if (photo != null) {
//            Intent intent = new Intent();
//            intent.putExtra(ConstantUtil.CROP_BITMAP, photo);
//            setResult(ConstantUtil.RESULT_CODE_CROP, intent);
//        }
//
//        finish();
//    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick");
        if (v.getId() == R.id.tv_ok) {
            Log.d(TAG, "onClick: ok");
            Bitmap photo = epv.getCustomPhoto();
            if (photo != null) {
                Intent intent = new Intent();
                intent.putExtra(ConstantUtil.CROP_BITMAP, photo);
                setResult(ConstantUtil.RESULT_CODE_CROP, intent);
            }
        }
    }
}
