package com.ss.fun2sh.utils.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_db.models.User;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.ui.activities.call.CallActivity;
import com.ss.fun2sh.utils.SystemUtils;
import com.ss.fun2sh.utils.helpers.notification.ChatNotificationHelper;

public class ChatMessageReceiver extends BroadcastReceiver {

    private static final String TAG = ChatMessageReceiver.class.getSimpleName();
    private static final String callActivityName = CallActivity.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        //M.E("--- onReceive()" + TAG);
        String activityOnTop = SystemUtils.getNameActivityOnTopStack();

        if (!SystemUtils.isAppRunningNow() && !callActivityName.equals(activityOnTop)) {
            ChatNotificationHelper chatNotificationHelper = new ChatNotificationHelper(context);
            try {
                String message = intent.getStringExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE);
                User user = (User) intent.getSerializableExtra(QBServiceConsts.EXTRA_USER);
                String dialogId = intent.getStringExtra(QBServiceConsts.EXTRA_DIALOG_ID);

                chatNotificationHelper.saveOpeningDialogData(user.getUserId(), dialogId);
                chatNotificationHelper.saveOpeningDialog(true);
                chatNotificationHelper.sendNotification(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}