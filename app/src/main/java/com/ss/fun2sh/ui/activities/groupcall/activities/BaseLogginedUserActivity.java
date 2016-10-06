package com.ss.fun2sh.ui.activities.groupcall.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.base.BaseLoggableActivity;


/**
 * QuickBlox team
 */
abstract public class BaseLogginedUserActivity extends BaseLoggableActivity {

    private static final String APP_VERSION = "App version";
    private ActionBar mActionBar;
    private Chronometer timerABWithTimer;
    private boolean isStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.initActionBar();
    }

    public void initGroupActionBar(String title) {
        setActionBarTitle(title);
    }

    public void initGroupActionBar() {
        super.setActionBarUpButtonEnabled(true);
        setActionBarTitle("Group Call");

    }

    public String getAppVersion() {
        PackageInfo pinfo = null;
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(BaseLogginedUserActivity.class.getSimpleName(), "Retriving versionName failed." + e.getLocalizedMessage());
            return null;
        }

    }

    public void initActionBarWithTimer(String title) {
        mActionBar = getSupportActionBar();
        super.setActionBarUpButtonEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.group_actionbar_with_timer, null);

        timerABWithTimer = (Chronometer) mCustomView.findViewById(R.id.timerABWithTimer);

        TextView userNameAB = (TextView) mCustomView.findViewById(R.id.userNameABWithTimer);
        userNameAB.setText(title);

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
    }

    public void startTimer() {
        if (!isStarted) {
            timerABWithTimer.setBase(SystemClock.elapsedRealtime());
            timerABWithTimer.start();
            isStarted = true;
        }
    }

    public void stopTimer() {
        if (timerABWithTimer != null) {
            timerABWithTimer.stop();
            isStarted = false;
        }
    }
}




