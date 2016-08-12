package com.ss.fun2sh.Activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.HexagonImageView;
import com.google.android.gcm.GCMRegistrar;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.Helper;
import com.ss.fun2sh.CRUD.JSONParser;
import com.ss.fun2sh.CRUD.M;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.ss.fun2sh.CRUD.Utility;
import com.ss.fun2sh.R;
import com.ss.fun2sh.utils.image.ImageLoaderUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class SecondTimeLoginActivity extends AppCompatActivity {

    private android.widget.TextView userName;
    private android.widget.ImageView existingAccount;
    private android.widget.TextView signInDifferentAccount;
    private String GCMRegId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secon_time_login);
        this.signInDifferentAccount = (TextView) findViewById(R.id.signInDifferentAccount);
        this.existingAccount = (ImageView) findViewById(R.id.existingAccount);
        this.userName = (TextView) findViewById(R.id.userName);
        userName.setText("Sign in as " + PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
        final HexagonImageView imageView=(HexagonImageView) findViewById(R.id.userImage);
        ImageLoader.getInstance().loadImage(PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.LAST_AVATAR_URL,""), ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedBitmap) {
                        imageView.setImageBitmap(loadedBitmap);
                    }
                });
        signInDifferentAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefsHelper.getPrefsHelper().delete(Const.App_Ver.isFirstTime);
                M.I(SecondTimeLoginActivity.this, LoginActivity.class, null);
                SecondTimeLoginActivity.this.finish();

            }
        });
        existingAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JSONParser(SecondTimeLoginActivity.this).parseVollyJSONObject(Const.URL.login, 1, getParam(), new Helper() {
                    @Override
                    public void backResponse(String response) {
                        try {
                            JSONObject data = new JSONObject(response);
//                                Log.e("responses",response.toString());
                            if (data.getString("MSG").equals("SUCCESS")) {
                                PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.userId, PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
                                PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.pwd, PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd));
                                PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.isFirstTime, true);
                                PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.isFirstTimeLogin, true);
                                PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.userData, data.toString());
//                                    PrefsHelper.getPrefsHelper().savePref("REGID", data.get("REGID").toString());
                                PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.reg_id, data.get(Const.App_Ver.reg_id).toString());

                                PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.reg_type, data.get(Const.App_Ver.reg_type).toString());
                                M.I(SecondTimeLoginActivity.this, DashBoardActivity.class, null);

                                SecondTimeLoginActivity.this.finish();
                            } else {
                                M.dError(SecondTimeLoginActivity.this, data.getString("MESSAGE"));
                            }
                        } catch (JSONException e) {
                            M.E(e.getMessage());
                        }
                    }
                });
            }
        });
    }

    public JSONObject getParam() {
        GCMRegId = GCMRegistrar.getRegistrationId(this);
        JSONObject params = null;
        try {
            TelephonyManager tMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            params = new JSONObject();
            params.put("IDNO", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
            params.put("PWD", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd));
            String mobileInfro = "MOBILENO:" + ((tMgr.getLine1Number() != null) ? tMgr.getLine1Number() : "N/A") + ";SIMSERIAL:" + tMgr.getSimSerialNumber() + ";IMEI: " + Utility.getIMEINO(SecondTimeLoginActivity.this) + ";SUBID: " + tMgr.getSubscriberId() + ";WLANMAC:" + ((wifi.isWifiEnabled()) ? Utility.getMACAddress("wlan0") : Utility.getMACAddress("eth0")) + ";ANDROIDID:" + Utility.getDeviceId(SecondTimeLoginActivity.this) + ";VERSION:" + Utility.getAppVersion(SecondTimeLoginActivity.this) + "";
            params.put("MOBINFO", mobileInfro);
//            params.put("GCMID", "APA91bHqDoWD1_QajEZkWFmCAMVnBcwAVaSX4b_foro9XFOjy_YlhJDdqC7tM8SAZpsDCKOueygO3dXG4AVKUOE_Xv8rYYa72ZmyNM_3scX33Pfu4Iv3JfU");
            params.put("GCMID", GCMRegId);
            params.put("IMEI", Utility.getIMEINO(SecondTimeLoginActivity.this));
            params.put("WLANMAC", wifi.isWifiEnabled() ? Utility.getMACAddress("wlan0") : Utility.getMACAddress("eth0"));
            params.put("ANDROIDID", Utility.getDeviceId(SecondTimeLoginActivity.this));
            params.put("CHK", "1");
        } catch (JSONException e) {
            M.E(e.getMessage());
        }
        return params;

    }
}
