package com.ss.fun2sh.CRUD;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.ss.fun2sh.R;


/**
 * Created by CRUD Technology on 10/26/2015.
 */
public class Splash {
    Context cx;

    public Splash(Context cx) {
        this.cx = cx;
    }

    //custom splesh screen
    public void isSplashScreen(final Class<?> startActivity) {
        final int TIME = 7 * 1000;// 5 seconds
        final Activity a = (Activity) cx;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(a, startActivity);
                a.startActivity(i);
                a.finish();
                a.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }, TIME);
    }
}

