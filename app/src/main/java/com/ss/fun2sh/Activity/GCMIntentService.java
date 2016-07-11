package com.ss.fun2sh.Activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCM MILifestyle::Service";

	
	// Oriens  SENDER_ID
    public static final String SENDER_ID = "980224455542";


	public GCMIntentService() {
		super(SENDER_ID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {

		Log.i("Messageid","Registered");
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {

		Log.i("Messageid","Registered");
	}

	@Override
	protected void onMessage(Context context, Intent data) {
		
		String message = data.getStringExtra("message");

		if (message != null) {
			
			if (message != null) {
				Intent intent2 = new Intent(context, GCMNotifService.class);
				intent2.putExtra("message", message);
				context.startService(intent2);
			}
			
//			try {
//				JSONObject jsonObject = new JSONObject(message);
//				String msg = jsonObject.getString("MSG");
//				String sub = jsonObject.getString("SUB");
//			    NotificationGenerate.generateNotification(context, msg,sub);
//				
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
		}
	}

	@Override
	protected void onError(Context arg0, String errorId) {

//		Log.e(TAG, "onError: errorId=" + errorId);
	}
	
}
