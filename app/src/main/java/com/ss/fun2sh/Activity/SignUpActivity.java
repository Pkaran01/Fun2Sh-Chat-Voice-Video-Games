package com.ss.fun2sh.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.Helper;
import com.ss.fun2sh.CRUD.JSONParser;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.CRUD.UserAccount;
import com.ss.fun2sh.CRUD.Utility;
import com.ss.fun2sh.R;
import com.ss.fun2sh.oldutils.ConnectionDetector;
import com.ss.fun2sh.oldutils.Constants;
import com.ss.fun2sh.oldutils.PostTask;
import com.ss.fun2sh.oldutils.WebserviceCallback;

public class SignUpActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, WebserviceCallback {

    private android.widget.EditText invitationKey;
    private android.widget.Spinner country;
    private android.widget.Spinner state;
    private android.widget.Spinner city;
    private android.widget.EditText firstName;
    private android.widget.EditText middleName;
    private android.widget.EditText lastName;
    private android.widget.Spinner mob;
    private android.widget.Spinner dob;
    private android.widget.Spinner yob;
    private android.widget.EditText emailId;
    private android.widget.EditText userName;
    private android.widget.EditText password;
    private android.widget.EditText accessCode;
    private android.widget.Button  btn_tc;
    private android.widget.Button signUp;
    private android.widget.CheckBox termsCheckBox;
    ArrayList<String> countryList, stateList, cityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);

        String postResult = getIntent().getStringExtra("postResult");
//        M.T(SignUpActivity.this,postResult.toString());

        this.signUp = (Button) findViewById(R.id.signUp);
        this. btn_tc = (Button) findViewById(R.id. btn_tc);
        this.accessCode = (EditText) findViewById(R.id.accessCode);
        this.password = (EditText) findViewById(R.id.password);
        this.userName = (EditText) findViewById(R.id.userName);
        this.emailId = (EditText) findViewById(R.id.emailId);
        this.yob = (Spinner) findViewById(R.id.yob);
        this.dob = (Spinner) findViewById(R.id.dob);
        this.mob = (Spinner) findViewById(R.id.mob);
        this.lastName = (EditText) findViewById(R.id.lastName);
        this.middleName = (EditText) findViewById(R.id.middleName);
        this.firstName = (EditText) findViewById(R.id.firstName);
        this.city = (Spinner) findViewById(R.id.city);
        this.state = (Spinner) findViewById(R.id.state);
        this.country = (Spinner) findViewById(R.id.country);
        this.invitationKey = (EditText) findViewById(R.id.invitationKey);
        this.termsCheckBox = (CheckBox) findViewById(R.id.termsCheckBox);
        //  final MaterialDialog materialDialog = M.initProgressDialog(SignUpActivity.this);
        countryList = new ArrayList<>();
        stateList = new ArrayList<>();
        cityList = new ArrayList<>();

        setTextToWidgets(postResult);
        setListenersToWidgets();

        btn_tc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this,TermsActivity.class));
            }
        });

        //zzz
        new JSONParser(SignUpActivity.this).parseVollyJSONObject(Const.URL.getCountry, 1, Utility.getBlankParam(), new Helper() {
            @Override
            public void backResponse(String response) {
                try {
                    JSONArray res = new JSONArray(response);
                    for (int i = 0; i < res.length(); i++) {
                        JSONObject object = res.getJSONObject(i);
                        countryList.add(object.getString("CNAME"));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(SignUpActivity.this, android.R.layout.simple_dropdown_item_1line, countryList);
                    country.setAdapter(adapter);
//                    materialDialog.dismiss();
                } catch (JSONException e) {
                    M.E(e.getMessage());
                }
            }
        });
        /*
        country.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final MaterialDialog materialDialog = M.initProgressDialog(SignUpActivity.this);
                new JSONParser(SignUpActivity.this).parseVollyJSONObject(Const.URL.country2States, 1, getStateParam(), new Helper() {
                    @Override
                    public void backResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            for (int i = 0; i < res.length(); i++) {
                                JSONObject object = res.getJSONObject(i);
                                countryList.add(object.getString("SNAME"));
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SignUpActivity.this, android.R.layout.simple_dropdown_item_1line, countryList);
                            state.setAdapter(adapter);
                            materialDialog.dismiss();
                        } catch (JSONException e) {
                            M.E(e.getMessage());
                        }
                    }
                });
            }
        });

        state.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final MaterialDialog materialDialog = M.initProgressDialog(SignUpActivity.this);
                new JSONParser(SignUpActivity.this).parseVollyJSONObject(Const.URL.state2City, 1, getCityParam(), new Helper() {
                    @Override
                    public void backResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            for (int i = 0; i < res.length(); i++) {
                                JSONObject object = res.getJSONObject(i);
                                countryList.add(object.getString("CNAME"));
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SignUpActivity.this, android.R.layout.simple_dropdown_item_1line, countryList);
                            state.setAdapter(adapter);
                            materialDialog.dismiss();
                        } catch (JSONException e) {
                            M.E(e.getMessage());
                        }
                    }
                });
            }
        });
        */
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SignUpActivity.this, android.R.layout.simple_dropdown_item_1line, getYear());
        yob.setAdapter(adapter);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserAccount.isEmpty(invitationKey, firstName, lastName, emailId, userName, password, accessCode)) {
                    if (termsCheckBox.isChecked()) {
                        M.E(getParam().toString());
                        new JSONParser(SignUpActivity.this).parseVollyJSONObject(Const.URL.signUp, 1, getParam(), new Helper() {
                            @Override
                            public void backResponse(String response) {
                                try {
                                    JSONObject data = new JSONObject(response);
                                    if (data.getString("MSG").equals("SUCCESS")) {
                                        M.T(SignUpActivity.this, data.getString("MESSAGE"));
                                        M.dSuccess(SignUpActivity.this,"\n" +
                                                "Congratulastions! ","You have successfully registered with Fun2sh.\n" +
                                                "Please proceed to Log in and enjoy the excitement of Fun2sh Entertainment Platform.");
                                        M.I(SignUpActivity.this, LoginActivity.class, null);
                                    } else {
                                        M.dError(SignUpActivity.this, data.getString("MESSAGE"));
                                    }
                                } catch (JSONException e) {
                                    M.E(e.getMessage());
                                }
                            }
                        });
                    } else {
                        M.T(SignUpActivity.this, "Please Select terms & Conditions");
                    }
                } else {
                    UserAccount.EditTextPointer.requestFocus();
                    UserAccount.EditTextPointer.setError("This can't be empty !");
                }
            }
        });
    }
    private void setTextToWidgets(String postResult) {

        ArrayList<String> countryResidArrs = new ArrayList<String>();
        countryResidArrs.add(Constants.SELECT_COUNTRY);


        try {
            JSONArray jsonArray = new JSONArray(postResult);
            for (int index = 0; index < jsonArray.length(); index++) {

                JSONObject jsonObject = jsonArray.getJSONObject(index);
                countryResidArrs.add(jsonObject.getString("CNAME"));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // String[] countryResidArrs = getResources().getStringArray(
        // R.array.countryResidArr);
        ArrayAdapter<String> countryResidAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, countryResidArrs);
        countryResidAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        country.setAdapter(countryResidAdapter);

    }

    private void setListenersToWidgets() {
        country.setOnItemSelectedListener(this);
        state.setOnItemSelectedListener(this);

    }


    private JSONObject getStateParam() {
        JSONObject param = new JSONObject();
        try {
            param.put("IDNO", "");
            param.put("PWD", "");
            param.put("CNAME", country.getSelectedItem().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return param;
    }

    private JSONObject getCityParam() {
        JSONObject param = new JSONObject();
        try {
            param.put("IDNO", "");
            param.put("PWD", "");
            param.put("CNAME", state.getSelectedItem().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return param;
    }

    public JSONObject getParam() {
        JSONObject params = null;
        try {
            params = new JSONObject();
            params.put("INVKEY", invitationKey.getText());
//            params.put("COUNTRY", "India");
//            params.put("STATE", "Madhya Pradesh");
//            params.put("CITY", "Indore");
            params.put("COUNTRY", country.getSelectedItem().toString());
            params.put("STATE", state.getSelectedItem().toString());
            params.put("CITY", city.getSelectedItem().toString());
            params.put("FNAME", firstName.getText());
            params.put("MNAME", middleName.getText());
            params.put("LNAME", lastName.getText());
            params.put("DOB", dob.getSelectedItem().toString() + "/" + mob.getSelectedItem().toString() + "/" + yob.getSelectedItem().toString());
            params.put("EMAIL", emailId.getText());
            params.put("USERNAME", userName.getText());
            params.put("PASSWORD", password.getText());
            params.put("ACCCODE", accessCode.getText());
            params.put("IPADDRESS", Utility.getIPAddress(true));
        } catch (JSONException e) {
            M.E(e.getMessage());
        }
        return params;
    }

    public ArrayList<String> getYear() {
        ArrayList<String> years = new ArrayList<String>();
        years.add(0, "Year of birth");
//        int startYear = 1985;
        int startYear = 1900;
        int endYear= Calendar.getInstance().get(Calendar.YEAR)-18;
//        while (startYear < Calendar.getInstance().get(Calendar.YEAR)-17) {
//            years.add(String.valueOf(startYear));
//            startYear++;
//        }

        while (endYear >= startYear ) {
            years.add(String.valueOf(endYear));
            endYear--;
        }
        return years;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (parent.getId() == R.id.country) {
            String selectedContry = (String) country.getSelectedItem();

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("IDNO", "");
                jsonObject.put("PWD", "");
                jsonObject.put("CNAME", selectedContry);
                callWebservice(jsonObject, Constants.COUNTRY2STATES_POSTMTD);



            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (parent.getId() == R.id.state) {
            String selectedState = (String) state.getSelectedItem();

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("IDNO", "");
                jsonObject.put("PWD", "");
                jsonObject.put("CNAME", selectedState);
                callWebservice(jsonObject, Constants.STATES2CITY_POSTMTD);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    private void callWebservice(JSONObject jsonObject, String postMethod) {

        if (ConnectionDetector.isConnected(this)) {
            PostTask postTask = new PostTask(SignUpActivity.this, postMethod, SignUpActivity.this);
            postTask.execute(jsonObject);
        } else {
//            showToastMsg(Constants.NO_INTERNET);
            M.T(SignUpActivity.this,Constants.NO_INTERNET);
        }

    }

    @Override
    public void postResult(String postResult, String postMethod) {

        if (postMethod.equals(Constants.COUNTRY2STATES_POSTMTD)) {

            ArrayList<String> stateOrProArrs = new ArrayList<String>();
            stateOrProArrs.add(Constants.SELECT_STATE);
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
                    this, android.R.layout.simple_spinner_item, stateOrProArrs);
            stateOrProAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            state.setAdapter(stateOrProAdapter);

        } else if (postMethod.equals(Constants.STATES2CITY_POSTMTD)) {

            ArrayList<String> CTDArrs = new ArrayList<String>();
            CTDArrs.add(Constants.SELECT_CITY);
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
            ArrayAdapter<String> CTDAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, CTDArrs);
            CTDAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            city.setAdapter(CTDAdapter);

        }

    }

}
