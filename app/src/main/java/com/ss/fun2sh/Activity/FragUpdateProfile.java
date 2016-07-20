package com.ss.fun2sh.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.Helper;
import com.ss.fun2sh.CRUD.JSONParser;
import com.ss.fun2sh.CRUD.M;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.ss.fun2sh.R;
import com.ss.fun2sh.oldutils.Constants;
import com.ss.fun2sh.oldutils.RestMethods;
import com.ss.fun2sh.oldutils.WebserviceCallback;

public class FragUpdateProfile extends Fragment {
    Button updateProfile, upgradePackage;
    private Button updateProfileDone;
    private TextView updateProfileEmail;
    private EditText updateProfilePassword;
    private EditText updateProfileFullName;
    private Spinner updateProfileCountry;
    private Spinner updateProfileState;
    private Spinner updateProfileCity;
    private EditText updateProfileAddress;
    private EditText updateProfileZipCode;
    private TextView pkgRegType;
    private TextView pkgDateofreg;
    private TextView pkgDateofstandard;
    private TextView pkgDateofPrimum;
    String profileDetails, packageDetails;
    ArrayList<String> countryList, stateList, cityList;
    String sel_country,sel_state,sel_city;
    String prev_sel_country,prev_sel_state,prev_sel_city;
    Boolean isFirstCountry=false,isFirstState=false;
    ArrayList<String> dynamic_state_list=new ArrayList<String>();
    String reg_type;
    boolean isFirst=false;
    boolean isSecond=false;
//    String ur="http://mobileapp.dabank.co.uk/Fun2ShNMobileServices/Fun2ShMService.svc/Country2States";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_update_profile, container, false);
        super.onCreate(savedInstanceState);
        profileDetails = getArguments().getString("update_data");

        reg_type=PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.reg_type);
//        packageDetails = getArguments().getString("packageDetail");

//        try {
////            reg_type=new JSONObject(packageDetails).getString("REGTYPE");
//            JSONObject obj=new JSONObject(packageDetails);
////            reg_type=obj.getString("REGTYPE");
//            reg_type="";
//        } catch (JSONException e) {
//
//        }
//        reg_type=packageDetails.getString("REGTYPE");

        this.pkgDateofPrimum = (TextView) v.findViewById(R.id.pkgDateofPrimum);
        this.pkgDateofstandard = (TextView) v.findViewById(R.id.pkgDateofstandard);
        this.pkgDateofreg = (TextView) v.findViewById(R.id.pkgDateofreg);
        this.pkgRegType = (TextView) v.findViewById(R.id.pkgRegType);
        this.updateProfileZipCode = (EditText) v.findViewById(R.id.updateProfileZipCode);
        this.updateProfileAddress = (EditText) v.findViewById(R.id.updateProfileAddress);
        this.updateProfileCity = (Spinner) v.findViewById(R.id.updateProfileCity);
        this.updateProfileState = (Spinner) v.findViewById(R.id.updateProfileState);
        this.updateProfileCountry = (Spinner) v.findViewById(R.id.updateProfileCountry);
        this.updateProfileFullName = (EditText) v.findViewById(R.id.updateProfileFullName);
        this.updateProfilePassword = (EditText) v.findViewById(R.id.updateProfilePassword);
        this.updateProfileEmail = (TextView) v.findViewById(R.id.updateProfileEmail);
        updateProfile = (Button) v.findViewById(R.id.updateProfileDone);
        upgradePackage = (Button) v.findViewById(R.id.packageUpgrade);
        countryList = new ArrayList<>();
        stateList = new ArrayList<>();
        cityList = new ArrayList<>();
//qqq
        if (reg_type.contains("PREMIUM"))
        {
//           M.T(getActivity(),"PREMIUM");
            upgradePackage.setVisibility(View.GONE);
        }
        else {
            upgradePackage.setVisibility(View.VISIBLE);
        }
        //set ProfileDetails
        try {
            JSONObject data = new JSONObject(profileDetails);
//            updateProfileEmail.setText(data.getString("EMAIL"));
//            updateProfileFullName.setText(data.getString("FNAME") + " " + data.getString("LNAME") + " " + data.getString("MNAME"));
//            updateProfileZipCode.setText(data.getString("ZIP"));
//            updateProfileAddress.setText(data.getString("ADDRESS"));
            updateProfileEmail.setText(data.getString("REGEMAIL"));
            updateProfileFullName.setText(data.getString("FULLNAME"));
            updateProfileZipCode.setText(data.getString("PINCODE"));
            updateProfileAddress.setText(data.getString("ADD"));
            updateProfilePassword.setText(PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd).toString());

            Log.d("zzz",data.getString("COUNTRY"));
            Log.d("zzz",data.getString("STATE"));
            Log.d("zzz",data.getString("CITY"));

//            updateProfileState.se


            //get Country
//            prev_sel_country=data.getString("COUNTRY");
//            prev_sel_country=data.getString("COUNTRY");
//            prev_sel_country=data.getString("COUNTRY");
            int countryPos = 0;
            JSONArray countryRptDt = data.getJSONArray("CountriesRptDt");
            for (int i = 0; i < countryRptDt.length(); i++) {
                JSONObject countryObj = countryRptDt.getJSONObject(i);
                if (data.getString("COUNTRY").equals(countryObj.getString("CNAME")))
                    countryPos = i;
                countryList.add(countryObj.getString("CNAME"));
            }
            Log.d("zzz-countryPos",""+countryPos);
            Log.d("zzz-countryPos-val",countryList.get(countryPos).toString());
            ArrayAdapter<String> adapterCountry = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, countryList);
            updateProfileCountry.setAdapter(adapterCountry);
            updateProfileCountry.setSelection(countryPos);

            sel_country=updateProfileCountry.getSelectedItem().toString();
//            M.T(getActivity(),sel_country);
//get State
            int statePos = 0;
            JSONArray stateRptDt = data.getJSONArray("StatesRptDt");
            for (int i = 0; i < stateRptDt.length(); i++) {
                JSONObject stateObj = stateRptDt.getJSONObject(i);
                if (data.getString("STATE").equals(stateObj.getString("SNAME"))){
                    statePos = i;
                }
                stateList.add(stateObj.getString("SNAME"));
                Log.d("zzz-states",stateObj.getString("SNAME"));
            }
//            Log.d("zzz-statePos",""+statePos);
//            Log.d("zzz-statePos-val",stateList.get(statePos).toString());
            ArrayAdapter<String> adapterState = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, stateList);
            updateProfileState.setAdapter(adapterState);
            updateProfileState.setSelection(statePos);

            //get Citys
            int cityPos = 0;
            JSONArray citiesRptDt = data.getJSONArray("CitiesRptDt");
            for (int i = 0; i < citiesRptDt.length(); i++) {
                JSONObject citiObj = citiesRptDt.getJSONObject(i);
                if (data.getString("CITY").equals(citiObj.getString("CITYNAME"))){
                    cityPos = i;
                }
                cityList.add(citiObj.getString("CITYNAME"));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, cityList);
            updateProfileCity.setAdapter(adapter);
            updateProfileCity.setSelection(cityPos);

            //setPackageDetails
//            JSONObject packageDetail = new JSONObject(packageDetails);
//            pkgRegType.setText(packageDetail.getString("REGTYPE"));
            pkgDateofreg.setText(data.getString("REGDATE"));
            pkgDateofstandard.setText(data.getString("STANDRDATE"));
            pkgDateofPrimum.setText(data.getString("PREMIUMDATE"));

            reg_type=data.getString("PREMIUMDATE");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JSONParser(getActivity()).parseVollyJSONObject(Const.URL.updatePersonalDetails, 1, getParam(), new Helper() {
                    @Override
                    public void backResponse(String response) {
                        try {
                            JSONObject data = new JSONObject(response);
                            if (data.getString("MSG").equals("SUCCESS")) {
//                                M.T(getActivity(), data.getString("MESSAGE"));
                                load_home();
                            } else {
                                M.dError(getActivity(), data.getString("MESSAGE"));
                            }
                        } catch (JSONException e) {
                            M.E(e.getMessage());
                        }
                    }
                });
            }
        });
        upgradePackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragPackageUpgrade profile = new FragPackageUpgrade();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();

                Bundle args=new Bundle();
                args.putString("reg_type", reg_type);
                profile.setArguments(args);

                ft.addToBackStack("FragPackageUpgrade");
                ft.replace(R.id.frame_container, profile);
                ft.commit();
            }
        });


        updateProfileCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                sel_country=updateProfileCountry.getSelectedItem().toString();
                JSONObject jsonObject = new JSONObject();
                try {
//                    jsonObject.put("IDNO", "a4abim");
//                     jsonObject.put("PWD", "1234");
                    jsonObject.put("IDNO", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
                    jsonObject.put("PWD", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd));
                    jsonObject.put("CNAME", sel_country);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(isFirst) {
                    new TermsTask(getActivity(), Constants.COUNTRY2STATES_POSTMTD).execute(jsonObject);
                }
                else
                {
                    isFirst=true;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        updateProfileState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                sel_state=updateProfileState.getSelectedItem().toString();
                JSONObject jsonObject = new JSONObject();
                try {
//                    jsonObject.put("IDNO", "a4abim");
//                    jsonObject.put("PWD", "1234");
                    jsonObject.put("IDNO", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
                    jsonObject.put("PWD", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd));
                    jsonObject.put("CNAME", sel_state);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(isSecond) {
//                    if(isFirstState) {
                        new TermsTask2(getActivity(), Constants.STATES2CITY_POSTMTD).execute(jsonObject);
//                    }
//                    else
//                    {
//                        isFirstState=true;
//                    }
                }
                else
                {
                    isSecond=true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return v;
    }

    public JSONObject getParam() {
        JSONObject params = null;
        try {
            params = new JSONObject();
            params.put("IDNO", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
            params.put("PWD", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd));
//            params.put("COUNTRY", "India");
//            params.put("STATE", "Madhya Pradesh");
//            params.put("CITY", "Indore");
            params.put("COUNTRY", updateProfileCountry.getSelectedItem().toString());
            params.put("STATE", updateProfileState.getSelectedItem().toString());
            params.put("CITY", updateProfileCity.getSelectedItem().toString());
            String[] name = updateProfileFullName.getText().toString().split(" ");
            params.put("FNAME", name[0]);
            try {
                params.put("MNAME", name[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                params.put("MNAME", " ");
            }
            try {
                params.put("LNAME", name[2]);
            } catch (ArrayIndexOutOfBoundsException e) {
                params.put("LNAME", " ");
            }
            params.put("EMAIL", updateProfileEmail.getText());
            params.put("LPWD", updateProfilePassword.getText());
            params.put("ZIP", updateProfileZipCode.getText());
            params.put("ADDRESS", updateProfileAddress.getText());
            params.put("SEX", "");
        } catch (JSONException e) {
            M.E(e.getMessage());
        }
        return params;
    }

    public void load_home() {
        FragHome home = new FragHome();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.frame_container, home);
        ft.commit();
    }

    public void web(){
        sel_state=updateProfileState.getSelectedItem().toString();
        JSONObject jsonObject = new JSONObject();
        try {
//            jsonObject.put("IDNO", "a4abim");
//            jsonObject.put("PWD", "1234");
            jsonObject.put("IDNO", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
            jsonObject.put("PWD", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd));
            jsonObject.put("CNAME", sel_state);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new TermsTask2(getActivity(),Constants.STATES2CITY_POSTMTD).execute(jsonObject);
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
                Log.e("zzz",postResult.toString());
                //                    JSONObject array=new JSONObject(postResult);
//                    JSONArray array=new JSONArray(postResult);
//                    JSONObject obj=array.getJSONObject(0);
//                    String st=obj.getString("CMDESCR");
//                    txt_terms_content.setText(Html.fromHtml(st));

                ArrayList<String> stateOrProArrs = new ArrayList<String>();

                try {
                    JSONArray jsonArray = new JSONArray(postResult);
                    for (int index = 0; index < jsonArray.length(); index++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(index);
                        stateOrProArrs.add(jsonObject.getString("SNAME"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // String[] stateOrProArrs = getResources().getStringArray(
                // R.array.stateOrProArr);
                ArrayAdapter<String> stateOrProAdapter = new ArrayAdapter<String>(
                        getActivity(), android.R.layout.simple_spinner_item, stateOrProArrs);
                stateOrProAdapter
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                updateProfileState.setAdapter(stateOrProAdapter);

                web();

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
    class TermsTask2 extends AsyncTask<JSONObject, Void, String> {

        private Context context;
        private String postMethod;
        private WebserviceCallback callback;
        private ProgressDialog progDialog;


        public TermsTask2(Context context, String postMethod) {
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
//                Log.e("zzz",postResult.toString());
                //                    JSONObject array=new JSONObject(postResult);
//                    JSONArray array=new JSONArray(postResult);
//                    JSONObject obj=array.getJSONObject(0);
//                    String st=obj.getString("CMDESCR");
//                    txt_terms_content.setText(Html.fromHtml(st));

                ArrayList<String> CTDArrs = new ArrayList<String>();

                try {
                    JSONArray jsonArray = new JSONArray(postResult);
                    for (int index = 0; index < jsonArray.length(); index++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(index);
                        CTDArrs.add(jsonObject.getString("CNAME"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // String[] CTDArrs = getResources().getStringArray(R.array.CTDArr);
                ArrayAdapter<String> CTDAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_item, CTDArrs);
                CTDAdapter
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                updateProfileCity.setAdapter(CTDAdapter);

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

