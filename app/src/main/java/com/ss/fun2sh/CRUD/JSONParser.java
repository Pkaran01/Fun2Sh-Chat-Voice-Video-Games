package com.ss.fun2sh.CRUD;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.ss.fun2sh.AppController;
import com.ss.fun2sh.encrypt.Encrypt;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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

    public void parseVollyJSONObjectQB(String URL, int method, JSONObject json, final Helper h) {
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
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("QB-Token", CoreSharedHelper.getInstance().getQBToken());
                    params.put("Content-Type", "application/json");

                    return params;
                }
            };
            objectRequest.setRetryPolicy(new DefaultRetryPolicy(90000, 1, 1f));
            AppController.getInstance().addToRequestQueue(objectRequest);
        }
    }
}
