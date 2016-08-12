package com.ss.fun2sh.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.HexagonImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.authorization.BaseAuthActivity;
import com.ss.fun2sh.utils.image.ImageLoaderUtils;

public class LogoutActivity extends BaseAuthActivity {


    private android.widget.TextView userName;

    public static void start(Context context, Intent intent) {
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_logout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.userName = (TextView) findViewById(R.id.userName);
        final HexagonImageView imageView=(HexagonImageView) findViewById(R.id.userImage);
        ImageLoader.getInstance().loadImage(PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.LAST_AVATAR_URL,""), ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedBitmap) {
                        imageView.setImageBitmap(loadedBitmap);
                    }
                });
        userName.setText(PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId) + " signing off");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 3000);
    }


}
