package com.ss.fun2sh.utils.helpers.notification;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.NotificationEvent;
import com.quickblox.q_municate_core.models.StartConversationReason;
import com.quickblox.q_municate_core.qb.commands.chat.QBInitCallChatCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.ss.fun2sh.AppController;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.CRUD.NetworkUtil;
import com.ss.fun2sh.CRUD.Utility;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.call.CallActivity;
import com.ss.fun2sh.ui.activities.groupcall.activities.GroupCallActivity;
import com.ss.fun2sh.utils.SystemUtils;
import com.ss.fun2sh.utils.helpers.LoginHelper;
import com.ss.fun2sh.utils.helpers.SharedHelper;
import com.ss.fun2sh.utils.listeners.simple.SimpleGlobalLoginListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
            if (AppSession.getSession().getUser() != null) {
                M.E("in call notification");
                LoginHelper loginHelper = new LoginHelper(context);
                loginHelper.makeGeneralLogin(new CallLoginListener());
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
            //QBInitCallChatCommand.start(context, CallActivity.class);
            QBInitCallChatCommand.start(context, GroupCallActivity.class);
            qbRtcClient.addSessionCallbacksListener(new QBRTCClientSessionCallbacks() {
                @Override
                public void onReceiveNewSession(QBRTCSession qbrtcSession) {
                    M.E("onReceive chatnotification");
                    ///startCallActivity(qbrtcSession);
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
                public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {

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

    private void startCallActivity(QBRTCSession qbRtcSession) {
        User user = DataManager.getInstance().getUserDataManager()
                .get(qbRtcSession.getSessionDescription().getCallerID());

        M.E("onReceive call activity");

        if (user != null) {
            List<QBUser> qbUsersList = new ArrayList<>(1);
            qbUsersList.add(UserFriendUtils.createQbUser(user));
            Intent intent = new Intent(context, CallActivity.class);
            intent.putExtra(QBServiceConsts.EXTRA_OPPONENTS, (Serializable) qbUsersList);
            intent.putExtra(QBServiceConsts.EXTRA_START_CONVERSATION_REASON_TYPE, StartConversationReason.INCOME_CALL_FOR_ACCEPTION);
            intent.putExtra(QBServiceConsts.EXTRA_CONFERENCE_TYPE, qbRtcSession.getConferenceType());
            intent.putExtra(QBServiceConsts.EXTRA_SESSION_DESCRIPTION, qbRtcSession.getSessionDescription());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            context.getApplicationContext().startActivity(intent);
        } else {
            throw new NullPointerException("user is null!");
        }
    }

}