package com.ss.fun2sh.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;
import com.ss.fun2sh.oldutils.Constants;
import com.ss.fun2sh.oldutils.RestMethods;
import com.ss.fun2sh.oldutils.WebserviceCallback;

public class TermsActivity extends AppCompatActivity {

    TextView terms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

         terms=(TextView)findViewById(R.id.terms);

        JSONObject params = null;
        try {
            params = new JSONObject();
            params.put("TYPE", Const.URL.TERMS);

        } catch (JSONException e) {
            M.E(e.getMessage());
        }

        new TermsTask(this,Const.URL.SITE).execute(getParam());
//        new TermsTask(this,Const.URL.SITE).execute(params);
//        new TermsTask(this,Const.URL.SITE_CONTENT).execute(params);

    }

    public JSONObject getParam() {
        JSONObject params = null;
        try {
            params = new JSONObject();
            params.put("TYPE", Const.URL.TERMS);

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

//            M.T(TermsActivity.this,"preexecute");
            showProgressDialog();
        }

        @Override
        protected String doInBackground(JSONObject... params) {
            JSONObject jsonParam = params[0];
//        M.T(con, "DoInBackgroiund");
//            M.T(TermsActivity.this,"do in background");
            return RestMethods.WSPost(jsonParam, postMethod);
        }

        @Override
        protected void onPostExecute(String postResult) {
            super.onPostExecute(postResult);
            dismissProgressDialog();
//            M.T(TermsActivity.this,"post");
//        M.T(TermsActivity.this, "result***"+postResult);
            if (postResult != null) {
//            callback.postResult(postResult,postMethod);
                try {
                    JSONArray array=new JSONArray(postResult);
                    JSONObject obj=array.getJSONObject(0);
                    String st=obj.getString("CMDESCR");
                    terms.setText(Html.fromHtml(st));

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
