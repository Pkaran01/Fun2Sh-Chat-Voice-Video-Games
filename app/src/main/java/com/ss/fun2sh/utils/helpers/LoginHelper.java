package com.ss.fun2sh.utils.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoadDialogsCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoginChatCompositeCommand;
import com.quickblox.q_municate_core.qb.commands.rest.QBLoginCompositeCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.users.model.QBUser;
import com.ss.fun2sh.AppController;
import com.ss.fun2sh.utils.listeners.ExistingQbSessionListener;
import com.ss.fun2sh.utils.listeners.GlobalLoginListener;

import java.util.concurrent.TimeUnit;

public class LoginHelper {

    private static String TAG = LoginHelper.class.getSimpleName();

    private Context context;
    private SharedHelper appSharedHelper;
    private CommandBroadcastReceiver commandBroadcastReceiver;
    private LoginBroadcastReceiver loginBroadcastReceiver;
    private GlobalLoginListener globalLoginListener;
    private ExistingQbSessionListener existingQbSessionListener;

    private String userEmail;
    private String userPassword;

    public LoginHelper(Context context) {
        this.context = context;
        appSharedHelper = AppController.getInstance().getAppSharedHelper();

        userEmail = appSharedHelper.getUserLogin();
        userPassword = appSharedHelper.getUserPassword();
    }

    public LoginHelper(Context context, ExistingQbSessionListener existingQbSessionListener) {
        this(context);
        this.existingQbSessionListener = existingQbSessionListener;
    }

    public static boolean isCorrectOldAppSession() {
        AppSession.load();
        return AppSession.getSession().getUser() != null && AppSession.getSession().getUser().getId() != 0;
    }

    public void checkStartExistSession() {
        if (needToClearAllData()) {
            existingQbSessionListener.onStartSessionFail();
            return;
        }
        if (appSharedHelper.isSavedRememberMe()) {
            startExistSession();
        } else {
            existingQbSessionListener.onStartSessionFail();
        }
    }

    public void startExistSession() {
        boolean isEmailEntered = !TextUtils.isEmpty(userEmail);
        boolean isPasswordEntered = !TextUtils.isEmpty(userPassword);
        if (isEmailEntered && isPasswordEntered) {
            runExistSession();
        } else {
            existingQbSessionListener.onStartSessionFail();
        }
    }

    public LoginType getCurrentLoginType() {
        return AppSession.getSession().getLoginType();
    }

    public void runExistSession() {
        //check is token valid for about 1 minute
        if (AppSession.isSessionExistOrNotExpired(TimeUnit.MINUTES.toMillis(
                ConstsCore.TOKEN_VALID_TIME_IN_MINUTES))) {
            existingQbSessionListener.onStartSessionSuccess();
        } else {
            login();
        }
    }

    public void login() {
        if (LoginType.EMAIL.equals(getCurrentLoginType())) {
            loginQB();
        }
    }

    public void loginQB() {
        appSharedHelper.saveUsersImportInitialized(true);
        QBUser qbUser = new QBUser(userEmail, userPassword, null);
        AppSession.getSession().closeAndClear();
        QBLoginCompositeCommand.start(context, qbUser);
    }

    public void loginChat() {
        QBLoginChatCompositeCommand.start(context);
    }

    private void loadDialogs() {
        QBLoadDialogsCommand.start(context);
    }

    private boolean needToClearAllData() {
        if (DataManager.getInstance().getUserDataManager().getAll().isEmpty()) {
            AppController.getInstance().getAppSharedHelper().clearAll();
            AppSession.getSession().closeAndClear();
            return true;
        } else {
            return false;
        }
    }


    public void makeGeneralLogin(GlobalLoginListener globalLoginListener) {
        this.globalLoginListener = globalLoginListener;
        commandBroadcastReceiver = new CommandBroadcastReceiver();
        registerCommandBroadcastReceiver();
        login();
    }

    private void registerLoginBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(QBServiceConsts.LOGIN_SUCCESS_ACTION);
        intentFilter.addAction(QBServiceConsts.LOGIN_FAIL_ACTION);


        LocalBroadcastManager.getInstance(context).registerReceiver(loginBroadcastReceiver, intentFilter);
    }

    public void makeCallLogin(GlobalLoginListener globalLoginListener) {
        this.globalLoginListener = globalLoginListener;
        loginBroadcastReceiver = new LoginBroadcastReceiver();
        registerLoginBroadcastReceiver();
        login();
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(commandBroadcastReceiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(loginBroadcastReceiver);
    }

    private void registerCommandBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(QBServiceConsts.LOGIN_SUCCESS_ACTION);
        intentFilter.addAction(QBServiceConsts.LOGIN_FAIL_ACTION);

        intentFilter.addAction(QBServiceConsts.SOCIAL_LOGIN_SUCCESS_ACTION);
        intentFilter.addAction(QBServiceConsts.SOCIAL_LOGIN_FAIL_ACTION);

        intentFilter.addAction(QBServiceConsts.LOGIN_CHAT_COMPOSITE_SUCCESS_ACTION);
        intentFilter.addAction(QBServiceConsts.LOGIN_CHAT_COMPOSITE_FAIL_ACTION);

        intentFilter.addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION);
        intentFilter.addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_FAIL_ACTION);

        LocalBroadcastManager.getInstance(context).registerReceiver(commandBroadcastReceiver, intentFilter);
    }

    private class CommandBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent.getAction().equals(QBServiceConsts.LOGIN_SUCCESS_ACTION)
                    || intent.getAction().equals(QBServiceConsts.SOCIAL_LOGIN_SUCCESS_ACTION)) {
                QBUser qbUser = (QBUser) intent.getExtras().getSerializable(QBServiceConsts.EXTRA_USER);
                AppSession.getSession().updateUser(qbUser);
                loginChat();
            } else if (intent.getAction().equals(QBServiceConsts.LOGIN_CHAT_COMPOSITE_SUCCESS_ACTION)) {
                loadDialogs();
            } else if (intent.getAction().equals(QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION)) {
                unregisterBroadcastReceiver();
                if (globalLoginListener != null) {
                    globalLoginListener.onCompleteQbChatLogin();
                }
            } else if (intent.getAction().equals(QBServiceConsts.LOGIN_FAIL_ACTION)
                    || intent.getAction().equals(QBServiceConsts.LOGIN_CHAT_COMPOSITE_FAIL_ACTION)
                    || intent.getAction().equals(QBServiceConsts.LOAD_CHATS_DIALOGS_FAIL_ACTION)
                    || intent.getAction().equals(QBServiceConsts.SOCIAL_LOGIN_FAIL_ACTION)) {
                unregisterBroadcastReceiver();
                if (globalLoginListener != null) {
                    globalLoginListener.onCompleteWithError("Login was finished with error!");
                }
            }
        }
    }

    private class LoginBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent.getAction().equals(QBServiceConsts.LOGIN_SUCCESS_ACTION)) {
                QBUser qbUser = (QBUser) intent.getExtras().getSerializable(QBServiceConsts.EXTRA_USER);
                AppSession.getSession().updateUser(qbUser);
                loginChat();
                if (globalLoginListener != null) {
                    globalLoginListener.onCompleteQbChatLogin();
                }
                unregisterBroadcastReceiver();
            } else if (intent.getAction().equals(QBServiceConsts.LOGIN_FAIL_ACTION)) {
                unregisterBroadcastReceiver();
                if (globalLoginListener != null) {
                    globalLoginListener.onCompleteWithError("Login was finished with error!");
                }
            }
        }
    }
}