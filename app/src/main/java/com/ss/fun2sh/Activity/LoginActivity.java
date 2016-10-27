package com.ss.fun2sh.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gcm.GCMRegistrar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.Helper;
import com.ss.fun2sh.CRUD.JSONParser;
import com.ss.fun2sh.CRUD.M;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.ss.fun2sh.CRUD.UserAccount;
import com.ss.fun2sh.CRUD.Utility;
import com.ss.fun2sh.R;
import com.ss.fun2sh.encrypt.Encrypt;
import com.ss.fun2sh.oldutils.ConnectionDetector;
import com.ss.fun2sh.oldutils.Constants;
import com.ss.fun2sh.oldutils.PostTask;
import com.ss.fun2sh.oldutils.WebserviceCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class LoginActivity extends AppCompatActivity implements WebserviceCallback {

    private android.widget.VideoView videoView;
    private android.widget.TextView signIn;
    private android.widget.EditText userName;
    private android.widget.EditText password;
    private android.widget.Button loginButton;
    private android.widget.TextView forgetPassword;
    private String GCMRegId = null;
    String regType;
    boolean isFirstLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.forgetPassword = (TextView) findViewById(R.id.forgetPassword);
        this.loginButton = (Button) findViewById(R.id.loginButton);
        this.password = (EditText) findViewById(R.id.password);
        this.userName = (EditText) findViewById(R.id.userName);
        this.signIn = (TextView) findViewById(R.id.signIn);
        this.videoView = (VideoView) findViewById(R.id.videoView);
        Utility.loginVideo(videoView, this);
        password.setTypeface(Typeface.DEFAULT);
        password.setTransformationMethod(new PasswordTransformationMethod());

//        userName.setText("a4abim");
//        password.setText("1234");

        //for marsmallow
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // only for gingerbread and newer versions
            if (!Dexter.isRequestOngoing()) {
                Dexter.checkPermissions(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        for (PermissionGrantedResponse response : report.getGrantedPermissionResponses()) {
                            showPermissionGranted(response.getPermissionName());
                        }

                        for (PermissionDeniedResponse response : report.getDeniedPermissionResponses()) {
                            showPermissionDenied(response.getPermissionName(), response.isPermanentlyDenied());
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        showPermissionRationale(token);
                    }
                }, Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO);
            }
        } else {
            elsePart();
        }
    }

    public void showPermissionRationale(final PermissionToken token) {
        new AlertDialog.Builder(this).setTitle(R.string.permission_rationale_title)
                .setMessage(R.string.permission_rationale_message)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.cancelPermissionRequest();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.continuePermissionRequest();
                    }
                })
                .show();
    }

    private void elsePart() {
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        GCMRegId = GCMRegistrar.getRegistrationId(this);
        if (GCMRegId.equals("")) {
            GCMRegistrar.register(this, GCMIntentService.SENDER_ID);
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserAccount.isEmpty(userName, password)) {
                    if (getParam() != null) {
                        M.E(getParam().toString());
                        android.util.Log.e("getParam-**"," "+getParam().toString());
                        new JSONParser(LoginActivity.this).parseVollyJSONObject(Const.URL.login, 1, getParam(), new Helper() {
                            @Override
                            public void backResponse(String response) {
                                try {
                                    JSONObject data = new JSONObject(response);
                                    android.util.Log.e("responselogin-**"," "+response.toString());
//                                Log.e("responses",response.toString());
                                    if (data.getString("MSG").equals("SUCCESS")) {
                                        PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.userId, userName.getText().toString());
                                        PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.pwd, password.getText().toString());
                                        PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.isFirstTime, true);
                                        PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.isFirstTimeLogin, true);
                                        PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.userData, data.toString());
//                                    PrefsHelper.getPrefsHelper().savePref("REGID", data.get("REGID").toString());
                                        PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.reg_id, data.get(Const.App_Ver.reg_id).toString());

                                        regType = data.get("REGTYPE").toString();
                                        PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.reg_type, data.get(Const.App_Ver.reg_type).toString());
                                        if (PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.expire_date, null) == null) {
                                            PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.expire_date, Utility.getAfter15DayDate());
                                        }
                                        M.I(LoginActivity.this, DashBoardActivity.class, null);

                                        LoginActivity.this.finish();
                                    } else {
                                        M.dError(LoginActivity.this, data.getString("MESSAGE"));
                                    }
                                } catch (JSONException e) {
                                    M.E(e.getMessage());
                                }
                            }
                        });
                    } else {
                        UserAccount.EditTextPointer.requestFocus();
                        UserAccount.EditTextPointer.setError("This can't be empty !");

                    }
                } else {
                    M.dError(LoginActivity.this, "Permissions denied");
                }
            }
        });
    }

    public void showPermissionGranted(String permission) {
        elsePart();
    }

    public void showPermissionDenied(String permission, boolean isPermanentlyDenied) {
        M.dError(this, "Permission is denied");
    }

    public JSONObject getParam() {
        GCMRegId = GCMRegistrar.getRegistrationId(this);
        JSONObject params = null;
        try {
            TelephonyManager tMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            params = new JSONObject();
            params.put("IDNO", userName.getText());
            params.put("PWD", password.getText());
            String mobileInfro = "MOBILENO:" + ((tMgr.getLine1Number() != null) ? tMgr.getLine1Number() : "N/A") + ";SIMSERIAL:" + tMgr.getSimSerialNumber() + ";IMEI: " + Utility.getIMEINO(LoginActivity.this) + ";SUBID: " + tMgr.getSubscriberId() + ";WLANMAC:" + ((wifi.isWifiEnabled()) ? Utility.getMACAddress("wlan0") : Utility.getMACAddress("eth0")) + ";ANDROIDID:" + Utility.getDeviceId(LoginActivity.this) + ";VERSION:" + Utility.getAppVersion(LoginActivity.this) + "";
            params.put("MOBINFO", mobileInfro);
//            params.put("GCMID", "APA91bHqDoWD1_QajEZkWFmCAMVnBcwAVaSX4b_foro9XFOjy_YlhJDdqC7tM8SAZpsDCKOueygO3dXG4AVKUOE_Xv8rYYa72ZmyNM_3scX33Pfu4Iv3JfU");
            params.put("GCMID", GCMRegId);
            params.put("IMEI", Utility.getIMEINO(LoginActivity.this));
            params.put("WLANMAC", wifi.isWifiEnabled() ? Utility.getMACAddress("wlan0") : Utility.getMACAddress("eth0"));
            params.put("ANDROIDID", Utility.getDeviceId(LoginActivity.this));
            params.put("CHK", "0");
        } catch (JSONException e) {
            M.E(e.getMessage());
        }
        return params;

    }

    public JSONObject getEncryptParam() {
        JSONObject parame = null;

        JSONObject obj = getParam();
        parame = Encrypt.encryptJsonObjectResponse(obj.toString());
        return parame;

    }

    public void createAccount(View view) {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("IDNO", "");
            jsonObject.put("PWD", "");
//            callWebservice(jsonObject, Constants.COUNTRY_POSTMTD);

            if (ConnectionDetector.isConnected(this)) {
                PostTask postTask = new PostTask(LoginActivity.this, Constants.COUNTRY_POSTMTD, LoginActivity.this);
                postTask.execute(jsonObject);
            } else {
//                showToastMsg(Constants.NO_INTERNET);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        M.I(LoginActivity.this, SignUpActivity.class, null);
    }

    public void forgetPassword() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_forgetpassword, null);
        Button loginButton = (Button) view.findViewById(R.id.loginButton);
        ImageView close = (ImageView) view.findViewById(R.id.close);
        final EditText emailEditText = (EditText) view.findViewById(R.id.emailEditText);
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .autoDismiss(false)
                .customView(view, false)
                .show();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserAccount.isEmpty(emailEditText)) {
                    JSONObject param = new JSONObject();
                    try {
                        param.put("EMAILID", emailEditText.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    new JSONParser(LoginActivity.this).parseVollyJSONObject(Const.URL.forgotPwd, 1, param, new Helper() {
                        @Override
                        public void backResponse(String response) {
                            try {
                                JSONObject data = new JSONObject(response);
                                if (data.getString("MSG").equals("SUCCESS")) {
//                                    M.dSuccess(LoginActivity.this, getString(R.string.app_name), data.getString("MESSAGE"));
//                                    M.dSuccess(LoginActivity.this, "", data.getString("MESSAGE"));
//                                    M.dSuccess(LoginActivity.this, "", "Your Login credentials are sent to the email you entered as your registered email with Fun2sh");
                                    M.dSuccess(LoginActivity.this, "Fun2sh", "Your Login credentials are sent to the email you entered as your registered email with Fun2sh");
                                    dialog.dismiss();
                                } else {
//                                    M.dError(LoginActivity.this, data.getString("MESSAGE"));
                                    M.dError(LoginActivity.this, data.getString("MESSAGE"));
//                                    M.dSuccess(LoginActivity.this, "Your ","");
//                                    M.dSimple(LoginActivity.this,"Failuresss");
                                    dialog.dismiss();
                                }
                            } catch (JSONException e) {
                                M.E(e.getMessage());
                            }
                        }
                    });
                } else {
                    UserAccount.EditTextPointer.requestFocus();
                    UserAccount.EditTextPointer.setError("This can't be empty !");
                }
            }
        });
    }

    public void forgetPassword(View view) {
        forgetPassword();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.videoView.start();
    }

    @Override
    public void postResult(String postResult, String postMethod) {

        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(postResult);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jsonArray.length() > 0) {
            Intent intent = new Intent(this, SignUpActivity.class);
            intent.putExtra("postResult", postResult);
            startActivity(intent);
        } else {
//            showToastMsg(Constants.RECORDS_NOT_FOUND);
            M.T(LoginActivity.this, Constants.RECORDS_NOT_FOUND);
        }

    }
}
