package com.ss.fun2sh.ui.activities.groupcall.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.q_municate_core.models.StartConversationReason;
import com.quickblox.q_municate_core.qb.helpers.QBCallChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_core.utils.call.RingtonePlayer;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.quickblox.videochat.webrtc.AppRTCAudioManager.AudioDevice;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCSessionDescription;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCStatsReportCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;
import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.groupcall.adapters.OpponentsAdapter;
import com.ss.fun2sh.ui.activities.groupcall.fragments.ConversationFragment;
import com.ss.fun2sh.ui.activities.groupcall.fragments.IncomeCallFragment;
import com.ss.fun2sh.ui.activities.groupcall.fragments.OnCallSettingsController;
import com.ss.fun2sh.ui.activities.groupcall.fragments.OpponentsFragment;
import com.ss.fun2sh.ui.activities.groupcall.fragments.SessionController;
import com.ss.fun2sh.ui.activities.groupcall.utils.ChatPingAlarmManager;
import com.ss.fun2sh.ui.activities.groupcall.utils.FragmentExecuotr;
import com.ss.fun2sh.ui.activities.groupcall.utils.NetworkConnectionChecker;
import com.ss.fun2sh.ui.activities.groupcall.utils.SettingsUtil;
import com.ss.fun2sh.utils.ToastUtils;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.webrtc.RendererCommon;
import org.webrtc.VideoCapturerAndroid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QuickBlox team
 */
public class GroupCallActivity extends BaseLogginedUserActivity implements QBRTCClientSessionCallbacks,
        QBRTCSessionConnectionCallbacks, QBRTCSignalingCallback,
        SessionController, OnCallSettingsController,
        NetworkConnectionChecker.OnConnectivityChangedListener {

    private static final String TAG = GroupCallActivity.class.getSimpleName();

    public static final String OPPONENTS_CALL_FRAGMENT = "opponents_call_fragment";
    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final String CONVERSATION_CALL_FRAGMENT = "conversation_call_fragment";
    public static final String CALLER_NAME = "caller_name";
    public static final String SESSION_ID = "sessionID";
    public static final String START_CONVERSATION_REASON = "start_conversation_reason";


    private QBRTCSession currentSession;
    public List<QBUser> opponentsList;
    private Runnable showIncomingCallWindowTask;
    private Handler showIncomingCallWindowTaskHandler;
    private BroadcastReceiver wifiStateReceiver;
    private boolean closeByWifiStateAllow = true;
    private String hangUpReason;
    private boolean isInCommingCall;
    private QBCallChatHelper qbCallChatHelper;
    private QBRTCClient rtcClient;
    private QBRTCSessionUserCallback sessionUserCallback;
    private boolean wifiEnabled = true;
    private SharedPreferences sharedPref;
    private RingtonePlayer ringtonePlayer;
    private LinearLayout connectionView;
    private AppRTCAudioManager audioManager;
    private AudioStateCallback audioStateCallback;
    private NetworkConnectionChecker networkConnectionChecker;
    DataManager dataManager;

    private StartConversationReason startConversationReason;
    private QBRTCSessionDescription qbRtcSessionDescription;
    private QBRTCTypes.QBConferenceType qbConferenceType;

    public static void start(Activity activity, List<QBUser> qbUsersList) {
        Intent intent = new Intent(activity, GroupCallActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_OPPONENTS, (Serializable) qbUsersList);
        intent.putExtra(QBServiceConsts.EXTRA_START_CONVERSATION_REASON_TYPE, StartConversationReason.OUTCOME_CALL_MADE);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivityForResult(intent, Const.App_Ver.CALL_ACTIVITY_CLOSE);
    }

    public static void start(Activity activity, List<QBUser> qbUsersList, QBRTCTypes.QBConferenceType qbConferenceType,
                             QBRTCSessionDescription qbRtcSessionDescription) {

        Intent intent = new Intent(activity, GroupCallActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_OPPONENTS, (Serializable) qbUsersList);
        intent.putExtra(QBServiceConsts.EXTRA_CONFERENCE_TYPE, qbConferenceType);
        intent.putExtra(QBServiceConsts.EXTRA_START_CONVERSATION_REASON_TYPE, StartConversationReason.SINGALUSER_CALL_MADE);
        intent.putExtra(QBServiceConsts.EXTRA_SESSION_DESCRIPTION, qbRtcSessionDescription);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivityForResult(intent, Const.App_Ver.CALL_ACTIVITY_CLOSE);
    }

    @Override
    protected int getContentResId() {
        return R.layout.group_activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.initActionBar();
        dataManager = DataManager.getInstance();
        try {
            opponentsList = (List<QBUser>) getIntent().getSerializableExtra(QBServiceConsts.EXTRA_OPPONENTS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        qbConferenceType = (QBRTCTypes.QBConferenceType) getIntent().getSerializableExtra(QBServiceConsts.EXTRA_CONFERENCE_TYPE);
        startConversationReason = (StartConversationReason) getIntent().getSerializableExtra(QBServiceConsts.EXTRA_START_CONVERSATION_REASON_TYPE);
        qbRtcSessionDescription = (QBRTCSessionDescription) getIntent().getSerializableExtra(QBServiceConsts.EXTRA_SESSION_DESCRIPTION);


        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        initQBRTCClient();
        initWiFiManagerListener();
        ///initPingListener();
        initAudioManager();
        ringtonePlayer = new RingtonePlayer(this, R.raw.beep);
        connectionView = (LinearLayout) View.inflate(this, R.layout.group_connection_popup, null);

        Log.d(TAG, "User  logged in!");
        if (startConversationReason.equals(StartConversationReason.INCOME_CALL_FOR_ACCEPTION)) {
            initCallFragment();
        } else if (startConversationReason.equals(StartConversationReason.OUTCOME_CALL_MADE)) {
            addOpponentsFragment();
        }
    }


    private void initAudioManager() {
        audioManager = AppRTCAudioManager.create(this, new AppRTCAudioManager.OnAudioManagerStateListener() {
            @Override
            public void onAudioChangedState(AppRTCAudioManager.AudioDevice audioDevice) {
                M.T(GroupCallActivity.this, "Audio device swicthed to  " + audioDevice);
            }
        });
        audioManager.setDefaultAudioDevice(AudioDevice.EARPIECE);
        audioManager.setOnWiredHeadsetStateListener(new AppRTCAudioManager.OnWiredHeadsetStateListener() {
            @Override
            public void onWiredHeadsetStateChanged(boolean plugged, boolean hasMicrophone) {
                M.T(GroupCallActivity.this, "Headset " + (plugged ? "plugged" : "unplugged"));
                if (audioStateCallback != null) {
                    audioStateCallback.onWiredHeadsetStateChanged(plugged);
                }
            }
        });
    }

    private void initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(this);
        // Add signalling manager
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
            @Override
            public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                if (!createdLocally) {
                    rtcClient.addSignaling((QBWebRTCSignaling) qbSignaling);
                }
            }
        });

        rtcClient.setCameraErrorHendler(new VideoCapturerAndroid.CameraEventsHandler() {
            @Override
            public void onCameraError(final String s) {

                showToast("Camera error: " + s);
            }

            @Override
            public void onCameraFreezed(String s) {
                showToast("Camera freezed: " + s);
                hangUpCurrentSession("camera freezed" + s);
            }

            @Override
            public void onCameraOpening(int i) {
                showToast("Camera " + i + " opening");
            }

            @Override
            public void onFirstFrameAvailable() {
                showToast("onFirstFrameAvailable");
            }

            @Override
            public void onCameraClosed() {
                showToast("onCameraClosed");
            }
        });


        /*// Configure
        //
        QBRTCConfig.setMaxOpponentsCount(6);
        QBRTCConfig.setDisconnectTime(30);
        QBRTCConfig.setAnswerTimeInterval(30l);
        QBRTCConfig.setStatsReportInterval(60);
        QBRTCConfig.setDebugEnabled(true);


        // Add activity as callback to RTCClient
        rtcClient.addSessionCallbacksListener(this);
        // Start mange QBRTCSessions according to VideoCall parser's callbacks
        rtcClient.prepareToProcessCalls();*/

        QBChatService.getInstance().addConnectionListener(new AbstractConnectionListener() {

            @Override
            public void connectionClosedOnError(Exception e) {
                showNotificationPopUp(R.string.connection_was_lost, true);
            }

            @Override
            public void reconnectionSuccessful() {
                showNotificationPopUp(R.string.connection_was_lost, false);
            }

            @Override
            public void reconnectingIn(int seconds) {
                Log.i(TAG, "reconnectingIn" + seconds);
            }
        });
    }

    private void initCallFragment() {
        switch (startConversationReason) {
            case INCOME_CALL_FOR_ACCEPTION:
                isInCommingCall = true;
                addIncomeCallFragment(currentSession);
                initIncommingCallTask();
                break;
            case OUTCOME_CALL_MADE:
                addOpponentsFragment();
                break;
            case SINGALUSER_CALL_MADE:
                Map<String, String> userInfo = new HashMap<>();
                userInfo.put("any_custom_data", "some data");
                userInfo.put("my_avatar_url", "avatar_reference");
                addConversationFragmentStartCall(opponentsList,
                        qbConferenceType, userInfo);
                break;
        }
    }

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);
        M.E("onConnectedToService");
        if (qbCallChatHelper == null) {
            M.E("onConnectedToService if");
            qbCallChatHelper = (QBCallChatHelper) service.getHelper(QBService.CALL_CHAT_HELPER);
            qbCallChatHelper.addRTCSessionUserCallback(GroupCallActivity.this);
            currentSession = qbCallChatHelper.getCurrentRtcSession();
            initCallFragment();
        }
    }

    private void showNotificationPopUp(final int text, final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    ((TextView) connectionView.findViewById(R.id.notification)).setText(text);
                    if (connectionView.getParent() == null) {
                        ((ViewGroup) GroupCallActivity.this.findViewById(R.id.fragment_container)).addView(connectionView);
                    }
                } else {
                    ((ViewGroup) GroupCallActivity.this.findViewById(R.id.fragment_container)).removeView(connectionView);
                }
            }
        });
    }

    private void initWiFiManagerListener() {
        networkConnectionChecker = new NetworkConnectionChecker(getApplication());
    }

    private void initPingListener() {
        ChatPingAlarmManager.onCreate(this);
        ChatPingAlarmManager.getInstanceFor().addPingListener(new PingFailedListener() {
            @Override
            public void pingFailed() {
                showToast("Ping chat server failed");
            }
        });
    }

    private void disableConversationFragmentButtons() {
        ConversationFragment fragment = (ConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
        if (fragment != null) {
            fragment.actionButtonsEnabled(false);
        }
    }

    private void initIncommingCallTask() {
        showIncomingCallWindowTaskHandler = new Handler(Looper.myLooper());
        showIncomingCallWindowTask = new Runnable() {
            @Override
            public void run() {
                IncomeCallFragment incomeCallFragment = (IncomeCallFragment) getFragmentManager().findFragmentByTag(INCOME_CALL_FRAGMENT);
                if (incomeCallFragment == null) {
                    ConversationFragment conversationFragment = (ConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
                    if (conversationFragment != null) {
                        disableConversationFragmentButtons();
                        ringtonePlayer.stop();
                        hangUpCurrentSession("  due to opponent has no action ");
                    }
                } else {
                    rejectCurrentSession(" opponent didn't answer");
                }
                M.E("Call was stopped by timer");
            }
        };
    }

    public void rejectCurrentSession(String rejectReason) {
        if (qbCallChatHelper != null && qbCallChatHelper.getCurrentRtcSession() != null) {
            Map<String, String> infoMap = new HashMap<>();
            infoMap.put(Const.App_Ver.REJECT_REASON, rejectReason);
            qbCallChatHelper.getCurrentRtcSession().rejectCall(infoMap);
        }
        finish();
    }

    @Override
    public void hangUpCurrentSession(String hangUpReason) {
        if (ringtonePlayer != null) {
            ringtonePlayer.stop();
        }
        if (qbCallChatHelper != null && qbCallChatHelper.getCurrentRtcSession() != null) {
            Map<String, String> infoMap = new HashMap<>();
            infoMap.put(Const.App_Ver.HANG_UP_REASON, hangUpReason);
            qbCallChatHelper.getCurrentRtcSession().hangUp(infoMap);
        }
    }

    private void startIncomeCallTimer(long time) {
        if (showIncomingCallWindowTaskHandler != null) {
            showIncomingCallWindowTaskHandler.postAtTime(showIncomingCallWindowTask, SystemClock.uptimeMillis() + time);
        }
    }

    private void stopIncomeCallTimer() {
        Log.d(TAG, "stopIncomeCallTimer");
        showIncomingCallWindowTaskHandler.removeCallbacks(showIncomingCallWindowTask);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (currentSession == null) {
            addOpponentsFragment();
        }
        super.onResume();
        networkConnectionChecker.registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        networkConnectionChecker.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    public QBRTCSession getCurrentSession() {
        if (qbCallChatHelper != null) {
            return qbCallChatHelper.getCurrentRtcSession();
        } else {
            return null;
        }
    }

    private void forbidenCloseByWifiState() {
        closeByWifiStateAllow = false;
    }


    // ---------------Chat callback methods implementation  ----------------------//

    @Override
    public void onReceiveNewSession(final QBRTCSession session) {

        /*Log.d(TAG, "Session " + session.getSessionID() + " are income");
        String curSession = (getCurrentSession() == null) ? null : getCurrentSession().getSessionID();

        if (getCurrentSession() == null) {
            Log.d(TAG, "Start new session");
            initCurrentSession(session);
            addIncomeCallFragment(session);

            isInCommingCall = true;
            initIncommingCallTask();
        } else {
            Log.d(TAG, "Stop new session. Device now is busy");
            Map<String, String> infoMap = new HashMap<>();
            infoMap.put(Const.App_Ver.REJECT_REASON, "I'm on a call right now!");
            session.rejectCall(infoMap);
        }*/


    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userID) {
        if (!session.equals(getCurrentSession())) {
            return;
        }
        if (sessionUserCallback != null) {
            sessionUserCallback.onUserNotAnswer(session, userID);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ringtonePlayer.stop();
            }
        });

    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
        startIncomeCallTimer(0);
    }

    @Override
    public void onStartConnectToUser(QBRTCSession session, Integer userID) {

    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        if (!session.equals(getCurrentSession())) {
            return;
        }
        if (sessionUserCallback != null) {
            sessionUserCallback.onCallAcceptByUser(session, userId, userInfo);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ringtonePlayer.stop();
            }
        });

    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, final Integer userID, final Map<String, String> userInfo) {
        if (!session.equals(getCurrentSession())) {
            return;
        }
        final String reason = userInfo != null ? userInfo.get(Const.App_Ver.REJECT_REASON) : "";
        if (sessionUserCallback != null) {
            ToastUtils.longToast("The user, whom you have called, is busy. Please try later.");
            sessionUserCallback.onCallRejectByUser(session, userID, reason);
        }


        String participantName = UserFriendUtils.getUserNameByID(userID, opponentsList);
        // showToast("User " + participantName + " " + getString(R.string.rejected) + " conversation:" + reason);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ringtonePlayer.stop();
            }
        });

    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession session, Integer userID) {

        // Close app after session close of network was disabled
        if (hangUpReason != null && hangUpReason.equals(Const.App_Ver.WIFI_DISABLED)) {
            Intent returnIntent = new Intent();
            setResult(Const.App_Ver.CALL_ACTIVITY_CLOSE_WIFI_DISABLED, returnIntent);
            finish();
        }

    }

    @Override
    public void onConnectedToUser(QBRTCSession session, final Integer userID) {
        forbidenCloseByWifiState();

        if (isInCommingCall) {
            stopIncomeCallTimer();
        }

        startTimer();
        Log.d(TAG, "onConnectedToUser() is started");


    }


    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession session, Integer userID) {

    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession session, Integer userID) {

    }

    @Override
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {
    }

    @Override
    public void onSessionClosed(final QBRTCSession session) {
        Log.d(TAG, "Session " + session.getSessionID() + " start stop session");
        String curSession = (getCurrentSession() == null) ? null : getCurrentSession().getSessionID();

        if (session.equals(getCurrentSession())) {

            Fragment currentFragment = getCurrentFragment();
            if (isInCommingCall) {
                stopIncomeCallTimer();
                if (currentFragment instanceof IncomeCallFragment) {
                    removeIncomeCallFragment();
                    finish();
                }
            }

            Log.d(TAG, "Stop session");
            /*if (!(currentFragment instanceof OpponentsFragment)) {
                addOpponentsFragment();
            }*/

            if (audioManager != null) {
                audioManager.close();
            }

            if (qbCallChatHelper != null) {
                qbCallChatHelper.releaseCurrentSession(GroupCallActivity.this, GroupCallActivity.this);
            }
            //releaseCurrentSession();

            stopTimer();
            closeByWifiStateAllow = true;
        }

    }

    @Override
    public void onSessionStartClose(final QBRTCSession session) {
        session.removeSessionCallbacksListener(GroupCallActivity.this);

        ConversationFragment fragment = (ConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
        if (fragment != null && session.equals(getCurrentSession())) {
            fragment.actionButtonsEnabled(false);
        }

    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession session, Integer userID) {

    }

    private void showToast(final int message) {
        M.T(GroupCallActivity.this, String.valueOf(message));
    }

    private void showToast(final String message) {
        M.T(GroupCallActivity.this, message);
    }

    @Override
    public void onReceiveHangUpFromUser(final QBRTCSession session, final Integer userID, Map<String, String> userInfo) {
        String reason = userInfo != null ? userInfo.get(Const.App_Ver.HANG_UP_REASON) : "";
        if (session.equals(getCurrentSession())) {

            if (sessionUserCallback != null) {
                sessionUserCallback.onReceiveHangUpFromUser(session, userID, reason);
            }
        }
        final String participantName = UserFriendUtils.getUserNameByID(userID, opponentsList);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.longToast("User " + participantName + " " + getString(
                        R.string.call_hung_up) + " this call. Please try later.");
            }
        });
    }

    @Override
    public void onVideoScalingSwitch(RendererCommon.ScalingType scalingType) {

    }

    @Override
    public void onCaptureFormatChange(int width, int height, int framerate) {
        currentSession.getMediaStreamManager().changeCaptureFormat(width, height, framerate);
    }

    @Override
    public void connectivityChanged(boolean availableNow) {
        //showToast("Internet connection " + (availableNow ? "available" : " unavailable"));
    }


    private Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentById(R.id.fragment_container);
    }

    public void addOpponentsFragment() {
        FragmentExecuotr.addFragment(getFragmentManager(), R.id.fragment_container, new OpponentsFragment(), OPPONENTS_CALL_FRAGMENT);
    }

    public void removeIncomeCallFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(INCOME_CALL_FRAGMENT);

        if (fragment != null) {
            FragmentExecuotr.removeFragment(fragmentManager, fragment);
        }
    }

    private void addIncomeCallFragment(QBRTCSession session) {

        Log.d(TAG, "QBRTCSession in addIncomeCallFragment is " + session);
        if (session != null) {
            setOptionsForSession(session, getDefaultSharedPrefs());
            Fragment fragment = new IncomeCallFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(QBServiceConsts.EXTRA_SESSION_DESCRIPTION, session.getSessionDescription());
            bundle.putIntegerArrayList(QBServiceConsts.EXTRA_OPPONENTS, new ArrayList<>(session.getOpponents()));
            bundle.putInt(QBServiceConsts.EXTRA_CONFERENCE_TYPE, session.getConferenceType().getValue());
            fragment.setArguments(bundle);
            FragmentExecuotr.addFragment(getFragmentManager(), R.id.fragment_container, fragment, INCOME_CALL_FRAGMENT);
        } else {
            Log.d(TAG, "SKIP addIncomeCallFragment method");
        }
    }

    public void addConversationFragmentStartCall(List<QBUser> opponents,
                                                 QBRTCTypes.QBConferenceType qbConferenceType,
                                                 Map<String, String> userInfo) {
        QBRTCSession newSessionWithOpponents = rtcClient.createNewSessionWithOpponents(
                UserFriendUtils.getFriendIdsList(opponentsList), qbConferenceType);
        SettingsUtil.setSettingsStrategy(opponents,
                getDefaultSharedPrefs(),
                this);

        setOptionsForSession(newSessionWithOpponents, getDefaultSharedPrefs());

        Log.d(TAG, "addConversationFragmentStartCall. Set session " + newSessionWithOpponents);
        if (qbCallChatHelper != null) {
            qbCallChatHelper.initCurrentSession(newSessionWithOpponents, this, this);
            ConversationFragment fragment = ConversationFragment.newInstance(opponents, opponents.get(0).getFullName(),
                    qbConferenceType, userInfo,
                    StartConversationReason.OUTCOME_CALL_MADE, qbCallChatHelper.getCurrentRtcSession().getSessionID());
            FragmentExecuotr.addFragment(getFragmentManager(), R.id.fragment_container, fragment, CONVERSATION_CALL_FRAGMENT);
            audioManager.init();
            ringtonePlayer.play(true);
        } else {
            throw new NullPointerException("qbCallChatHelper is not initialized");
        }

    }


    public static ArrayList<Integer> getOpponentsIds(List<QBUser> opponents) {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (QBUser user : opponents) {
            ids.add(user.getId());
        }
        return ids;
    }


    public void addConversationFragmentReceiveCall() {
        if (qbCallChatHelper != null) {
            QBRTCSession session = qbCallChatHelper.getCurrentRtcSession();
            if (session != null) {
                Integer myId = QBChatService.getInstance().getUser().getId();
                ArrayList<Integer> opponentsWithoutMe = new ArrayList<>(session.getOpponents());
                opponentsWithoutMe.remove(new Integer(myId));
                opponentsWithoutMe.add(session.getCallerID());

                ArrayList<QBUser> newOpponents = (ArrayList<QBUser>) UserFriendUtils
                        .getUsersByIDs(opponentsWithoutMe.toArray(new Integer[opponentsWithoutMe.size()]),
                                opponentsList);

                SettingsUtil.setSettingsStrategy(newOpponents, getDefaultSharedPrefs(), this);

                setOptionsForSession(session, getDefaultSharedPrefs());

                ConversationFragment fragment = ConversationFragment.newInstance(newOpponents,
                        UserFriendUtils.getUserNameByID(session.getCallerID(), newOpponents),
                        session.getConferenceType(), session.getUserInfo(),
                        StartConversationReason.INCOME_CALL_FOR_ACCEPTION, getCurrentSession().getSessionID());
                // Start conversation fragment
                FragmentExecuotr.addFragment(getFragmentManager(), R.id.fragment_container, fragment, CONVERSATION_CALL_FRAGMENT);
                audioManager.init();
            }
        }
    }


    public void setOpponentsList(List<QBUser> qbUsers) {
        this.opponentsList = qbUsers;
    }

    public List<QBUser> getOpponentsList() {
        return opponentsList;
    }

    @Override
    public void addVideoTrackCallbacksListener(QBRTCClientVideoTracksCallbacks videoTracksCallbacks) {
        if (currentSession != null) {
            currentSession.addVideoTrackCallbacksListener(videoTracksCallbacks);
        }
    }

    @Override
    public void addTCClientConnectionCallback(QBRTCSessionConnectionCallbacks sessionConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.addSessionCallbacksListener(sessionConnectionCallbacks);
        }
    }

    private void setOptionsForSession(QBRTCSession session, SharedPreferences sharedPref) {
        QBRTCSession.SessionOptions sessionOptions = new QBRTCSession.SessionOptions();
        sessionOptions.leaveSessionIfInitiatorHangUp = sharedPref.getBoolean(getString(R.string.pref_initiator_behaviour_key),
                Boolean.valueOf(getString(R.string.pref_initiator_behaviour_default)));
        session.setOptions(sessionOptions);
    }

    public void removeRTCClientConnectionCallback(QBRTCSessionConnectionCallbacks clientConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.removeSessionCallbacksListener(clientConnectionCallbacks);
        }
    }

    @Override
    public void addAudioStateCallback(AudioStateCallback audioStateCallback) {
        this.audioStateCallback = audioStateCallback;
    }

    @Override
    public void switchAudio() {
        if (audioManager.getSelectedAudioDevice() == AudioDevice.WIRED_HEADSET
                || audioManager.getSelectedAudioDevice() == AudioDevice.EARPIECE) {
            audioManager.setAudioDevice(AudioDevice.SPEAKER_PHONE);
        } else {
            audioManager.setAudioDevice(AudioDevice.EARPIECE);
        }
    }

    public void addRTCSessionUserCallback(QBRTCSessionUserCallback sessionUserCallback) {
        this.sessionUserCallback = sessionUserCallback;
    }

    public void removeRTCSessionUserCallback(QBRTCSessionUserCallback sessionUserCallback) {
        this.sessionUserCallback = null;
    }

    public void addRTCStatsReportCallback(QBRTCStatsReportCallback statsReportCallback) {
        if (currentSession != null) {
            currentSession.addStatsReportCallback(statsReportCallback);
        }
    }

    public void removeRTCStatsReportCallback(QBRTCStatsReportCallback statsReportCallback) {
        if (currentSession != null) {
            currentSession.removeStatsReportCallback(statsReportCallback);
        }
    }

    public void showSettings() {
        SettingsActivity.start(this);
    }


    public SharedPreferences getDefaultSharedPrefs() {
        return sharedPref;
    }

    @Override
    public void onSuccessSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer) {
    }

    @Override
    public void onErrorSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer userId, QBRTCSignalException e) {
        showToast(R.string.dlg_signal_error);
    }

    @Override
    public void onSwitchAudio() {
        if (audioManager.getSelectedAudioDevice() == AudioDevice.WIRED_HEADSET
                || audioManager.getSelectedAudioDevice() == AudioDevice.EARPIECE) {
            audioManager.setAudioDevice(AudioDevice.SPEAKER_PHONE);
        } else {
            audioManager.setAudioDevice(AudioDevice.EARPIECE);
        }
    }

    @Override
    public void onUseHeadSet(boolean use) {
        audioManager.setManageHeadsetByDefault(use);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
        if (fragment == null) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        opponentsList = null;
        OpponentsAdapter.i = 0;
        if (qbCallChatHelper != null) {
            qbCallChatHelper.removeRTCSessionUserCallback();
            qbCallChatHelper.releaseCurrentSession(GroupCallActivity.this, GroupCallActivity.this);
        }
        hangUpCurrentSession("due tp app was closed ");
    }

}

