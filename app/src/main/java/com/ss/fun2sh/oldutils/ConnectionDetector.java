package com.ss.fun2sh.oldutils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector {

	public static NetworkInfo getNetworkInfo(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivityManager.getActiveNetworkInfo();

	}

	public static boolean isConnected(Context context) {
		NetworkInfo networkInfo = getNetworkInfo(context);
		return (networkInfo != null && networkInfo.isConnected());
	}

}
