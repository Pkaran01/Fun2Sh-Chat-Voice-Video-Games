package com.ss.fun2sh.ui.activities.groupcall.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.chat.QBChatService;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.StartConversationReason;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.qb.commands.push.QBSendPushCommand;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBMediaStreamManager;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;
import com.ss.fun2sh.oldutils.Constants;
import com.ss.fun2sh.ui.activities.base.BaseActivity;
import com.ss.fun2sh.ui.activities.groupcall.activities.GroupCallActivity;
import com.ss.fun2sh.ui.activities.groupcall.utils.FragmentLifeCycleHandler;
import com.ss.fun2sh.ui.activities.groupcall.utils.QBRTCSessionUtils;
import com.ss.fun2sh.utils.image.ImageLoaderUtils;
import com.ss.fun2sh.utils.image.ImageUtils;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * QuickBlox team
 */
public abstract class ConversationFragment extends Fragment implements SessionController.QBRTCSessionUserCallback,
        QBRTCClientVideoTracksCallbacks, QBRTCSessionConnectionCallbacks, SessionController.AudioStateCallback,
        FragmentLifeCycleHandler.FragmentLifycleListener {

    private static final String TAG = ConversationFragment.class.getSimpleName();

    public static final String CALLER_NAME = "caller_name";
    public static final String SESSION_ID = "sessionID";
    public static final String START_CONVERSATION_REASON = "start_conversation_reason";

    private static final long TOGGLE_CAMERA_DELAY = 1000;

    protected boolean isVideoEnabled = false;
    protected SurfaceViewRenderer localVideoView;
    protected EglBase rootEglBase;
    protected View view;

    protected ArrayList<QBUser> opponents;
    protected SessionController sessionController;
    private int qbConferenceType;
    private int startReason;
    private String sessionID;

    private ToggleButton cameraToggle;
    private ToggleButton switchCameraToggle;
    private ToggleButton dynamicToggleVideoCall;
    private ToggleButton micToggleVideoCall;
    private ImageButton handUpVideoCall;
    private View myCameraOff;
    private TextView incUserName;

    private Map<String, String> userInfo;
    private boolean isAudioEnabled = true;
    private String callerName;
    private boolean isMessageProcessed;

    private CameraState cameraState = CameraState.NONE;
    private boolean isPeerToPeerCall;
    private FragmentLifeCycleHandler mainHandler;
    private SeekBar captureFormatSlider;
    private TextView captureFormatText;
    private OnCallSettingsController videoSettingsController;
    private ImageButton videoScalingButton;
    private ScalingType scalingType = ScalingType.SCALE_ASPECT_FIT;
    Toolbar toolbar;
    boolean fullscreen = false;
    View element_set_video_buttons;
    LinearLayout switchcamerabtn, linearbackground;
    ImageView userimageView, local_imageviewown_layout;

    public static ConversationFragment newInstance(List<QBUser> opponents, String callerName,
                                                   QBRTCTypes.QBConferenceType qbConferenceType,
                                                   Map<String, String> userInfo,
                                                   StartConversationReason reason,
                                                   String sesionnId) {
        ConversationFragment fragment = (opponents.size() == 1) ? new OneToOneConversationFragment() :
                new GroupConversationFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(QBServiceConsts.EXTRA_CONFERENCE_TYPE, qbConferenceType.getValue());
        bundle.putString(CALLER_NAME, callerName);
        bundle.putSerializable(QBServiceConsts.EXTRA_OPPONENTS, (Serializable) opponents);
        if (userInfo != null) {
            for (String key : userInfo.keySet()) {
                bundle.putString("UserInfo:" + key, userInfo.get(key));
            }
        }
        bundle.putInt(START_CONVERSATION_REASON, reason.ordinal());
        if (sesionnId != null) {
            bundle.putString(SESSION_ID, sesionnId);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    protected abstract TextView getStatusViewForOpponent(int userId);

    protected abstract TextView getStatusViewForOpponentName(int userId);

    protected abstract void initCustomView(View view);

    protected abstract SurfaceViewRenderer getVideoViewForOpponent(Integer userID);

    protected abstract RelativeLayout getInnerRelative();

    protected View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState,
                                int contentId) {
        view = inflater.inflate(R.layout.group_kk_conversation_fragment, container, false);
        Log.d(TAG, "Fragment. Thread id: " + Thread.currentThread().getId());
        if (getArguments() != null) {
            opponents = (ArrayList<QBUser>) getArguments().getSerializable(QBServiceConsts.EXTRA_OPPONENTS);
            qbConferenceType = getArguments().getInt(QBServiceConsts.EXTRA_CONFERENCE_TYPE);
            startReason = getArguments().getInt(START_CONVERSATION_REASON);
            sessionID = getArguments().getString(SESSION_ID);
            callerName = getArguments().getString(CALLER_NAME);

            isPeerToPeerCall = opponents.size() == 1;
            isVideoEnabled = (qbConferenceType ==
                    QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.getValue());

            Log.d(TAG, "CALLER_NAME: " + callerName);
            Log.d(TAG, "opponents: " + opponents.toString());
        }


        // Create video renderers.
        initContentView(view, contentId);
        initViews(view);
        initButtonsListener();
        initSessionListener();
        setUpUiByCallType(qbConferenceType);

        mainHandler = new FragmentLifeCycleHandler(this);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#000000"));
        getInnerRelative().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVideoEnabled) {
                    if (CoreSharedHelper.getInstance().getPref(Constants.FUULSCREENPREF).toString().equalsIgnoreCase("true")) {
                        if (fullscreen == false) {
                            toolbar.setVisibility(View.GONE);
                            element_set_video_buttons.setVisibility(View.GONE);
                            fullscreen = true;
                        } else {
                            toolbar.setVisibility(View.VISIBLE);
                            element_set_video_buttons.setVisibility(View.VISIBLE);
                            fullscreen = false;
                        }
                    }
                } else {
                    if (CoreSharedHelper.getInstance().getPref(Constants.FUULSCREENPREF).toString().equalsIgnoreCase("true")) {
                        if (fullscreen == false) {
                            toolbar.setVisibility(View.GONE);
                            element_set_video_buttons.setVisibility(View.GONE);
                            fullscreen = true;
                        } else {
                            toolbar.setVisibility(View.VISIBLE);
                            element_set_video_buttons.setVisibility(View.VISIBLE);
                            fullscreen = false;
                        }
                    }
                }

            }
        });


        return view;
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        sessionController = (SessionController) context;
        videoSettingsController = (OnCallSettingsController) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        CoreSharedHelper.getInstance().savePref(Constants.FUULSCREENPREF, "false");
        CoreSharedHelper.getInstance().savePref(Constants.USERPICAUDIOPREF, "false");
        CoreSharedHelper.getInstance().savePref(Constants.USERORIENTATIONAUDIOPREF, "false");
        sessionController = null;
        videoSettingsController = null;
        mainHandler.detach();
    }

    private void initSessionListener() {
        sessionController.addVideoTrackCallbacksListener(this);
    }

    private void setUpUiByCallType(int qbConferenceType) {
        if (!isVideoEnabled) {
            cameraToggle.setVisibility(View.GONE);
            switchCameraToggle.setVisibility(View.INVISIBLE);
        }
    }

    public void actionButtonsEnabled(boolean enability) {

        cameraToggle.setEnabled(enability);
        micToggleVideoCall.setEnabled(enability);
        dynamicToggleVideoCall.setEnabled(enability);

        // inactivate toggle buttons
        cameraToggle.setActivated(enability);
        micToggleVideoCall.setActivated(enability);
        dynamicToggleVideoCall.setActivated(enability);

        switchCameraToggle.setEnabled(enability);
        switchCameraToggle.setActivated(enability);

        if (isVideoEnabled) {
            captureFormatSlider.setEnabled(enability);
            captureFormatSlider.setActivated(enability);
        }
    }


    @Override
    public void onStart() {

        super.onStart();
        QBRTCSession session = sessionController.getCurrentSession();
        if (!isMessageProcessed) {
            if (startReason == StartConversationReason.INCOME_CALL_FOR_ACCEPTION.ordinal()) {
                session.acceptCall(session.getUserInfo());
                if (isVideoEnabled) {
                    ((GroupCallActivity) getActivity()).initActionBarWithTimer("Video call from " + getOtherIncUsersNames(opponents));
                    if (CoreSharedHelper.getInstance().getPref(Constants.USERORIENTATIONAUDIOPREF).toString().equalsIgnoreCase("true")) {
                        //  ownUserIcon();
                        CoreSharedHelper.getInstance().savePref(Constants.USERORIENTATIONAUDIOPREF, "false");
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }

                } else {
                    ((GroupCallActivity) getActivity()).initActionBarWithTimer("Audio call from " + getOtherIncUsersNames(opponents));

                    if (CoreSharedHelper.getInstance().getPref(Constants.AUDIOONETOONECALL).toString().equalsIgnoreCase("true")) {
                        setUserImage();
                    }

                    if (CoreSharedHelper.getInstance().getPref(Constants.USERPICAUDIOPREF).toString().equalsIgnoreCase("true")) {
                        ownUserIcon();
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    if (CoreSharedHelper.getInstance().getPref(Constants.USERORIENTATIONAUDIOPREF).toString().equalsIgnoreCase("true")) {
                        //  ownUserIcon();
                        CoreSharedHelper.getInstance().savePref(Constants.USERORIENTATIONAUDIOPREF, "false");
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }

                }
            } else {
                sendPushAboutCall();
                session.startCall(session.getUserInfo());
                if (isVideoEnabled) {
                    //  ((GroupCallActivity) getActivity()).initActionBarWithTimer("Video call to " + getOtherIncUsersNames(opponents));

                    if (CoreSharedHelper.getInstance().getPref(Constants.VIDEOONETOONECALL).toString().equalsIgnoreCase("true")) {
                        CoreSharedHelper.getInstance().savePref(Constants.VIDEOONETOONECALL, "false");
                        CoreSharedHelper.getInstance().savePref(Constants.FUULSCREENPREF, "true");
                        ((GroupCallActivity) getActivity()).initActionBarWithTimer("Video call to " + callerName);

                    } else if (CoreSharedHelper.getInstance().getPref(Constants.VIDEOGROUPCALL).toString().equalsIgnoreCase("true")) {
                        CoreSharedHelper.getInstance().savePref(Constants.VIDEOGROUPCALL, "false");
                        CoreSharedHelper.getInstance().savePref(Constants.FUULSCREENPREF, "false");
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        ((GroupCallActivity) getActivity()).initActionBarWithTimer("Video call to " + getOtherIncUsersNames(opponents));
                    }

                } else {
                    //  ((GroupCallActivity) getActivity()).initActionBarWithTimer("Audio call to " + getOtherIncUsersNames(opponents));

                    if (CoreSharedHelper.getInstance().getPref(Constants.AUDIOONETOONECALL).toString().equalsIgnoreCase("true")) {
                        CoreSharedHelper.getInstance().savePref(Constants.AUDIOONETOONECALL, "false");
                        CoreSharedHelper.getInstance().savePref(Constants.FUULSCREENPREF, "true");
                        linearbackground.setBackgroundResource(R.drawable.bg);
                        setUserImage();
                        ((GroupCallActivity) getActivity()).initActionBarWithTimer("Audio call to " + callerName);
                    } else if (CoreSharedHelper.getInstance().getPref(Constants.AUDIOGROUPCALL).toString().equalsIgnoreCase("true")) {
                        CoreSharedHelper.getInstance().savePref(Constants.AUDIOGROUPCALL, "false");
                        CoreSharedHelper.getInstance().savePref(Constants.FUULSCREENPREF, "false");
                        CoreSharedHelper.getInstance().savePref(Constants.USERPICAUDIOPREF, "true");
                        ownUserIcon();
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        ((GroupCallActivity) getActivity()).initActionBarWithTimer("Audio call to " + getOtherIncUsersNames(opponents));
                    }

                }
            }
            isMessageProcessed = true;
        }
        sessionController.addTCClientConnectionCallback(this);
        sessionController.addRTCSessionUserCallback(this);
        sessionController.addAudioStateCallback(this);
    }

    protected void setUserImage() {
        if (userimageView != null) {
            userimageView.setVisibility(View.VISIBLE);
            if (DataManager.getInstance().getUserDataManager().get(getOpponentUserId(opponents)).getAvatar().toString().trim().equalsIgnoreCase("")) {
                userimageView.setBackgroundResource(R.drawable.avatarprofilecaling);
            } else {
                loadusersLogo(DataManager.getInstance().getUserDataManager().get(getOpponentUserId(opponents)).getAvatar(), userimageView);
            }
        }
    }

    private void ownUserIcon() {
        if (local_imageviewown_layout != null) {
            local_imageviewown_layout.setVisibility(View.VISIBLE);
            UserCustomData userCustomData = Utils.customDataToObject(AppSession.getSession().getUser().getCustomData());
            if (!TextUtils.isEmpty(userCustomData.getAvatar_url())) {
                loadusersLogo(userCustomData.getAvatar_url(), local_imageviewown_layout);
                PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.LAST_AVATAR_URL, userCustomData.getAvatar_url());
            } else {
                local_imageviewown_layout.setImageResource(R.drawable.avatarprofile);
            }
        }
    }


    protected void loadusersLogo(String logoUrl, final ImageView userimageView) {
        ImageLoader.getInstance().loadImage(logoUrl, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedBitmap) {
                        userimageView.setImageBitmap(loadedBitmap);
                    }
                });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() from " + TAG);
        super.onCreate(savedInstanceState);
    }

    protected void initContentView(View view, int layoutID) {
        ViewStub stubCompat = (ViewStub) view.findViewById(R.id.main_content);
        stubCompat.setLayoutResource(layoutID);
        stubCompat.inflate();
    }

    private String getOtherIncUsersNames(ArrayList<QBUser> opponents) {
        M.E("getOtherIncUsersNames size" + opponents);
        StringBuffer s = new StringBuffer("");
        opponents.remove(QBChatService.getInstance().getUser().getId());

        for (QBUser usr : opponents) {
            s.append(usr.getFullName() + ", ");
        }
        if (s.toString().endsWith(", ")) {
            return s.toString().substring(0, s.toString().length() - 2);
        } else {
            return s.toString();
        }
    }


    private int getOpponentUserId(ArrayList<QBUser> opponents) {
        M.E("getOtherIncUsersNames size" + opponents);
        opponents.remove(QBChatService.getInstance().getUser().getId());
        for (QBUser usr : opponents) {
            return usr.getId();
        }
        return 0;
    }


    private void initViews(View view) {
        localVideoView = (SurfaceViewRenderer) view.findViewById(R.id.localSurfView);
        userimageView = (ImageView) view.findViewById(R.id.userimageView);
        local_imageviewown_layout = (ImageView) view.findViewById(R.id.local_imageviewown_layout);
        linearbackground = (LinearLayout) view.findViewById(R.id.linearbackground);
        element_set_video_buttons = view.findViewById(R.id.element_set_video_buttons);
        localVideoView.setZOrderMediaOverlay(true);
        updateVideoView(localVideoView, false);
        initLocalViewUI(view);
        initCustomView(view);

        if (isVideoEnabled) {
            initVideoCallSettings(view);
        } else {
            localVideoView.setVisibility(View.INVISIBLE);
        }

        cameraToggle = (ToggleButton) view.findViewById(R.id.cameraToggle);
        dynamicToggleVideoCall = (ToggleButton) view.findViewById(R.id.dynamicToggleVideoCall);
        micToggleVideoCall = (ToggleButton) view.findViewById(R.id.micToggleVideoCall);

        handUpVideoCall = (ImageButton) view.findViewById(R.id.handUpVideoCall);
        incUserName = (TextView) view.findViewById(R.id.incUserName);
        incUserName.setText(callerName);
        actionButtonsEnabled(false);

        initRemoteView();
    }

    private void initVideoCallSettings(View view) {
        view.findViewById(R.id.video_call_settings_view).setVisibility(View.GONE);

        videoScalingButton =
                (ImageButton) view.findViewById(R.id.button_call_scaling_mode);

        captureFormatText =
                (TextView) view.findViewById(R.id.capture_format_text_call);
        captureFormatSlider =
                (SeekBar) view.findViewById(R.id.capture_format_slider_call);

        captureFormatSlider.setOnSeekBarChangeListener(
                new CaptureQualityController(captureFormatText, videoSettingsController));
    }

    protected abstract void initRemoteView();

    protected void updateVideoView(SurfaceViewRenderer surfaceViewRenderer, boolean mirror) {
        updateVideoView(surfaceViewRenderer, mirror, ScalingType.SCALE_ASPECT_FIT);
    }

    protected void updateVideoView(SurfaceViewRenderer surfaceViewRenderer, boolean mirror, ScalingType scalingType) {
        Log.i(TAG, "updateVideoView mirror:" + mirror + ", scalintType = " + scalingType);
        surfaceViewRenderer.setScalingType(scalingType);
        surfaceViewRenderer.setMirror(mirror);
        surfaceViewRenderer.requestLayout();
    }

    @Override
    public void onResume() {
        super.onResume();

        // If user changed camera state few times and last state was CameraState.ENABLED_FROM_USER // Жень, глянь здесь, смысл в том, что мы здесь включаем камеру, если юзер ее не выключал
        // than we turn on cam, else we nothing change
        if (cameraState != CameraState.DISABLED_FROM_USER
                && isVideoEnabled) {
            toggleCamera(true);
        }
    }

    @Override
    public void onPause() {
        // If camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
        // than we turn off cam
        if (cameraState != CameraState.DISABLED_FROM_USER && isVideoEnabled) {
            toggleCamera(false);
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        sessionController.removeRTCClientConnectionCallback(this);
        sessionController.removeRTCSessionUserCallback(this);
        actionButtonsEnabled(false);
        sessionController.hangUpCurrentSession(" because I'm busy");
        handUpVideoCall.setEnabled(false);
        handUpVideoCall.setActivated(false);
        getActivity().finish();
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession session, final QBRTCVideoTrack videoTrack,
                                          final Integer userID) {
        Log.d(TAG, "onRemoteVideoTrackReceive for opponent= " + userID);
        SurfaceViewRenderer remoteVideoView = getVideoViewForOpponent(userID);
        if (remoteVideoView != null) {
            fillVideoView(remoteVideoView, videoTrack, true);
        }
        Log.e(TAG, "Receive for opponent= " + userID);

    }


    private void initSwitchCameraButton(View view) {
        switchCameraToggle = (ToggleButton) view.findViewById(R.id.switchCameraToggle);
        switchcamerabtn = (LinearLayout) view.findViewById(R.id.switchcamerabtn);
        switchCameraToggle.setVisibility(isVideoEnabled ?
                View.GONE : View.GONE);
        switchcamerabtn.setVisibility(isVideoEnabled ?
                View.VISIBLE : View.GONE);
    }

    private void initButtonsListener() {

        cameraToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cameraState = isChecked ? CameraState.ENABLED_FROM_USER : CameraState.DISABLED_FROM_USER;
                enableCamera(isChecked);
            }
        });

        dynamicToggleVideoCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sessionController.switchAudio();
            }
        });


        micToggleVideoCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableAudio(isChecked);
            }
        });

        handUpVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionButtonsEnabled(false);
                Log.d(TAG, "Call is stopped");

                sessionController.hangUpCurrentSession(" because I'm busy");
                handUpVideoCall.setEnabled(false);
                handUpVideoCall.setActivated(false);

            }
        });

        switchcamerabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QBRTCSession currentSession = sessionController.getCurrentSession();
                if (currentSession == null) {
                    return;
                }
                final QBMediaStreamManager mediaStreamManager = currentSession.getMediaStreamManager();
                if (mediaStreamManager == null) {
                    return;
                }
                mediaStreamManager.switchCameraInput(null);
            }
        });

        switchCameraToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QBRTCSession currentSession = sessionController.getCurrentSession();
                if (currentSession == null) {
                    return;
                }
                final QBMediaStreamManager mediaStreamManager = currentSession.getMediaStreamManager();
                if (mediaStreamManager == null) {
                    return;
                }
                mediaStreamManager.switchCameraInput(null);
            }
        });

        if (videoScalingButton != null) {
            videoScalingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    scalingType = getRandomScalingType();
                    onVideoScalingUpdated(scalingType);
                }
            });
        }

    }

    ScalingType getRandomScalingType() {
        Random random = new Random();
        int nextInt = random.nextInt(3);

        ScalingType[] values = ScalingType.values();
        return values[nextInt];
    }

    protected void onVideoScalingUpdated(ScalingType scalingType) {
        Log.e(TAG, "onVideoScalingUpdated to " + scalingType);
        // Toaster.longToast("View format changed to :"+scalingType);
        updateVideoView(localVideoView, true, scalingType);
    }

    protected void runOnUiThread(Runnable runnable) {
        mainHandler.post(runnable);
    }

    private void toggleCamera(boolean isNeedEnableCam) {
        QBRTCSession currentSession = sessionController.getCurrentSession();
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            if (isNeedEnableCam) {
                currentSession.getMediaStreamManager().startVideoSource();
            } else {
                currentSession.getMediaStreamManager().stopVideoSource();
            }
            myCameraOff.setVisibility(isNeedEnableCam ? View.INVISIBLE : View.VISIBLE);
            switchCameraToggle.setVisibility(isNeedEnableCam ? View.GONE : View.GONE);
        }
    }

    @Override
    public void onWiredHeadsetStateChanged(boolean plugged) {
        dynamicToggleVideoCall.setChecked(plugged);
    }

    private void enableCamera(boolean isNeedEnableCam) {
        QBRTCSession currentSession = sessionController.getCurrentSession();
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            currentSession.getMediaStreamManager().setVideoEnabled(isNeedEnableCam);
            myCameraOff.setVisibility(isNeedEnableCam ? View.INVISIBLE : View.VISIBLE);
            switchCameraToggle.setVisibility(isNeedEnableCam ? View.GONE : View.GONE);
        }
    }

    private void enableAudio(boolean enable) {
        QBRTCSession currentSession = sessionController.getCurrentSession();
        if (currentSession != null) {
            isAudioEnabled = enable;
            currentSession.getMediaStreamManager().setAudioEnabled(enable);
        }
    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, final QBRTCVideoTrack videoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive() run");

        if (localVideoView != null) {
            fillVideoView(localVideoView, videoTrack, !isPeerToPeerCall);
        }
    }


    private void initLocalViewUI(View localView) {
        initSwitchCameraButton(localView);
        myCameraOff = localView.findViewById(R.id.cameraOff);
    }

    private void fillVideoView(SurfaceViewRenderer videoView, QBRTCVideoTrack videoTrack, boolean remoteRenderer) {
        videoTrack.addRenderer(new VideoRenderer(videoView));
        Log.d(TAG, (remoteRenderer ? "remote" : "local") + " Track is rendering");
    }

    private void fillVideoView(SurfaceViewRenderer videoView, QBRTCVideoTrack videoTrack) {
        fillVideoView(videoView, videoTrack, true);
    }

    private void setStatusForOpponent(int userId, final String status) {
        final TextView opponentView = getStatusViewForOpponent(userId);
        if (opponentView == null) {
            return;
        }
        opponentView.setText(status);

    }

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer userId) {
        Log.i(TAG, "onStartConnectToUser" + userId);
        setStatusForOpponent(userId, getString(R.string.checking));

        Log.e(TAG, "checking...");


    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, final Integer userId) {
        Log.i(TAG, "onConnectedToUser" + userId);
        actionButtonsEnabled(true);
        //  setStatusForOpponent(userId, getString(R.string.connected));

        if (getStatusViewForOpponent(userId) != null) {
            getStatusViewForOpponent(userId).setVisibility(View.GONE);
        }
        if (getStatusViewForOpponentName(userId) != null) {
            getStatusViewForOpponentName(userId).setVisibility(View.GONE);
        }

        Log.e(TAG, "Connected...");

    }


    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer userId) {
        Integer status = QBRTCSessionUtils.getStatusDescriptionReosuurce(qbrtcSession.getPeerChannel(userId).getDisconnectReason());
        if (status == null) {
            status = R.string.closed;
        }
        getVideoViewForOpponent(userId).release();
        setStatusForOpponent(userId, getString(status));
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {
        //  setStatusForOpponent(integer, getString(R.string.disconnected));
    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.time_out));
    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.i(TAG, "onConnectionFailedWithUser" + integer);
        setStatusForOpponent(integer, getString(R.string.failed));
    }

    @Override
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {
        Log.i(TAG, "onError" + e.getLocalizedMessage());
    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userId) {
        setStatusForOpponent(userId, getString(R.string.noAnswer));
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userId, String userInfo) {
        setStatusForOpponent(userId, getString(R.string.rejected));
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        setStatusForOpponent(userId, getString(R.string.accepted));
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession session, Integer userId, String userInfo) {
        setStatusForOpponent(userId, getString(R.string.hungUp));
        actionButtonsEnabled(false);
/*        sessionController.hangUpCurrentSession(" because I'm busy");
        handUpVideoCall.setEnabled(false);
        handUpVideoCall.setActivated(false);*/
        getActivity().finish();
    }

    @Override
    public boolean isFragmentAlive() {
        return isAdded();
    }

    private class AudioStreamReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
                Log.d(TAG, "ACTION_HEADSET_PLUG " + intent.getIntExtra("state", -1));
            } else if (intent.getAction().equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
                Log.d(TAG, "ACTION_SCO_AUDIO_STATE_UPDATED " + intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -2));
            }

        }
    }

    private enum CameraState {
        NONE,
        DISABLED_FROM_USER,
        ENABLED_FROM_USER
    }

    class CameraSwitchCallback implements VideoCapturerAndroid.CameraSwitchHandler {
        @Override
        public void onCameraSwitchDone(boolean b) {

        }

        @Override
        public void onCameraSwitchError(String s) {

        }

    }

    //TODO Push for call message
    private void sendPushAboutCall() {
        QBFriendListHelper qbFriendListHelper = new QBFriendListHelper(getActivity());
        for (QBUser qbUser : opponents) {
            if (!qbFriendListHelper.isUserOnline(qbUser.getId())) {
                String callMsg = getString(R.string.dlg_offline_call,
                        AppSession.getSession().getUser().getFullName());
                QBSendPushCommand.start(getActivity(), callMsg, qbUser.getId());
            }
        }
    }

}


