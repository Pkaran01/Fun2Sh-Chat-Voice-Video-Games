package com.ss.fun2sh.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;

import com.ss.fun2sh.CRUD.Const;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.ss.fun2sh.CRUD.Splash;
import com.ss.fun2sh.R;

public class SplashActivity extends AppCompatActivity {

    private android.widget.VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        this.videoView = (VideoView) findViewById(R.id.videoView);
        new Splash(SplashActivity.this).isSplashScreen(PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.isFirstTimeLogin, false) ? DashBoardActivity.class : (PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.secondTimeLogin, false)) ? SecondTimeLoginActivity.class : LoginActivity.class);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.funtwoshlogo;
        videoView.setVideoURI(Uri.parse(path));
        videoView.start();
    }
}
