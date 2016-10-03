package com.ss.fun2sh.ui.activities.groupcall.utils;

import android.util.SparseArray;

import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.ss.fun2sh.R;


public class QBRTCSessionUtils {

    private static final SparseArray<Integer> peerStateDescriptions = new SparseArray<>();
    static {
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_HANG_UP.ordinal(), R.string.hungUp);
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_REJECT.ordinal(), R.string.rejected);
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_NOT_ANSWER.ordinal(), R.string.noAnswer);
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_NOT_OFFER.ordinal(), R.string.noAnswer);
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_DISCONNECT_TIMEOUT.ordinal(), R.string.disconnected);
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CONNECTING.ordinal(), R.string.connect);
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_PENDING.ordinal(), R.string.pending);
    }

    public static Integer getStatusDescriptionReosuurce(QBRTCTypes.QBRTCConnectionState connectionState){
        return peerStateDescriptions.get(connectionState.ordinal());
    }
}
