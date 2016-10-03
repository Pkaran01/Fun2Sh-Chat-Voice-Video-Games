package com.ss.fun2sh.ui.activities.call;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;

import com.quickblox.chat.QBChatService;
import com.quickblox.q_municate_core.models.StartConversationReason;
import com.quickblox.q_municate_core.qb.helpers.QBCallChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.DateUtilsCore;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_core.utils.call.CameraUtils;
import com.quickblox.q_municate_core.utils.call.RingtonePlayer;
import com.quickblox.q_municate_core.utils.call.SettingsUtil;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Call;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.quickblox.videochat.webrtc.QBMediaStreamManager;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCSessionDescription;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;
import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.base.BaseLoggableActivity;
import com.ss.fun2sh.ui.activities.groupcall.fragments.SessionController;
import com.ss.fun2sh.ui.fragments.call.ConversationCallFragment;
import com.ss.fun2sh.ui.fragments.call.IncomingCallFragment;
import com.ss.fun2sh.utils.ToastUtils;

import org.webrtc.RendererCommon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;

import static com.ss.fun2sh.CRUD.M.E;

public class CallActivity extends BaseLoggableActivity implements
        QBRTCClientSessionCallbacks, QBRTCSessionConnectionCallbacks, QBRTCSignalingCallback, SessionController {

    public static final int CALL_ACTIVITY_CLOSE = 1000;
    public static final int CALL_ACTIVITY_CLOSE_WIFI_DISABLED = 1001;

    private static final String TAG = CallActivity.class.getSimpleName();

    @Bind(R.id.timer_chronometer)
    Chronometer timerChronometer;

    private QBRTCTypes.QBConferenceType qbConferenceType;
    private List<QBUser> opponentsList;
    private Runnable showIncomingCallWindowTask;
    private Handler showIncomingCallWindowTaskHandler;
    private BroadcastReceiver wifiStateReceiver;
    private boolean closeByWifiStateAllow = true;
    private String hangUpReason;
    private boolean isInComingCall;
    private boolean isInFront;
    private QBRTCClient qbRtcClient;
    private boolean wifiEnabled = true;
    private RingtonePlayer ringtonePlayer;
    private boolean isStarted = false;

    private SessionController.QBRTCSessionUserCallback qbRtcSessionUserCallback;
    private QBCallChatHelper qbCallChatHelper;
    private StartConversationReason startConversationReason;
    private QBRTCSessionDescription qbRtcSessionDescription;
    private ActionBar actionBar;
    private boolean isSpeakerEnabled;
    private AppRTCAudioManager audioManager;
    private String ACTION_ANSWER_CALL = "action_answer_call";
    private SessionController.AudioStateCallback audioStateCallback;
    DataManager dataManager;
    private Call call;

    private SharedPreferences sharedPref;

    public static void start(Activity activity, List<QBUser> qbUsersList, QBRTCTypes.QBConferenceType qbConferenceType,
                             QBRTCSessionDescription qbRtcSessionDescription) {

        Intent intent = new Intent(activity, CallActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_OPPONENTS, (Serializable) qbUsersList);
        intent.putExtra(QBServiceConsts.EXTRA_CONFERENCE_TYPE, qbConferenceType);
        intent.putExtra(QBServiceConsts.EXTRA_START_CONVERSATION_REASON_TYPE, StartConversationReason.OUTCOME_CALL_MADE);
        intent.putExtra(QBServiceConsts.EXTRA_SESSION_DESCRIPTION, qbRtcSessionDescription);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivityForResult(intent, CALL_ACTIVITY_CLOSE);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_call;
    }

    public void initActionBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_call);
        if (toolbar != null) {
            toolbar.setVisibility(View.VISIBLE);
            setSupportActionBar(toolbar);
        }

        actionBar = getSupportActionBar();

    }

    @Override
    public void addAudioStateCallback(SessionController.AudioStateCallback audioStateCallback) {
        this.audioStateCallback = audioStateCallback;
    }

    @Override
    public void switchAudio() {
        if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.WIRED_HEADSET
                || audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.EARPIECE) {
            audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
        }
    }

    @Override
    public void onUseHeadSet(boolean use) {
        audioManager.setManageHeadsetByDefault(use);
    }

    @Override
    public void onVideoScalingSwitch(RendererCommon.ScalingType scalingType) {

    }

    @Override
    public void onCaptureFormatChange(int width, int height, int framerate) {
        getCurrentSession().getMediaStreamManager().changeCaptureFormat(width, height, framerate);
    }

    public void setCallActionBarTitle(String title) {
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    public void hideCallActionBar() {
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    public void showCallActionBar() {
        if (actionBar != null) {
            actionBar.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        dataManager = DataManager.getInstance();
        canPerformLogout.set(false);
        initFields();


        //initWiFiManagerListener();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
        wakeLock.acquire();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if (ACTION_ANSWER_CALL.equals(getIntent().getAction())) {
            addConversationFragmentReceiveCall();
        }

    }

    public SharedPreferences getDefaultSharedPrefs() {
        return sharedPref;
    }

    private void initAudioManager() {
        audioManager = AppRTCAudioManager.create(this, new AppRTCAudioManager.OnAudioManagerStateListener() {
            @Override
            public void onAudioChangedState(AppRTCAudioManager.AudioDevice audioDevice) {
                M.T(CallActivity.this, "Audio device swicthed to  " + audioDevice);
            }
        });
        audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
        audioManager.setOnWiredHeadsetStateListener(new AppRTCAudioManager.OnWiredHeadsetStateListener() {
            @Override
            public void onWiredHeadsetStateChanged(boolean plugged, boolean hasMicrophone) {
                M.T(CallActivity.this, "Headset " + (plugged ? "plugged" : "unplugged"));
                if (audioStateCallback != null) {
                    audioStateCallback.onWiredHeadsetStateChanged(plugged);
                }
            }
        });
    }

    private void initCallFragment() {
        switch (startConversationReason) {
            case INCOME_CALL_FOR_ACCEPTION:
                if (qbRtcSessionDescription != null) {
                    addIncomingCallFragment(qbRtcSessionDescription);
                    isInComingCall = true;
                    initIncomingCallTask();
                }
                break;
            case OUTCOME_CALL_MADE:
                addConversationCallFragment();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dataManager.getCallDataManager().create(call);
        // unregisterReceiver(wifiStateReceiver);
    }

    @Override
    protected void onPause() {
        isInFront = false;
        super.onPause();
    }


    @Override
    protected void onResume() {
        isInFront = true;
        super.onResume();
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    PowerManager.WakeLock wakeLock;

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        //registerReceiver(wifiStateReceiver, intentFilter);

        ;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.call_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemSpeakerToggle = menu.findItem(R.id.switch_speaker_toggle);
        if (itemSpeakerToggle != null) {
            itemSpeakerToggle.setIcon(isSpeakerEnabled ? R.drawable.ic_phonelink_ring : R.drawable.ic_speaker_phone);
        }

        MenuItem itemCameraToggle = menu.findItem(R.id.switch_camera_toggle);
        if (itemCameraToggle != null && getCurrentSession() != null) {
            if (QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(getCurrentSession().getConferenceType())) {
                if (isVideoEnabled()) {
                    itemCameraToggle.setIcon(isFrontCameraSelected() ? R.drawable.ic_camera_front_white : R.drawable.ic_camera_rear_white);
                }

                itemCameraToggle.setVisible(true);
                itemCameraToggle.setEnabled(isVideoEnabled());
            } else {
                itemCameraToggle.setEnabled(false);
                itemCameraToggle.setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean isFrontCameraSelected() {
        QBRTCSession currentSession = getCurrentSession();
        QBMediaStreamManager mediaStreamManager;
        if (currentSession != null) {
            mediaStreamManager = currentSession.getMediaStreamManager();
        } else {
            return false;
        }

        if (mediaStreamManager != null) {
            return CameraUtils.isCameraFront(mediaStreamManager.getCurrentCameraId());
        } else {
            return false;
        }
    }

    private boolean isVideoEnabled() {
        QBRTCSession currentSession = getCurrentSession();
        QBMediaStreamManager mediaStreamManager;
        if (currentSession != null) {
            mediaStreamManager = currentSession.getMediaStreamManager();
        } else {
            return false;
        }

        if (mediaStreamManager != null) {
            return mediaStreamManager.isVideoEnabled();
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getCurrentSession() == null) {
            return super.onOptionsItemSelected(item);
        }

        QBMediaStreamManager mediaStreamManager = getCurrentSession().getMediaStreamManager();
        if (mediaStreamManager == null) {
            return super.onOptionsItemSelected(item);
        }

        switch (item.getItemId()) {
            case R.id.switch_speaker_toggle:
                if (isSpeakerEnabled) {
                    mediaStreamManager.switchAudioOutput();
                    isSpeakerEnabled = false;
                    invalidateOptionsMenu();
                } else {
                    mediaStreamManager.switchAudioOutput();
                    isSpeakerEnabled = true;
                    invalidateOptionsMenu();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        opponentsList = null;
        if (qbCallChatHelper != null) {
            qbCallChatHelper.removeRTCSessionUserCallback();
            qbCallChatHelper.releaseCurrentSession(CallActivity.this, CallActivity.this);
        }
    }

    @Override
    public void onSuccessSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer) {
    }

    @Override
    public void onErrorSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer userId,
                                     QBRTCSignalException e) {
        ToastUtils.longToast(R.string.dlg_signal_error);
    }

    @Override
    public void onReceiveNewSession(final QBRTCSession session) {
        Log.e(TAG, "Session " + session.getSessionID() + " are income");
    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userID) {
        if (!session.equals(getCurrentSession())) {
            return;
        }

        if (qbRtcSessionUserCallback != null) {
            qbRtcSessionUserCallback.onUserNotAnswer(session, userID);
        }
        //missed call ka code

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ringtonePlayer.stop();
            }
        });
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userID, Map<String, String> userInfo) {
        if (!session.equals(getCurrentSession())) {
            return;
        }
        final String reason = userInfo != null ? userInfo.get(Const.App_Ver.REJECT_REASON) : "";
        if (qbRtcSessionUserCallback != null) {
            ToastUtils.longToast("The user, whom you have called, is busy. Please try later.");
            qbRtcSessionUserCallback.onCallRejectByUser(session, userID, reason);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ringtonePlayer.stop();
            }
        });
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        if (!session.equals(getCurrentSession())) {
            return;
        }

        if (qbRtcSessionUserCallback != null) {
            qbRtcSessionUserCallback.onCallAcceptByUser(session, userId, userInfo);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ringtonePlayer.stop();
            }
        });
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession session, Integer userID, Map<String, String> userInfo) {
        //call.setStatus(4);
        String reason = userInfo != null ? userInfo.get(Const.App_Ver.HANG_UP_REASON) : "";
        if (session.equals(getCurrentSession())) {

            if (qbRtcSessionUserCallback != null) {
                qbRtcSessionUserCallback.onReceiveHangUpFromUser(session, userID, reason);
            }

            final String participantName = UserFriendUtils.getUserNameByID(userID, opponentsList);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.longToast("User " + participantName + " " + getString(
                            R.string.call_hung_up) + " this call. Please try later.");
                }
            });

            finish();
        }
    }


    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
        startIncomeCallTimer(0);
    }

    @Override
    public void onSessionClosed(final QBRTCSession session) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Session " + session.getSessionID() + " start stop session");

                if (session.equals(getCurrentSession())) {

                    Fragment currentFragment = getCurrentFragment();
                    if (isInComingCall) {
                        stopIncomeCallTimer();
                        if (currentFragment instanceof IncomingCallFragment) {
                            removeFragment();
                            finish();
                        }
                    }
                    Log.d(TAG, "Stop session");
                    if (qbCallChatHelper != null) {
                        qbCallChatHelper.releaseCurrentSession(CallActivity.this, CallActivity.this);
                    }

                    stopTimer();
                    closeByWifiStateAllow = true;
                    finish();
                }
            }
        });
    }

    @Override
    public void onSessionStartClose(final QBRTCSession session) {
        session.removeSessionCallbacksListener(CallActivity.this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (currentFragment instanceof ConversationCallFragment && session.equals(getCurrentSession())) {
                    ((ConversationCallFragment) currentFragment).actionButtonsEnabled(false);
                }
            }
        });
    }

    @Override
    public void onStartConnectToUser(QBRTCSession session, Integer userID) {

    }

    @Override
    public void onConnectedToUser(QBRTCSession session, final Integer userID) {
        forbiddenCloseByWifiState();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isInComingCall) {
                    stopIncomeCallTimer();
                }
                Log.d(TAG, "onConnectedToUser() is started");
            }
        });
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession session, Integer userID) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Close app after session close of network was disabled
                if (hangUpReason != null && hangUpReason.equals(QBServiceConsts.EXTRA_WIFI_DISABLED)) {
                    Intent returnIntent = new Intent();
                    setResult(CALL_ACTIVITY_CLOSE_WIFI_DISABLED, returnIntent);
                }
                finish();
            }
        });
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession session, Integer userID) {
        setMissedCallValue();
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
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);
        if (qbCallChatHelper == null) {
            qbCallChatHelper = (QBCallChatHelper) service.getHelper(QBService.CALL_CHAT_HELPER);
            qbCallChatHelper.addRTCSessionUserCallback(CallActivity.this);
            initCallFragment();
        }
    }

    private void initFields() {
        try {
            opponentsList = (List<QBUser>) getIntent().getSerializableExtra(QBServiceConsts.EXTRA_OPPONENTS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        qbConferenceType = (QBRTCTypes.QBConferenceType) getIntent().getSerializableExtra(QBServiceConsts.EXTRA_CONFERENCE_TYPE);
        startConversationReason = (StartConversationReason) getIntent().getSerializableExtra(QBServiceConsts.EXTRA_START_CONVERSATION_REASON_TYPE);
        qbRtcSessionDescription = (QBRTCSessionDescription) getIntent().getSerializableExtra(QBServiceConsts.EXTRA_SESSION_DESCRIPTION);

        initAudioManager();

        ringtonePlayer = new RingtonePlayer(this, R.raw.beep);
        // Add activity as callback to RTCClient

        qbRtcClient = QBRTCClient.getInstance(CallActivity.this);
        //store in database
        call = new Call();

        call.setUser(dataManager.getUserDataManager().get(opponentsList.get(0).getId()));
        call.setCallType(qbConferenceType.getValue());
        call.setCreatedDate(DateUtilsCore.getCurrentTime());
        call.setCallDuration(1245678);
        switch (startConversationReason) {
            case INCOME_CALL_FOR_ACCEPTION:
                call.setStatus(1);
                break;
            case OUTCOME_CALL_MADE:
                call.setStatus(2);
                break;
        }

    }


    public User getOpponentAsUserFromDB(int opponentId) {
        DataManager dataManager = DataManager.getInstance();
        Friend friend = dataManager.getFriendDataManager().getByUserId(opponentId);
        return friend.getUser();
    }

    private void processCurrentWifiState(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(WIFI_SERVICE);
        if (wifiEnabled != wifi.isWifiEnabled()) {
            wifiEnabled = wifi.isWifiEnabled();
            ToastUtils.longToast("Wifi " + (wifiEnabled ? "enabled" : "disabled"));
        }
    }

    private void disableConversationFragmentButtons() {
        if (currentFragment instanceof ConversationCallFragment) {
            ((ConversationCallFragment) currentFragment).actionButtonsEnabled(false);
        }
    }

    private void initIncomingCallTask() {
        showIncomingCallWindowTaskHandler = new Handler(Looper.myLooper());
        showIncomingCallWindowTask = new Runnable() {
            @Override
            public void run() {
                if (currentFragment instanceof ConversationCallFragment) {
                    disableConversationFragmentButtons();
                    ringtonePlayer.stop();
                    hangUpCurrentSession();
                } else {
                    rejectCurrentSession();
                }

                ToastUtils.longToast("Call was stopped by timer");
            }
        };
    }

    public void rejectCurrentSession() {
        M.E("rejectCurrentSesstion");
        if (qbCallChatHelper != null && qbCallChatHelper.getCurrentRtcSession() != null) {
            qbCallChatHelper.getCurrentRtcSession().rejectCall(new HashMap<String, String>());
        }
        finish();
    }

    public void hangUpCurrentSession() {
        M.E("stopIncomeCallTimer");
        ringtonePlayer.stop();
        if (qbCallChatHelper != null && qbCallChatHelper.getCurrentRtcSession() != null) {
            qbCallChatHelper.getCurrentRtcSession().hangUp(new HashMap<String, String>());
        }
        finish();
    }

    public void setMissedCallValue() {
        call.setStatus(3);
    }

    private void startIncomeCallTimer(long time) {
        showIncomingCallWindowTaskHandler
                .postAtTime(showIncomingCallWindowTask, SystemClock.uptimeMillis() + time);
    }

    private void stopIncomeCallTimer() {
        M.E("stopIncomeCallTimer");
        showIncomingCallWindowTaskHandler.removeCallbacks(showIncomingCallWindowTask);
    }

    private void forbiddenCloseByWifiState() {
        closeByWifiStateAllow = false;
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.container_fragment);
    }

    private void addIncomingCallFragment(QBRTCSessionDescription qbRtcSessionDescription) {
        Log.d(TAG, "QBRTCSession in addIncomingCallFragment is " + qbRtcSessionDescription);
        if (isInFront) {
            setOptionsForSession(getCurrentSession(), getDefaultSharedPrefs());
            Fragment fragment = new IncomingCallFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(QBServiceConsts.EXTRA_SESSION_DESCRIPTION, qbRtcSessionDescription);
            bundle.putIntegerArrayList(QBServiceConsts.EXTRA_OPPONENTS, new ArrayList<>(qbRtcSessionDescription.getOpponents()));
            bundle.putSerializable(QBServiceConsts.EXTRA_CONFERENCE_TYPE,
                    qbRtcSessionDescription.getConferenceType());
            fragment.setArguments(bundle);
            setCurrentFragment(fragment);
        } else {
            Log.d(TAG, "SKIP addIncomingCallFragment method");
        }
    }

    private void setOptionsForSession(QBRTCSession session, SharedPreferences sharedPref) {
        QBRTCSession.SessionOptions sessionOptions = new QBRTCSession.SessionOptions();
        sessionOptions.leaveSessionIfInitiatorHangUp = sharedPref.getBoolean(getString(R.string.pref_initiator_behaviour_key),
                Boolean.valueOf(getString(R.string.pref_initiator_behaviour_default)));
        session.setOptions(sessionOptions);
    }

    public void addConversationCallFragment() {
        Log.d(TAG, "addConversationCallFragment()");

        QBRTCSession newSessionWithOpponents = qbRtcClient.createNewSessionWithOpponents(
                UserFriendUtils.getFriendIdsList(opponentsList), qbConferenceType);
        SettingsUtil.setSettingsStrategy(this, opponentsList);

        if (qbCallChatHelper != null) {
            qbCallChatHelper.initCurrentSession(newSessionWithOpponents, this, this);
            setOptionsForSession(newSessionWithOpponents, getDefaultSharedPrefs());
            ConversationCallFragment fragment = ConversationCallFragment
                    .newInstance(opponentsList, opponentsList.get(0).getFullName(), qbConferenceType,
                            StartConversationReason.OUTCOME_CALL_MADE,
                            qbCallChatHelper.getCurrentRtcSession().getSessionID());

            setCurrentFragment(fragment);
            ringtonePlayer.play(true);
        } else {
            throw new NullPointerException("qbCallChatHelper is not initialized");
        }
    }

    public List<QBUser> getOpponentsList() {
        return opponentsList;
    }

    public void setOpponentsList(List<QBUser> qbUsers) {
        this.opponentsList = qbUsers;
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
                SettingsUtil.setSettingsStrategy(this, newOpponents);
                com.ss.fun2sh.ui.activities.groupcall.utils.SettingsUtil.setSettingsStrategy(newOpponents, getDefaultSharedPrefs(), this);
                setOptionsForSession(getCurrentSession(), getDefaultSharedPrefs());
                ConversationCallFragment fragment = ConversationCallFragment.newInstance(newOpponents,
                        UserFriendUtils.getUserNameByID(session.getCallerID(), newOpponents),
                        session.getConferenceType(), StartConversationReason.INCOME_CALL_FOR_ACCEPTION,
                        session.getSessionID());
                // Start conversation fragment
                setCurrentFragment(fragment);
            }
        }
    }

    public void startTimer() {
        E("startTimer() from CallActivity, timerChronometer = " + timerChronometer);
        if (!isStarted) {
            timerChronometer.setVisibility(View.VISIBLE);
            timerChronometer.setBase(SystemClock.elapsedRealtime());
            timerChronometer.start();
            isStarted = true;
        }
    }

    private void stopTimer() {
        if (timerChronometer != null) {
            timerChronometer.stop();
            isStarted = false;
        }
    }

    @Override
    public void hangUpCurrentSession(String hangUpReason) {
        if (ringtonePlayer != null) {
            ringtonePlayer.stop();
        }
        if (getCurrentSession() != null) {
            Map<String, String> infoMap = new HashMap<>();
            infoMap.put(Const.App_Ver.HANG_UP_REASON, hangUpReason);
            getCurrentSession().hangUp(infoMap);
        }
    }

    public QBRTCSession getCurrentSession() {
        if (qbCallChatHelper != null) {
            return qbCallChatHelper.getCurrentRtcSession();
        } else {
            return null;
        }
    }

    public void addVideoTrackCallbacksListener(QBRTCClientVideoTracksCallbacks videoTracksCallbacks) {
        if (getCurrentSession() != null) {
            getCurrentSession().addVideoTrackCallbacksListener(videoTracksCallbacks);
        }
    }

    @Override
    public void onBackPressed() {
        //blocked back button
    }

    public void addTCClientConnectionCallback(QBRTCSessionConnectionCallbacks clientConnectionCallbacks) {
        if (getCurrentSession() != null) {
            getCurrentSession().addSessionCallbacksListener(clientConnectionCallbacks);
        }
    }

    public void removeRTCClientConnectionCallback(QBRTCSessionConnectionCallbacks clientConnectionCallbacks) {
        if (getCurrentSession() != null) {
            getCurrentSession().removeSessionCallbacksListener(clientConnectionCallbacks);
        }
    }

    @Override
    public void removeRTCSessionUserCallback(QBRTCSessionUserCallback sessionUserCallback) {
        this.qbRtcSessionUserCallback = null;
    }

    public void addRTCSessionUserCallback(SessionController.QBRTCSessionUserCallback qbRtcSessionUserCallback) {
        this.qbRtcSessionUserCallback = qbRtcSessionUserCallback;
    }

    public void removeRTCSessionUserCallback() {
        this.qbRtcSessionUserCallback = null;
    }

//    @Override
//    public boolean isCanPerformLogoutInOnStop() {
//        return false;
//    }

}