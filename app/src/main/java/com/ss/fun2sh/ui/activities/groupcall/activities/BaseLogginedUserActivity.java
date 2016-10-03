package com.ss.fun2sh.ui.activities.groupcall.activities;

import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.users.model.QBUser;
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

    public void initGroupActionBar() {
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.group_actionbar_view, null);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);


        TextView numberOfListAB = (TextView) mCustomView.findViewById(R.id.numberOfListAB);
        QBUser loggedUser = AppSession.getSession().getUser();
        if (loggedUser != null) {
     /*       int number = DataHolder.getUserIndexByID(loggedUser.getId());
            numberOfListAB.setBackgroundResource(ListUsersActivity.resourceSelector(number));
            numberOfListAB.setText(String.valueOf(number+1));*/

            TextView loginAsAB = (TextView) mCustomView.findViewById(R.id.loginAsAB);
            loginAsAB.setText(R.string.logged_in_as);
            //
            TextView userNameAB = (TextView) mCustomView.findViewById(R.id.userNameAB);
            userNameAB.setText(String.valueOf(loggedUser.getLogin()));
        }

        numberOfListAB.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(BaseLogginedUserActivity.this);
                dialog.setTitle(APP_VERSION);
                String appVersion = getAppVersion();
                dialog.setMessage(appVersion);
                dialog.show();
                return true;
            }
        });


        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

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

    public void initActionBarWithTimer() {
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.group_actionbar_with_timer, null);

        timerABWithTimer = (Chronometer) mCustomView.findViewById(R.id.timerABWithTimer);

        TextView loginAsABWithTimer = (TextView) mCustomView.findViewById(R.id.loginAsABWithTimer);
        loginAsABWithTimer.setText(R.string.logged_in_as);

        TextView userNameAB = (TextView) mCustomView.findViewById(R.id.userNameABWithTimer);
        QBUser user = AppSession.getSession().getUser();
        if (user != null) {
            userNameAB.setText(user.getFullName());
        }

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




