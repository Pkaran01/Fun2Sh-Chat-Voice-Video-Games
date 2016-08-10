package com.ss.fun2sh.ui.activities.others;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.base.BaseLoggableActivity;
import com.ss.fun2sh.ui.views.TouchImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;

public class PreviewProfileImageActivity extends BaseLoggableActivity {

    public static final String EXTRA_IMAGE_URL = "image";

    private static final int IMAGE_MAX_ZOOM = 4;

    @Bind(R.id.image_touchimageview)
    TouchImageView imageTouchImageView;

    public static void start(Context context, String url) {
        Intent intent = new Intent(context, PreviewProfileImageActivity.class);
        intent.putExtra(EXTRA_IMAGE_URL, url);
        context.startActivity(intent);
    }


    @Override
    protected int getContentResId() {
        return R.layout.activity_preview_image;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        setUpActionBarWithUpButton();
        initTouchImageView();
        displayImage();
    }

    private void initFields() {
        title = getString(R.string.preview_image_title);
    }

    private void displayImage() {
        imageTouchImageView.setImageBitmap(Const.previewImage);
        /*String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        if (!TextUtils.isEmpty(imageUrl)) {
            ImageLoader.getInstance().displayImage(imageUrl, imageTouchImageView,
                    ImageLoaderUtils.UIL_DEFAULT_DISPLAY_OPTIONS);
        }*/
    }

    private void initTouchImageView() {
        imageTouchImageView.setMaxZoom(IMAGE_MAX_ZOOM);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_previewimage, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                try {
                    if (saveToInternalStorage(Const.previewImage)) {
                        M.T(PreviewProfileImageActivity.this, "Image save in gallery.");
                    } else {
                        M.T(PreviewProfileImageActivity.this, "Something went wrong.");
                    }
                } catch (IOException e) {
                    M.E(e.getMessage());
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private Boolean saveToInternalStorage(Bitmap bitmapImage) throws IOException {
        boolean success = false;
        // path to /data/data/yourapp/app_data/imageDir
        String path = Environment.getExternalStorageDirectory().toString();

        new File(path + "/Fun2Sh/FunChat Profile Photos").mkdirs();
        // Create imageDir
        File mypath = new File(path, "/Fun2Sh/FunChat Profile Photos/" + getIntent().getStringExtra(EXTRA_IMAGE_URL) + ".png");

        // Encode the file as a PNG image.
        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        /* 100 to keep full quality of the image */

            outStream.flush();
            outStream.close();
            success = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }
}