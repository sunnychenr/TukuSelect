package com.example.tukuselect.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.tukuselect.R;
import com.example.tukuselect.utils.ConstantUtil;
import com.example.tukuselect.utils.ImageUtil;
import com.example.tukuselect.view.EditPicView;

import java.io.File;
import java.io.Serializable;

public class MainActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "chenr";
    private View ll_album;
    private View ll_crop;
    private EditPicView epv;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        epv = (EditPicView) findViewById(R.id.epv);
        iv = (ImageView) findViewById(R.id.iv);

        ll_album = findViewById(R.id.ll_album);
        ll_crop = findViewById(R.id.ll_crop);

        ll_album.setVisibility(View.VISIBLE);
        ll_crop.setVisibility(View.GONE);

        findViewById(R.id.tv_ok).setOnClickListener(this);
        findViewById(R.id.tv_no).setOnClickListener(this);
    }

    public void btnClick (View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, ConstantUtil.RESULT_CODE_ALBUM);

//        startActivity(new Intent(this, Main2Activity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ConstantUtil.RESULT_CODE_ALBUM && data != null) {
            Uri uri = data.getData();

            String path = ImageUtil.getPath(this, uri);
            ll_crop.setVisibility(View.VISIBLE);
            ll_album.setVisibility(View.GONE);
//            Intent intent = new Intent(this, Main2Activity.class);
//            intent.setData(uri);
            epv.setImageUri(uri);
        }
//        if (requestCode == ConstantUtil.RESULT_CODE_CROP && data != null) {
//            Bitmap bmp = data.getParcelableExtra(ConstantUtil.CROP_BITMAP);
//            ImageView iv = (ImageView) findViewById(R.id.iv);
//            iv.setImageBitmap(bmp);
//        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_ok) {
            Bitmap photo = epv.getCustomPhoto();
            iv.setImageBitmap(photo);
            ll_album.setVisibility(View.VISIBLE);
            ll_crop.setVisibility(View.GONE);
        }
    }
}
