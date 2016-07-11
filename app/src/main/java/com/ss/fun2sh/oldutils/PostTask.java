package com.ss.fun2sh.oldutils;

import org.json.JSONObject;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class PostTask extends AsyncTask<JSONObject, Void, String> {

	private Context context;
	private String postMethod;
	private WebserviceCallback callback;
	private ProgressDialog progDialog;

	
	public PostTask(Context context, String postMethod,
			WebserviceCallback callback)
	 {
		this.context = context;
		this.postMethod = postMethod;
		this.callback = callback;
		
	}
	
	private void showProgressDialog() {
		progDialog = new ProgressDialog(context);
//		progDialog.setMessage("Loading...");
		progDialog.setMessage("Loading Please Wait...");
		
		progDialog.setIndeterminate(true);
		progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDialog.setCancelable(false);
		progDialog.show();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		showProgressDialog();
	}

	@Override
	protected String doInBackground(JSONObject... params) {
		JSONObject jsonParam = params[0];
		return RestMethods.WSPost(jsonParam, postMethod);
		
	}

	@Override
	protected void onPostExecute(String postResult) {
		super.onPostExecute(postResult);
		dismissProgressDialog();
		
		if (postResult != null) {
			callback.postResult(postResult,postMethod);
		} else {
			Toast.makeText(context, Constants.TIME_OUT, Toast.LENGTH_LONG)
					.show();
		}
	}
	
	private void dismissProgressDialog() {
		if ((progDialog != null) && progDialog.isShowing()) {
			try {
				progDialog.dismiss();
				progDialog = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
