package com.ss.fun2sh.CRUD;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.commands.chat.QBInitCallChatCommand;
import com.quickblox.q_municate_core.utils.ConnectivityUtils;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.ss.fun2sh.AppController;
import com.ss.fun2sh.ui.activities.call.CallActivity;
import com.ss.fun2sh.utils.SystemUtils;
import com.ss.fun2sh.utils.helpers.LoginHelper;
import com.ss.fun2sh.utils.helpers.SharedHelper;
import com.ss.fun2sh.utils.listeners.simple.SimpleGlobalLoginListener;

/**
 * Created by Karan on 7/16/2016.
 */

public class AlarmReceiverLifeLog extends BroadcastReceiver {

    private static final String TAG = "LL24";
    Context cx;
    private SharedHelper appSharedHelper;

    public AlarmReceiverLifeLog() {
        super();
        appSharedHelper = AppController.getInstance().getAppSharedHelper();

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e(TAG, "Alarm for LifeLog...");
        // do stuff
        cx = context;
        if (!SystemUtils.isAppRunningNow() && ConnectivityUtils.isNetworkAvailable(cx)) {
            if (!(appSharedHelper.getPref(CoreSharedHelper.isCallRunning, false))) {
                if (AppSession.getSession().getUser() != null) {
                    LoginHelper loginHelper = new LoginHelper(context);
                    loginHelper.makeGeneralLogin(new GlobalLoginListener());
                }
            }
        }
    }

    private class GlobalLoginListener extends SimpleGlobalLoginListener {

        @Override
        public void onCompleteQbChatLogin() {
            // on success
            M.E("onCompleteQbChatLogin ");
            QBInitCallChatCommand.start(cx, CallActivity.class);
        }

        @Override
        public void onCompleteWithError(String error) {
            M.E(error);
        }
    }
}
