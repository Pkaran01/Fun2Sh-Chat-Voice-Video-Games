package com.ss.fun2sh.utils.listeners;

import android.os.Bundle;

public interface GlobalActionsListener {

    void onReceiveChatMessageAction(Bundle extras);

    void onReceiveForceReloginAction(Bundle extras);

    void onReceiveRefreshSessionAction(Bundle extras);

    void onReceiveContactRequestAction(Bundle extras);
}