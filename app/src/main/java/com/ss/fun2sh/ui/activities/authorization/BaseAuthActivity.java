package com.ss.fun2sh.ui.activities.authorization;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;

import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.qb.commands.rest.QBLoginCompositeCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;
import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.PrefsHelper;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.base.BaseActivity;
import com.ss.fun2sh.ui.activities.main.MainActivity;
import com.ss.fun2sh.ui.activities.profile.FirstTimeUserProfileActivity;

import butterknife.Bind;

public abstract class BaseAuthActivity extends BaseActivity {

    protected static final String STARTED_LOGIN_TYPE = "started_login_type";
    private static String TAG = BaseAuthActivity.class.getSimpleName();
    @Nullable
    @Bind(R.id.email_edittext)
    protected EditText emailEditText;
    @Nullable
    @Bind(R.id.password_edittext)
    protected EditText passwordEditText;
    protected LoginType loginType = LoginType.EMAIL;
    protected Resources resources;
    protected LoginSuccessAction loginSuccessAction;
    protected FailAction failAction;

    public static void start(Context context) {
        Intent intent = new Intent(context, BaseAuthActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addActions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeActions();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STARTED_LOGIN_TYPE, loginType);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }


    private void initFields(Bundle savedInstanceState) {
        resources = getResources();
        if (savedInstanceState != null && savedInstanceState.containsKey(STARTED_LOGIN_TYPE)) {
            loginType = (LoginType) savedInstanceState.getSerializable(STARTED_LOGIN_TYPE);
        }
        loginSuccessAction = new LoginSuccessAction();
        failAction = new FailAction();
    }



    protected void startMainActivity(QBUser user) {
        AppSession.getSession().updateUser(user);
        if (!PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.firstTimeProfile, false)) {
            startMainActivity();
        } else {
            startMainActivitySecondTime();
        }

    }

    protected void startMainActivity(boolean importInitialized) {
        appSharedHelper.saveUsersImportInitialized(importInitialized);
        startMainActivitySecondTime();
    }

    protected void startMainActivity() {
        //MainActivity.start(BaseAuthActivity.this);
        FirstTimeUserProfileActivity.start(BaseAuthActivity.this);
        finish();
    }

    protected void startMainActivitySecondTime() {
        MainActivity.start(BaseAuthActivity.this);
        finish();
    }

    protected void parseExceptionMessage(Exception exception) {
        hideProgress();

        String errorMessage = exception.getMessage();

        if (errorMessage != null) {
            if (errorMessage.equals(getString(R.string.error_bad_timestamp))) {
                errorMessage = getString(R.string.error_bad_timestamp_from_app);
            } else if (errorMessage.equals(getString(R.string.error_login_or_email_required))) {
                errorMessage = getString(R.string.error_login_or_email_required_from_app);
            } else if (errorMessage.equals(getString(R.string.error_email_already_taken))
                    && loginType.equals(LoginType.FACEBOOK)) {
                errorMessage = getString(R.string.error_email_already_taken_from_app);
            } else if (errorMessage.equals(getString(R.string.error_unauthorized))) {
                errorMessage = getString(R.string.error_unauthorized_from_app);
            }

            ErrorUtils.showError(this, errorMessage);
        }
    }

    protected void parseFailException(Bundle bundle) {
        Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
        int errorCode = bundle.getInt(QBServiceConsts.EXTRA_ERROR_CODE);
        parseExceptionMessage(exception);
    }

    protected void login(String userEmail, String userPassword) {
        appSharedHelper.saveFirstAuth(true);
        appSharedHelper.saveSavedRememberMe(true);
        appSharedHelper.saveUsersImportInitialized(true);
        QBUser user = new QBUser(userEmail, userPassword, null);
        AppSession.getSession().closeAndClear();
        QBLoginCompositeCommand.start(this, user);
    }

    protected void performLoginSuccessAction(Bundle bundle) {
        QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
        startMainActivity(user);

        // send analytics data
        //  GoogleAnalyticsHelper.pushAnalyticsData(BaseAuthActivity.this, user, "User Sign In");
        //FlurryAnalyticsHelper.pushAnalyticsData(BaseAuthActivity.this);
    }

    protected boolean isLoggedInToServer() {
        return AppSession.getSession().isLoggedIn();
    }

    private void addActions() {
        addAction(QBServiceConsts.LOGIN_SUCCESS_ACTION, loginSuccessAction);
        addAction(QBServiceConsts.LOGIN_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.SIGNUP_FAIL_ACTION, failAction);

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.LOGIN_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOGIN_FAIL_ACTION);

        removeAction(QBServiceConsts.SOCIAL_LOGIN_SUCCESS_ACTION);
        removeAction(QBServiceConsts.SOCIAL_LOGIN_FAIL_ACTION);

        removeAction(QBServiceConsts.SIGNUP_FAIL_ACTION);

        updateBroadcastActionList();
    }

    protected void startLandingScreen() {
        LandingActivity.start(this);
        finish();
    }


    private class LoginSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            performLoginSuccessAction(bundle);
        }
    }


    private class FailAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            parseFailException(bundle);
        }
    }


}