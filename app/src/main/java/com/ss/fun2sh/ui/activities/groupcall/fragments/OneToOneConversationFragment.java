package com.ss.fun2sh.ui.activities.groupcall.fragments;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.ss.fun2sh.R;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

public class OneToOneConversationFragment extends ConversationFragment {

    SurfaceViewRenderer opponentViewRenderer;
    private TextView connectionStatus;
    RelativeLayout innerLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return onCreateView(inflater, container, savedInstanceState, R.layout.group_one_to_one_conversation_fragment);
    }

    @Override
    protected void initCustomView(View view) {
        opponentViewRenderer = (SurfaceViewRenderer) view.findViewById(R.id.opponentView);
        innerLayout = (RelativeLayout) view.findViewById(R.id.innerLayout);
        connectionStatus = (TextView) view.findViewById(R.id.connectionStatus);
    }

    @Override
    protected TextView getStatusViewForOpponent(int userId) {
        return connectionStatus;
    }

    @Override
    protected TextView getStatusViewForOpponentName(int userId) {
        return null;
    }

    @Override
    protected SurfaceViewRenderer getVideoViewForOpponent(Integer userID) {
        return opponentViewRenderer;
    }


    @Override
    protected RelativeLayout getInnerRelative() {
        return innerLayout;
    }

    @Override
    protected void initRemoteView() {
        SurfaceViewRenderer videoViewForOpponent = getVideoViewForOpponent(sessionController.getCurrentSession().getOpponents().get(0));
        updateVideoView(videoViewForOpponent, false);
    }

    @Override
    protected void onVideoScalingUpdated(RendererCommon.ScalingType scalingType) {
        super.onVideoScalingUpdated(scalingType);
        updateVideoView(getVideoViewForOpponent(sessionController.getCurrentSession().getOpponents().get(0)), false, scalingType);
    }
}
