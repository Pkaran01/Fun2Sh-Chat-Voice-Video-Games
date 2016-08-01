package com.ss.fun2sh.Activity;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.Helper;
import com.ss.fun2sh.CRUD.JSONParser;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;
import com.ss.fun2sh.oldutils.Constants;
import com.ss.fun2sh.oldutils.RestMethods;
import com.ss.fun2sh.oldutils.WebserviceCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class FragProfile extends BaseFragment {
    Button updateProfile, upgradePackage;
    private android.widget.TextView proEmailid;
    private android.widget.TextView proPassword;
    private android.widget.TextView proFullName;
    private android.widget.TextView proCountry;
    private android.widget.TextView proState;
    private android.widget.TextView proCity;
    private android.widget.TextView proAddress;
    private android.widget.TextView proZipCode;
    private TextView pkgRegType;
    private TextView pkgDateofreg;
    private TextView pkgDateofstandard;
    private TextView pkgDateofPrimum;
    String profileDetails, packageDetails, reg_type;
    JSONObject jsonObject;
    JSONObject packageDetail;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_profile, container, false);
        this.pkgDateofPrimum = (TextView) v.findViewById(R.id.pkgDateofPrimum);
        this.pkgDateofstandard = (TextView) v.findViewById(R.id.pkgDateofstandard);
        this.pkgDateofreg = (TextView) v.findViewById(R.id.pkgDateofreg);
        this.pkgRegType = (TextView) v.findViewById(R.id.pkgRegType);
        this.proZipCode = (TextView) v.findViewById(R.id.proZipCode);
        this.proAddress = (TextView) v.findViewById(R.id.proAddress);
        this.proCity = (TextView) v.findViewById(R.id.proCity);
        this.proState = (TextView) v.findViewById(R.id.proState);
        this.proCountry = (TextView) v.findViewById(R.id.proCountry);
        this.proFullName = (TextView) v.findViewById(R.id.proFullName);
        this.proPassword = (TextView) v.findViewById(R.id.proPassword);
        this.proEmailid = (TextView) v.findViewById(R.id.proEmailid);
        super.onCreate(savedInstanceState);
        updateProfile = (Button) v.findViewById(R.id.updateProfile);
        upgradePackage = (Button) v.findViewById(R.id.packageUpgrade);

        try {
            jsonObject = new JSONObject();
            jsonObject.put("IDNO", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
            jsonObject.put("PWD", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd));
//        callWebservice(jsonObject, Constants.PACKAGEDETAILS_POSTMTD);
        } catch (JSONException e) {
            e.printStackTrace();
        }


//        new DetailsTask(getActivity(), Constants.PROFILE_DETAILS).execute(jsonObject);

        //single webservice

        new JSONParser(getActivity()).parseVollyJSONObject(Const.URL.PROFILE_DETAILS, 1, jsonObject, new Helper() {
            @Override
            public void backResponse(String response) {
//                M.E("Package Details Detail :" + response);
                if(response!=null) {
                    try {

                        Log.d("response", response);
                        packageDetails = response;
                         packageDetail = new JSONObject(response);
                        if (packageDetail.getString("MSG").equals("SUCCESS")) {
                            Log.d("response", "success");
//                        M.T(getActivity(),""+packageDetail.getString("REGTYPE"));
//                            Log.d("response", packageDetail.getString("REGEMAIL"));
//                            Log.d("response", packageDetail.getString("COUNTRY"));
//                            Log.d("response", packageDetail.getString("STATE"));
//                            Log.d("response", packageDetail.getString("CITY"));
//                            Log.d("response", packageDetail.getString("ADD"));
//                            Log.d("response", packageDetail.getString("PINCODE"));
//                            Log.d("response", packageDetail.getString("STATUS"));
//
//                            Log.d("response", packageDetail.getString("REGID"));
//                            Log.d("response", packageDetail.getString("REGTYPE"));
//                            Log.d("response", packageDetail.getString("REGDATE"));
//                            Log.d("response", packageDetail.getString("STANDRDATE"));
//                            Log.d("response", packageDetail.getString("PREMIUMDATE"));

                            Log.d("response", packageDetail.getString("STATE"));
                            Log.d("response", packageDetail.getString("REGDATE"));
                            Log.d("response", packageDetail.getString("PREMIUMDATE"));
                            Log.d("response", packageDetail.getString("COUNTRY"));
                            Log.d("response", packageDetail.getString("STATUS"));
                            Log.d("response", packageDetail.getString("REGID"));
                            Log.d("response", packageDetail.getString("REGTYPE"));

                            Log.d("response", packageDetail.getString("FULLNAME"));
                            Log.d("response", packageDetail.getString("PINCODE"));
                            Log.d("response", packageDetail.getString("REGEMAIL"));
                            Log.d("response", packageDetail.getString("CITY"));
                            Log.d("response", packageDetail.getString("STANDRDATE"));
                            Log.d("response", packageDetail.getString("ADD"));

                            proEmailid.setText(packageDetail.getString("REGEMAIL"));
//                            proFullName.setText(packageDetail.getString("FNAME") + " " + packageDetail.getString("LNAME") + " " + packageDetail.getString("MNAME"));
                            proFullName.setText(packageDetail.getString("FULLNAME"));
                            proCountry.setText(packageDetail.getString("COUNTRY"));
                            proState.setText(packageDetail.getString("STATE"));
                            proCity.setText(packageDetail.getString("CITY"));
                            proAddress.setText(packageDetail.getString("ADD"));
                            proZipCode.setText(packageDetail.getString("PINCODE"));

//                          pkgRegType.setText(packageDetail.getString("REGTYPE"));
                            pkgDateofreg.setText(packageDetail.getString("REGDATE"));
                            pkgDateofstandard.setText(packageDetail.getString("STANDRDATE"));
                            pkgDateofPrimum.setText(packageDetail.getString("PREMIUMDATE"));

                            reg_type = packageDetail.getString("REGTYPE");
                            if (reg_type.contains("PREMIUM")) {
//                                            M.T(getActivity(),"PREMIUM");
                                upgradePackage.setVisibility(View.GONE);
                            }
                            else
                            {
                                upgradePackage.setVisibility(View.VISIBLE);
                            }

                        } else {
                        M.T(getActivity(), packageDetail.getString("MESSAGE"));
                            load_home();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Log.d("response","empty");
                }

            }
        });

        //two webservices

//        new JSONParser(getActivity()).parseVollyJSONObject(Const.URL.editPerInfo, 1, Utility.getParam(), new Helper() {
//            @Override
//            public void backResponse(final String profileresponse) {
//                M.E("Profile Detail :" + profileresponse);
//                try {
//                    profileDetails = profileresponse;
//                    Log.e("profileresponse", profileresponse.toString());
//                    final JSONObject data = new JSONObject(profileresponse);
//                    if (data.getString("MSG").equals("SUCCESS")) {
//                        new JSONParser(getActivity()).parseVollyJSONObject(Const.URL.packageDetails, 1, Utility.getParam(), new Helper() {
//                            @Override
//                            public void backResponse(String response) {
//                                M.E("Package Details Detail :" + response);
//                                try {
//                                    packageDetails = response;
//                                    JSONObject packageDetail = new JSONObject(response);
//                                    Log.e("packageDetails", response.toString());
//                                    if (packageDetail.getString("MSG").equals("SUCCESS")) {
////                                        pkgRegType.setText(packageDetail.getString("REGTYPE"));
//                                        pkgDateofreg.setText(packageDetail.getString("REGDATE"));
//                                        pkgDateofstandard.setText(packageDetail.getString("STANDRDATE"));
//                                        pkgDateofPrimum.setText(packageDetail.getString("PREMIUMDATE"));
//                                        proEmailid.setText(data.getString("EMAIL"));
//                                        proFullName.setText(data.getString("FNAME") + " " + data.getString("LNAME") + " " + data.getString("MNAME"));
////                                        proFullName.setText(""+PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
//                                        proState.setText(data.getString("STATE"));
//                                        proZipCode.setText(data.getString("ZIP"));
//                                        proCountry.setText(data.getString("COUNTRY"));
//                                        proCity.setText(data.getString("CITY"));
//                                        proAddress.setText(data.getString("ADDRESS"));
//
//                                        reg_type = packageDetail.getString("REGTYPE");
//                                        if (reg_type.contains("PREMIUM")) {
////                                            M.T(getActivity(),"PREMIUM");
//                                            upgradePackage.setVisibility(View.GONE);
//                                        }
//
//                                    } else {
//                                        M.T(getActivity(), packageDetail.getString("MESSAGE"));
//                                        load_home();
//                                    }
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//                    } else {
//                        M.T(getActivity(), data.getString("MESSAGE"));
//                        load_home();
//                    }
//                } catch (JSONException e) {
//                    M.E(e.getMessage());
//                }
//            }
//        });


        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragUpdateProfile profile = new FragUpdateProfile();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();

                Bundle args = new Bundle();
                args.putString("update_data", packageDetails);
                profile.setArguments(args);

                ft.addToBackStack("FragUpdateProfile");
                ft.replace(R.id.frame_container, profile);
                ft.commit();


            }
        });
        upgradePackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragPackageUpgrade profile = new FragPackageUpgrade();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();

                Bundle args = new Bundle();
                args.putString("reg_type", reg_type);
                profile.setArguments(args);

                ft.addToBackStack("FragPackageUpgrade");
                ft.replace(R.id.frame_container, profile);
                ft.commit();
            }
        });
        return v;
    }

    class DetailsTask extends AsyncTask<JSONObject, Void, String> {

        private Context context;
        private String postMethod;
        private WebserviceCallback callback;
        private ProgressDialog progDialog;


        public DetailsTask(Context context, String postMethod) {
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
                Log.e("zzz", postResult.toString());

                try {
                    packageDetails = postResult.toString();
                    JSONObject packageDetail = new JSONObject(packageDetails);
//                    Log.e("packageDetails", response.toString());
                    if (packageDetail.getString("MSG").equals("SUCCESS")) {
//                                        pkgRegType.setText(packageDetail.getString("REGTYPE"));
                        Log.e("zzz","success");
                        pkgDateofreg.setText(packageDetail.getString("REGDATE"));
                        pkgDateofstandard.setText(packageDetail.getString("STANDRDATE"));
                        pkgDateofPrimum.setText(packageDetail.getString("PREMIUMDATE"));
                        proEmailid.setText(packageDetail.getString("EMAIL"));
                        proFullName.setText(packageDetail.getString("FNAME") + " " + packageDetail.getString("LNAME") + " " + packageDetail.getString("MNAME"));
//                                        proFullName.setText(""+PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
                        proState.setText(packageDetail.getString("STATE"));
                        proZipCode.setText(packageDetail.getString("ZIP"));
                        proCountry.setText(packageDetail.getString("COUNTRY"));
                        proCity.setText(packageDetail.getString("CITY"));
                        proAddress.setText(packageDetail.getString("ADDRESS"));

                        reg_type = packageDetail.getString("REGTYPE");
                        if (reg_type.contains("PREMIUM")) {
//                                            M.T(getActivity(),"PREMIUM");
                            upgradePackage.setVisibility(View.GONE);
                        }

//                        packageDetail.getString("COUNTRY");
//                        packageDetail.getString("STATE");
//                        packageDetail.getString("CITY");
//                        packageDetail.getString("ADD");
//                        packageDetail.getString("PINCODE");
//                        packageDetail.getString("FULLNAME");
//                        packageDetail.getString("REGEMAIL");
//                        packageDetail.getString("REGID");
//                        packageDetail.getString("REGTYPE");
//                        packageDetail.getString("STANDRDATE");
//                        packageDetail.getString("PREMIUMDATE");
//                        packageDetail.getString("STATUS");



//                        PrefsHelper.getPrefsHelper().savePref("COUNTRY",packageDetail.getString("COUNTRY"));
//                        PrefsHelper.getPrefsHelper().savePref("STATE",packageDetail.getString("STATE"));
//                        PrefsHelper.getPrefsHelper().savePref("CITY",packageDetail.getString("CITY"));
//                        PrefsHelper.getPrefsHelper().savePref("ADD",packageDetail.getString("ADD"));
//                        PrefsHelper.getPrefsHelper().savePref("PINCODE",packageDetail.getString("PINCODE"));
//                        PrefsHelper.getPrefsHelper().savePref("FULLNAME",packageDetail.getString("FULLNAME"));
//                        PrefsHelper.getPrefsHelper().savePref("REGEMAIL",packageDetail.getString("REGEMAIL"));
//                        PrefsHelper.getPrefsHelper().savePref("REGID",packageDetail.getString("REGID"));
//                        PrefsHelper.getPrefsHelper().savePref("REGTYPE",packageDetail.getString("REGTYPE"));
//                        PrefsHelper.getPrefsHelper().savePref("STANDRDATE",packageDetail.getString("STANDRDATE"));
//                        PrefsHelper.getPrefsHelper().savePref("PREMIUMDATE",packageDetail.getString("PREMIUMDATE"));
//                        PrefsHelper.getPrefsHelper().savePref("STATUS",packageDetail.getString("STATUS"));

                    } else {
                        M.T(getActivity(), packageDetail.getString("MESSAGE"));
                        load_home();
                    }
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

    public void load_home() {
        FragHome home = new FragHome();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.frame_container, home);
        ft.commit();
    }


}
