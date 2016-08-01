package com.ss.fun2sh.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.ss.fun2sh.AppController;
import com.ss.fun2sh.CRUD.Const;


public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    private static String TAG = GcmBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean enablePush = AppController.getInstance().getAppSharedHelper().isEnablePushNotifications();
        Log.d(TAG, "--- PUSH. onReceive(), show notification = " + enablePush + " ---");
//        if (!enablePush) {
//            return;
//        }
        if (PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.isFirstTimeLogin, false)) {
            ComponentName comp = new ComponentName(context.getPackageName(), GCMIntentService.class.getName());
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);
        }
    }
}