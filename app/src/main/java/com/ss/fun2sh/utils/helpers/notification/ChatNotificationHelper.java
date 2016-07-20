package com.ss.fun2sh.utils.helpers.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.NotificationEvent;
import com.quickblox.q_municate_core.qb.commands.chat.QBInitCallChatCommand;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.ss.fun2sh.AppController;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.call.CallActivity;
import com.ss.fun2sh.utils.SystemUtils;
import com.ss.fun2sh.utils.helpers.LoginHelper;
import com.ss.fun2sh.utils.helpers.SharedHelper;
import com.ss.fun2sh.utils.listeners.simple.SimpleGlobalLoginListener;

import java.util.Map;

public class ChatNotificationHelper {

    public static final String MESSAGE = "message";
    public static final String DIALOG_ID = "dialog_id";
    public static final String USER_ID = "user_id";

    private Context context;
    private SharedHelper appSharedHelper;
    private String dialogId;
    private int userId;

    private static String message;
    private static boolean isLoginNow;

    private QBRTCClient qbRtcClient;

    public ChatNotificationHelper(Context context) {
        this.context = context;
        appSharedHelper = AppController.getInstance().getAppSharedHelper();
        qbRtcClient = QBRTCClient.getInstance(context);
    }

    public void parseChatMessage(Bundle extras) {
        if (extras.getString(ChatNotificationHelper.MESSAGE) != null) {
            message = extras.getString(ChatNotificationHelper.MESSAGE);
        }

        if (extras.getString(ChatNotificationHelper.USER_ID) != null) {
            userId = Integer.parseInt(extras.getString(ChatNotificationHelper.USER_ID));
        }

        if (extras.getString(ChatNotificationHelper.DIALOG_ID) != null) {
            dialogId = extras.getString(ChatNotificationHelper.DIALOG_ID);
        }

        if (SystemUtils.isAppRunningNow()) {
            return;
        }

        boolean chatPush = userId != 0 && !TextUtils.isEmpty(dialogId);

        if (chatPush) {
            saveOpeningDialogData(userId, dialogId);
            if (AppSession.getSession().getUser() != null && !isLoginNow) {
                isLoginNow = true;
                LoginHelper loginHelper = new LoginHelper(context);
                loginHelper.makeGeneralLogin(new GlobalLoginListener());
                return;
            }
        } else {
            // push about call
            M.E("Call Noti" + message);
            if (AppSession.getSession().getUser() != null) {
                LoginHelper loginHelper = new LoginHelper(context);
                loginHelper.makeCallLogin(new CallLoginListener());
            }
        }

        saveOpeningDialog(false);
    }

    public void sendNotification(String message) {
        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setTitle(context.getString(R.string.app_name));
        notificationEvent.setSubject(message);
        notificationEvent.setBody(message);

        NotificationManagerHelper.sendNotificationEvent(context, notificationEvent);
    }

    public void saveOpeningDialogData(int userId, String dialogId) {
        appSharedHelper.savePushUserId(userId);
        appSharedHelper.savePushDialogId(dialogId);
    }

    public void saveOpeningDialog(boolean open) {
        appSharedHelper.saveNeedToOpenDialog(open);
    }

    private boolean isPushForPrivateChat() {
        Dialog dialog = DataManager.getInstance().getDialogDataManager().getByDialogId(dialogId);
        return dialog != null && dialog.getType().equals(Dialog.Type.PRIVATE);
    }

    private class GlobalLoginListener extends SimpleGlobalLoginListener {

        @Override
        public void onCompleteQbChatLogin() {
            isLoginNow = false;

            saveOpeningDialog(true);

            Intent intent = SystemUtils.getPreviousIntent(context);
            if (!isPushForPrivateChat() || intent == null) {
                sendNotification(message);
            }
        }

        @Override
        public void onCompleteWithError(String error) {
            isLoginNow = false;

            saveOpeningDialog(false);
        }
    }

    private class CallLoginListener extends SimpleGlobalLoginListener {

        @Override
        public void onCompleteQbChatLogin() {
            M.E("onCompleteQbChatLogin");
            QBInitCallChatCommand.start(context, CallActivity.class);
            qbRtcClient.addSessionCallbacksListener(new QBRTCClientSessionCallbacks() {
                @Override
                public void onReceiveNewSession(QBRTCSession qbrtcSession) {

                }

                @Override
                public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {

                }

                @Override
                public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {

                }

                @Override
                public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {

                }

                @Override
                public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer) {

                }

                @Override
                public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
                    sendNotification(message);
                }

                @Override
                public void onSessionClosed(QBRTCSession qbrtcSession) {

                }

                @Override
                public void onSessionStartClose(QBRTCSession qbrtcSession) {

                }
            });
        }

        @Override
        public void onCompleteWithError(String error) {
            isLoginNow = false;
            sendNotification(message);
        }
    }
}