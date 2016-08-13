package com.ss.fun2sh.ui.activities.authorization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.R;
import com.ss.fun2sh.utils.helpers.LoginHelper;
import com.ss.fun2sh.utils.listeners.ExistingQbSessionListener;

public class SplashActivity extends BaseAuthActivity implements ExistingQbSessionListener {

    @Override
    protected int getContentResId() {
        return R.layout.activity_qb_splash;
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isNetworkAvailable()) {
            LoginHelper loginHelper = new LoginHelper(this, this);
            loginHelper.checkStartExistSession();
        } else if (LoginHelper.isCorrectOldAppSession()) {
            startMainActivity();
        } else {
            startLandingActivity();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLoggedInToServer()) {
            startMainActivity(true);
        }
    }

    @Override
    public void onStartSessionSuccess() {
        appSharedHelper.saveSavedRememberMe(true);
        startMainActivity(true);
        SplashActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onStartSessionFail() {
        startLandingActivity();
    }

    @Override
    public void checkShowingConnectionError() {
        // nothing. Toolbar is missing.
    }

    private void startLandingActivity() {
        if (checkNetworkAvailableWithError()) {
            login();
            //SplashActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void login() {
        loginType = LoginType.EMAIL;
        showProgress();
        String userName = (String) PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId);
        boolean ownerUser = DataManager.getInstance().getUserDataManager().isUserOwner(userName);
        if (!ownerUser) {
            DataManager.getInstance().clearAllTables();
        }
        login(userName, Const.App_Ver.QBPassword);
    }
}