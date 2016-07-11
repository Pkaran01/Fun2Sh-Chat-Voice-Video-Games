package com.ss.fun2sh.utils.listeners;

public interface FriendOperationListener {

    void onAcceptUserClicked(int position, int userId);

    void onRejectUserClicked(int position, int userId);
}