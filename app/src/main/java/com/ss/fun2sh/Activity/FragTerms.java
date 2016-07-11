package com.ss.fun2sh.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class FragTerms extends Fragment {

    TextView txt_terms_content,registerd;
    Context con;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_terms, container, false);
        super.onCreate(savedInstanceState);

        con=getActivity();

        txt_terms_content = (TextView) v.findViewById(R.id.txt_terms_content);
        registerd = (TextView) v.findViewById(R.id.registerd);
//

        new TermsTask(con,Const.URL.SITE).execute(getParam());
//        JSONObject jsonobj=new JSONObject();
//        new JSONParser(getActivity()).parseVollyJSONObject(Const.URL.TERMS, 1, jsonobj, new Helper() {
//            @Override
//            public void backResponse(String response) {
//
//            }
//        });
//        Log.e("zzz","calling Webservicwe");
//        new JSONParser(getActivity()).parseVollyJSONObject(Const.URL.SITE_CONTENT, 1, getParam(), new Helper() {
//            @Override
//            public void backResponse(String response) {

//                Toast.makeText(getActivity(),"result",Toast.LENGTH_LONG).show();
//                Log.e("result",response);


//                M.T(getActivity(),"result");
//                txt_terms_content.setText("output"+response);
//                registerd.setText("output"+response);
//                try {
//                    JSONObject data = new JSONObject(response);
//                    if (data.getString("MSG").equals("SUCCESS")) {
//                        M.T(getActivity(), data.getString("MESSAGE"));
////                        load_home();
//                    } else {
//                        M.dError(getActivity(), data.getString("MESSAGE"));
//                    }
//                } catch (JSONException e) {
//                    M.E(e.getMessage());
//                }
//            }
//        });


        //zzz

        return v;
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
//    private void callWebservice(JSONObject jsonObject, String postMethod) {
//
//        if (ConnectionDetector.isConnected(getActivity())) {
////            PostTask postTask = new PostTask(getActivity(), postMethod, getActivity());
//            postTask.execute(jsonObject);
//        } else {
//            M.T(getActivity(),Constants.NO_INTERNET);
//        }
//    }



//    public void load_home() {
//        FragHome home = new FragHome();
//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//        FragmentTransaction ft = fragmentManager.beginTransaction();
//        ft.replace(R.id.frame_container, home);
//        ft.commit();
//    }



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
                txt_terms_content.setText(Html.fromHtml(st));

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
