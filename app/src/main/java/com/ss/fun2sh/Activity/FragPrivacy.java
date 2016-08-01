package com.ss.fun2sh.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;
import com.ss.fun2sh.oldutils.Constants;
import com.ss.fun2sh.oldutils.RestMethods;
import com.ss.fun2sh.oldutils.WebserviceCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by CRUD Technology on 5/27/2016.
 */
public class FragPrivacy extends BaseFragment {

    JSONObject jobj;
    Context con;
    TextView about_content;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_privacy, container, false);
        super.onCreate(savedInstanceState);
        con=getActivity();
        about_content=(TextView)v.findViewById(R.id.policy_content);
        try
        {
            jobj=new JSONObject();
            jobj.put("TYPE", Const.URL.POLICIES);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new TermsTask(con,Const.URL.SITE).execute(jobj);
        return v;
    }

    public JSONObject getParam() {
        JSONObject params = null;
        try {
            params = new JSONObject();
            params.put("TYPE", Const.URL.POLICIES);

        } catch (JSONException e) {
            M.E(e.getMessage());
        }
        return params;
    }

    class TermsTask extends AsyncTask<JSONObject, Void, String> {

        private Context context;
        private String postMethod;
        private WebserviceCallback callback;
        private ProgressDialog progDialog;


        public TermsTask(Context context, String postMethod) {
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
//        M.T(con, "DoInBackgroiund");
            return RestMethods.WSPost(jsonParam, postMethod);

        }

        @Override
        protected void onPostExecute(String postResult) {
            super.onPostExecute(postResult);
            dismissProgressDialog();

//        M.T(con, "PostExecute");
            if (postResult != null) {
//            callback.postResult(postResult,postMethod);
                try {
                    JSONArray array=new JSONArray(postResult);
                    JSONObject obj=array.getJSONObject(0);
                    String st=obj.getString("CMDESCR");
                    about_content.setText(Html.fromHtml(st));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(context, Constants.TIME_OUT, Toast.LENGTH_LONG).show();
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
}


