package com.ss.fun2sh.CRUD;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import com.ss.fun2sh.Activity.FragHelp;
import com.ss.fun2sh.AppController;
import com.ss.fun2sh.encrypt.Encrypt;

public class JSONParser {

    private Context cx;

    // constructor
    public JSONParser(Context cx) {
        this.cx = cx;
    }


    public void parseVollyJSONObject(String URL, int method, JSONObject json, final Helper h) {

        JSONObject jsonobj = Encrypt.encryptJsonObjectResponse(json.toString());

        if (NetworkUtil.getConnectivityStatus(cx)) {
            final MaterialDialog materialDialog = M.initProgressDialog(cx);
            JsonObjectRequest objectRequest = new JsonObjectRequest(method, URL, jsonobj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // TODO Auto-generated method stub
                    materialDialog.dismiss();
                    M.E("result in parser" + response.toString());
                    h.backResponse(response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub
                    materialDialog.dismiss();
                    M.E("Error: " + error.getMessage());
                }
            });
            objectRequest.setRetryPolicy(new DefaultRetryPolicy(90000, 1, 1f));
            AppController.getInstance().addToRequestQueue(objectRequest);
        } else {
            M.dError(cx, "Unable to connect internet.");
        }
    }

    public void parseVollyJSONObjectL(String URL, int method, JSONObject json, final Helper h) {
        if (NetworkUtil.getConnectivityStatus(cx)) {
            JsonObjectRequest objectRequest = new JsonObjectRequest(method, URL, json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // TODO Auto-generated method stub
                    h.backResponse(response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub
                    M.E("Error: " + error.getMessage());
                }
            });
            objectRequest.setRetryPolicy(new DefaultRetryPolicy(90000, 1, 1f));
            AppController.getInstance().addToRequestQueue(objectRequest);
        }
    }

    //pars mati par via asyctask
    public void parseMutipartViaAsyncTask(String url, final Helper h) {
        if (NetworkUtil.getConnectivityStatus(cx)) {
            UploadData task = new UploadData(url, h);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                task.execute();
        } else {
            M.T(cx, "Oop's unable to connect internet..!");
        }
    }

    public class UploadData extends AsyncTask<String, Integer, String> {
        private String url;
        ProgressDialog progressDialog;
        Helper h;

        UploadData(String url, Helper h) {
            this.url = url;
            this.h = h;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressDialog = new ProgressDialog(cx);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(100);
            progressDialog.setProgress(0);
            progressDialog.show();
            progressDialog.setCancelable(true);

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setProgress(progress[0]);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            return FragHelp.fragHelp.uploadIt(url, this);
        }

        public void doProgress(int value) {
            publishProgress(value);
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            if (result != null && result.length() > 0) {
                //here your logic to parse response
                progressDialog.hide();
                h.backResponse(result);
            } else {
                progressDialog.hide();
                M.T(cx, "Invalid Response");
            }

        }

    }
}
