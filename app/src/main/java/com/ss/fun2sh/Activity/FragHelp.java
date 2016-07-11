package com.ss.fun2sh.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import com.ss.fun2sh.Adapter.CloseTicketsAdapter;
import com.ss.fun2sh.Adapter.NotificationListAdapter;
import com.ss.fun2sh.Adapter.OpenTicketAdapter;
import com.ss.fun2sh.CRUD.AndroidMultiPartEntity;
import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.JSONParser;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.CRUD.PrefsHelper;
import com.ss.fun2sh.R;
import com.ss.fun2sh.oldutils.ConnectionDetector;
import com.ss.fun2sh.oldutils.Constants;

import com.ss.fun2sh.oldutils.RestMethods;
import com.ss.fun2sh.oldutils.WebserviceCallback;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;

public class FragHelp extends Fragment {
    TextView close_ticketBT, notificationBT, new_ticketBT, tiketsName, open_ticketBT;
    OpenTicketAdapter ticketAdapter;
    ListView openticket_list, newticketListview, closeTicketListView, notificationListview;
    LinearLayout openticketlinear, closeticketlinear, notificattionlinear, newTicketlinear;
    NotificationListAdapter notificationListAdapter;
    CloseTicketsAdapter closeTicketsAdapter;
    TextView attach_create, txt_preview, txt_create_submit;
    String filePath = "";
    String img_name = "";
    int RESULT_OK = -1;
    ProgressDialog progDialog;
    Spinner spin_dept, spin_sub;
    EditText ed_msg;
    static String cur_web_method;
    boolean is_spin_debt_first = false, is_spin_sub_first = false, is_open = false, is_close = false;
    public static String sel_method = "open";
    HashMap<String, ArrayList<String>> not_map, open_map, close_map;
    long max_file_size = 3000000;
    int PICKFILE_REQUEST_CODE = 10;
    private AndroidMultiPartEntity reqEntity;
    long totalSize = 0;
    public static FragHelp fragHelp;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_help, container, false);
        super.onCreate(savedInstanceState);
        tiketsName = (TextView) v.findViewById(R.id.tiketsName);
        fragHelp=this;
        open_ticketBT = (TextView) v.findViewById(R.id.open_ticketBT);
        close_ticketBT = (TextView) v.findViewById(R.id.close_ticketBT);
        notificationBT = (TextView) v.findViewById(R.id.notificationBT);
        new_ticketBT = (TextView) v.findViewById(R.id.new_ticketBT);

        openticket_list = (ListView) v.findViewById(R.id.openticket_list);
        closeTicketListView = (ListView) v.findViewById(R.id.closeTicketListView);
        notificationListview = (ListView) v.findViewById(R.id.notificationListview);

        openticketlinear = (LinearLayout) v.findViewById(R.id.openticketlinear);
        closeticketlinear = (LinearLayout) v.findViewById(R.id.closeticket);
        notificattionlinear = (LinearLayout) v.findViewById(R.id.notificattion);
        newTicketlinear = (LinearLayout) v.findViewById(R.id.newTicket);

        attach_create = (TextView) v.findViewById(R.id.txt_attach_create);
        txt_preview = (TextView) v.findViewById(R.id.txt_preview);

//        ticketAdapter = new OpenTicketAdapter(getActivity());
//        openticket_list.setAdapter(ticketAdapter);
        tiketsName.setText("Create New Ticket");

        //create new ticket

        spin_dept = (Spinner) v.findViewById(R.id.spin_dept);
        spin_sub = (Spinner) v.findViewById(R.id.spin_sub);
        txt_create_submit = (TextView) v.findViewById(R.id.txt_create_submit);
        ed_msg = (EditText) v.findViewById(R.id.ed_msg);


//        notificationListAdapter = new NotificationListAdapter(getActivity());
//        notificationListview.setAdapter(notificationListAdapter);

//        closeTicketsAdapter = new CloseTicketsAdapter(getActivity());
//        closeTicketListView.setAdapter(closeTicketsAdapter);

        open_ticketBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tiketsName.setText("Open Tickets");
                openticketlinear.setVisibility(View.VISIBLE);
                notificattionlinear.setVisibility(View.GONE);
                newTicketlinear.setVisibility(View.GONE);
                closeticketlinear.setVisibility(View.GONE);

                is_open = true;
                is_close = false;

                JSONObject obj = new JSONObject();
                try {
                    obj.put("IDNO", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
                    obj.put("PWD", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd));
                    obj.put("TKTTYPE", "Open");
                    obj.put("SITE", "Fun2sh");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sel_method = "open";
                if (ConnectionDetector.isConnected(getActivity())) {
                    new HelpTask(getActivity(), Constants.HELP_OPEN_CLOSE).execute(obj);
                } else {
                    M.T(getActivity(), Constants.NO_INTERNET);
                }
//                new HelpTask(getActivity(),Constants.HELP_OPEN_CLOSE).execute(obj);

            }
        });


        close_ticketBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tiketsName.setText("Closed Tickets");
                openticketlinear.setVisibility(View.GONE);
                notificattionlinear.setVisibility(View.GONE);
                newTicketlinear.setVisibility(View.GONE);
                closeticketlinear.setVisibility(View.VISIBLE);

                is_open = false;
                is_close = true;

                JSONObject obj = new JSONObject();
                try {
                    obj.put("IDNO", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
                    obj.put("PWD", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd));
                    obj.put("TKTTYPE", "Closed");
                    obj.put("SITE", "Fun2sh");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sel_method = "close";
                if (ConnectionDetector.isConnected(getActivity())) {
                    new HelpTask(getActivity(), Constants.HELP_OPEN_CLOSE).execute(obj);
                } else {
                    M.T(getActivity(), Constants.NO_INTERNET);
                }
//                new HelpTask(getActivity(),Constants.HELP_OPEN_CLOSE).execute(obj);
            }
        });


        notificationBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tiketsName.setText("Notifications");
                openticketlinear.setVisibility(View.GONE);
                notificattionlinear.setVisibility(View.VISIBLE);
                newTicketlinear.setVisibility(View.GONE);
                closeticketlinear.setVisibility(View.GONE);

                JSONObject obj = new JSONObject();
                try {
                    obj.put("SITE", "Fun2sh");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (ConnectionDetector.isConnected(getActivity())) {
                    new HelpTask(getActivity(), Constants.HELP_ADMIN).execute(obj);
                } else {
                    M.T(getActivity(), Constants.NO_INTERNET);
                }
//                new HelpTask(getActivity(),Constants.HELP_ADMIN).execute(obj);

            }
        });


        new_ticketBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tiketsName.setText("Create New Ticket");
                openticketlinear.setVisibility(View.GONE);
                notificattionlinear.setVisibility(View.GONE);
                newTicketlinear.setVisibility(View.VISIBLE);
                closeticketlinear.setVisibility(View.GONE);
                if (ConnectionDetector.isConnected(getActivity())) {
                    new HelpTask2(getActivity(), Constants.HELP_DEPT).execute(getParam());
                    new HelpTask3(getActivity(), Constants.HELP_SUB).execute(getParam());
                } else {
                    M.T(getActivity(), Constants.NO_INTERNET);
                }
            }
        });

        openticket_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent suportintent = new Intent(getActivity(), SupportTicketActivity.class);
                Bundle b = new Bundle();
                Log.d("ddd", "open_map size" + open_map.size());
                Log.d("ddd", "open_map.get(Constants.TKTSLNO)--" + open_map.get(Constants.TKTSLNO).get(position));
                Log.d("ddd", "open_map.get(Constants.TKTNO)--" + open_map.get(Constants.TKTNO).get(position));
                Log.d("ddd", "open_map.get(Constants.DEPTNAM)--" + open_map.get(Constants.DEPTNAM).get(position));
                Log.d("ddd", "open_map.get(Constants.SUBJECT)--" + open_map.get(Constants.SUBJECT).get(position));
                Log.d("ddd", "position" + position);
                Log.d("ddd", "sel_method" + sel_method);
                b.putString(Constants.TKTSLNO, open_map.get(Constants.TKTSLNO).get(position));
                b.putString(Constants.TKTNO, open_map.get(Constants.TKTNO).get(position));
                b.putString(Constants.DEPTNAM, open_map.get(Constants.DEPTNAM).get(position));
                b.putString(Constants.SUBJECT, open_map.get(Constants.SUBJECT).get(position));
                b.putString("sel_method", sel_method);

                suportintent.putExtras(b);
                startActivity(suportintent);
            }
        });


        closeTicketListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent suportintent = new Intent(getActivity(), SupportTicketActivity.class);

                Bundle b = new Bundle();
                b.putString(Constants.TKTSLNO, close_map.get(Constants.TKTSLNO).get(position));
                b.putString(Constants.TKTNO, close_map.get(Constants.TKTNO).get(position));
                b.putString(Constants.DEPTNAM, close_map.get(Constants.DEPTNAM).get(position));
                b.putString(Constants.SUBJECT, close_map.get(Constants.SUBJECT).get(position));
                b.putString("sel_method", sel_method);
                suportintent.putExtras(b);
                getActivity().startActivity(suportintent);

            }
        });

        notificationListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent suportintent = new Intent(getActivity(), NotificationActivity.class);

                Bundle b = new Bundle();
                b.putSerializable("not_map", not_map);
                b.putString("ATTCHEMENT", not_map.get("ATTCHEMENT").get(position));
                b.putString("ISSBY", not_map.get("ISSBY").get(position));
                b.putString("ISSCODE", not_map.get("ISSCODE").get(position));
                b.putString("ISSON", not_map.get("ISSON").get(position));
                b.putString("MESSAGE", not_map.get("MESSAGE").get(position));
                b.putString("SUBJECT", not_map.get("SUBJECT").get(position));

                suportintent.putExtras(b);
                getActivity().startActivity(suportintent);

            }
        });


//        HelpTask task1= (HelpTask) new HelpTask(getActivity(),Constants.HELP_DEPT).execute(getParam());
//        new HelpTask2(getActivity(),Constants.HELP_DEPT).execute(getParam());

//        JSONObject obj3=new JSONObject();
//        try {
//            obj3.put("DEPTNAME","Sales");
//            obj3.put("SITE","Fun2sh");
//            new HelpTask3(getActivity(),Constants.HELP_SUB).execute(obj3);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }


        attach_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFile("*/*");
            }
        });

//        newTicketMethod();
//        callSubjectMethod();

        spin_dept.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (is_spin_debt_first) {
                    JSONObject obj3 = new JSONObject();
                    try {
                        obj3.put("DEPTNAME", spin_dept.getSelectedItem().toString());
                        obj3.put("SITE", "Fun2sh");

                        if (ConnectionDetector.isConnected(getActivity())) {
                            new HelpTask3(getActivity(), Constants.HELP_SUB).execute(obj3);
                        } else {
                            M.T(getActivity(), Constants.NO_INTERNET);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    is_spin_debt_first = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        spin_sub.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//                if(is_spin_sub_first) {
//                    M.T(getActivity(), "selected spin sub");
//
//                    JSONObject obj4=new JSONObject();
//                    try {
//                        obj4.put("DEPTNAME",spin_dept.getSelectedItem().toString());
//                        obj4.put("SITE","Fun2sh");
//                        new HelpTask3(getActivity(),Constants.HELP_SUB).execute(obj4);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//                else
//                {
//                    is_spin_sub_first=true;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });


        txt_create_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (img_name.length() > 4) {

                } else {


                    JSONObject param = new JSONObject();
                    try {
                        param.put("IDNO", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
                        param.put("PWD", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd));
                        param.put("DEPTNAME", spin_dept.getSelectedItem().toString());
                        param.put("SUBJECT", spin_sub.getSelectedItem().toString());
                        param.put("MESSAGE", ed_msg.getText().toString());
                        param.put("ATTACHMENT", "" + img_name);
                        param.put("SITE", "Fun2sh");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (ConnectionDetector.isConnected(getActivity())) {
                        new HelpTask(getActivity(), Constants.HELP_NEW).execute(param);
                    } else {
                        M.T(getActivity(), Constants.NO_INTERNET);
                    }
                }
            }
        });

        if (ConnectionDetector.isConnected(getActivity())) {
            new HelpTask2(getActivity(), Constants.HELP_DEPT).execute(getParam());
            new HelpTask3(getActivity(), Constants.HELP_SUB).execute(getParam());
        } else {
            M.T(getActivity(), Constants.NO_INTERNET);
        }

        return v;
    }

    public void openFile(String minmeType) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(minmeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", minmeType);
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (getActivity().getPackageManager().resolveActivity(sIntent, 0) != null) {
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
        } else {
            chooserIntent = Intent.createChooser(intent, "Open file");
        }
        try {
            startActivityForResult(chooserIntent, PICKFILE_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity().getApplicationContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
        }
    }

    public void newTicketMethod() {


        if (ConnectionDetector.isConnected(getActivity())) {
            new HelpTask2(getActivity(), Constants.HELP_DEPT).execute(getParam());
        } else {
            M.T(getActivity(), Constants.NO_INTERNET);
        }

//        new HelpTask3(getActivity(),Constants.HELP_SUB).execute(getParam());

        callSubjectMethod();


    }

    public void callSubjectMethod() {
        String dept = spin_dept.getSelectedItem().toString();

        JSONObject obj2 = new JSONObject();
        try {
            obj2.put("SITE", "Fun2sh");
            obj2.put("DEPTNAME", dept);


            if (ConnectionDetector.isConnected(getActivity())) {
                new HelpTask3(getActivity(), Constants.HELP_SUB).execute(obj2);
            } else {
                M.T(getActivity(), Constants.NO_INTERNET);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getParam() {
        JSONObject param = new JSONObject();
        try {
            param.put("SITE", "Fun2sh");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return param;
    }

    public JSONObject getParams() {
        JSONObject param = new JSONObject();
        try {
            param.put("IDNO", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
            param.put("PWD", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd));
//            param.put("IDNO", "a4abim");
//            param.put("PWD", "1234");
//            param.put("DEPTNAME","dname");
//            param.put("SUBJECT","sub");
            param.put("DEPTNAME", spin_dept.getSelectedItem().toString());
            param.put("SUBJECT", spin_sub.getSelectedItem().toString());
            param.put("MESSAGE", "msg");
            param.put("ATTACHMENT", "attachName");
            param.put("SITE", "Fun2sh");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return param;
    }

    class HelpTask extends AsyncTask<JSONObject, Void, String> {

        private Context context;
        private String postMethod;
        private WebserviceCallback callback;
        private ProgressDialog progDialog;

        public HelpTask(Context context, String postMethod) {
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
            cur_web_method = postMethod;
            return RestMethods.WSPost(jsonParam, postMethod);

        }

        @Override
        protected void onPostExecute(String postResult) {
            super.onPostExecute(postResult);
            dismissProgressDialog();

//        M.T(con, "PostExecute");
            if (postResult != null) {

                Log.e("ddd", postResult.toString());
                if (postMethod.contains(Constants.HELP_DEPT)) {

                    ArrayList<String> stateOrProArrs = new ArrayList<String>();

                    try {
                        JSONArray jsonArray = new JSONArray(postResult);
                        for (int index = 0; index < jsonArray.length(); index++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(index);
                            stateOrProArrs.add(jsonObject.getString("DEPTNAME"));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    ArrayAdapter<String> stateOrProAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, stateOrProArrs);
                    stateOrProAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spin_dept.setAdapter(stateOrProAdapter);
                } else if (postMethod.contains(Constants.HELP_SUB)) {
                    ArrayList<String> stateOrProArrs = new ArrayList<String>();

                    try {
                        JSONArray jsonArray = new JSONArray(postResult);
                        for (int index = 0; index < jsonArray.length(); index++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(index);
                            stateOrProArrs.add(jsonObject.getString("SUBJECT"));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (postMethod.contains(Constants.HELP_OPEN_CLOSE)) {

                    if (sel_method.contains("open")) {
                        open_map = new HashMap<String, ArrayList<String>>();
                        ArrayList<String> open_tktslno_list = new ArrayList<String>();
                        ArrayList<String> open_tktno_list = new ArrayList<String>();
                        ArrayList<String> open_lastupdate_list = new ArrayList<String>();
                        ArrayList<String> open_sub_list = new ArrayList<String>();
                        ArrayList<String> open_resawait_list = new ArrayList<String>();
                        ArrayList<String> open_deptnam_list = new ArrayList<String>();

                        JSONArray open_array = null;
//                        M.T(getActivity(),"open");
                        try {
                            open_array = new JSONArray(postResult);
                            Log.d("ddd", "length of array--" + open_array.length());


                            JSONObject open_jobj;
                            for (int j = 0; j < open_array.length(); j++) {
                                open_jobj = open_array.getJSONObject(j);
                                Log.d("ddd", "length of array--" + open_array.length());

                                open_tktslno_list.add(open_jobj.getString(Constants.TKTSLNO));
                                Log.d("ddd--trtslno", open_jobj.getString(Constants.TKTSLNO));
                                open_tktno_list.add(open_jobj.getString(Constants.TKTNO));
                                open_lastupdate_list.add(open_jobj.getString(Constants.LASTUPDATE));
                                open_sub_list.add(open_jobj.getString(Constants.SUBJECT));
                                open_resawait_list.add(open_jobj.getString(Constants.RESAWAIT));
                                open_deptnam_list.add(open_jobj.getString(Constants.DEPTNAM));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.d("ddd--empty", "" + open_tktslno_list.isEmpty());
                        Log.d("ddd--size", "" + open_tktno_list.size());

                        open_map.put(Constants.TKTSLNO, open_tktslno_list);
                        open_map.put(Constants.TKTNO, open_tktno_list);
                        open_map.put(Constants.LASTUPDATE, open_lastupdate_list);
                        open_map.put(Constants.SUBJECT, open_sub_list);
                        open_map.put(Constants.RESAWAIT, open_resawait_list);
                        open_map.put(Constants.DEPTNAM, open_deptnam_list);


//                            ticketAdapter = new OpenTicketAdapter(getActivity(),open_array.length(),open_map);
//                        ticketAdapter = new OpenTicketAdapter(getActivity(),open_array.length());
//                        ticketAdapter = new OpenTicketAdapter(getActivity());
//                            openticket_list.setAdapter(ticketAdapter);


//                        ticketAdapter = new OpenTicketAdapter(getActivity(),open_array.length(),open_map);
                        ticketAdapter = new OpenTicketAdapter(getActivity(), open_array.length(), open_map);
                        openticket_list.setAdapter(ticketAdapter);

                    } else if (sel_method.contains("close")) {


                        close_map = new HashMap<String, ArrayList<String>>();
                        ArrayList<String> close_tktslno_list = new ArrayList<String>();
                        ArrayList<String> close_tktno_list = new ArrayList<String>();
                        ArrayList<String> close_lastupdate_list = new ArrayList<String>();
                        ArrayList<String> close_sub_list = new ArrayList<String>();
                        ArrayList<String> close_resawait_list = new ArrayList<String>();
                        ArrayList<String> close_deptnam_list = new ArrayList<String>();

                        JSONArray close_array = null;
//                        M.T(getActivity(),"close");
                        try {
                            close_array = new JSONArray(postResult);

                            JSONObject close_jobj;
                            for (int j = 0; j < close_array.length(); j++) {
                                close_jobj = close_array.getJSONObject(j);

                                close_tktslno_list.add(close_jobj.getString(Constants.TKTSLNO));
                                close_tktno_list.add(close_jobj.getString(Constants.TKTNO));
                                close_lastupdate_list.add(close_jobj.getString(Constants.LASTUPDATE));
                                close_sub_list.add(close_jobj.getString(Constants.SUBJECT));
                                close_resawait_list.add(close_jobj.getString(Constants.RESAWAIT));
                                close_deptnam_list.add(close_jobj.getString(Constants.DEPTNAM));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        close_map.put(Constants.TKTSLNO, close_tktslno_list);
                        close_map.put(Constants.TKTNO, close_tktno_list);
                        close_map.put(Constants.LASTUPDATE, close_lastupdate_list);
                        close_map.put(Constants.SUBJECT, close_sub_list);
                        close_map.put(Constants.RESAWAIT, close_resawait_list);
                        close_map.put(Constants.DEPTNAM, close_deptnam_list);

                        closeTicketsAdapter = new CloseTicketsAdapter(getActivity(), close_array.length(), close_map);
                        closeTicketListView.setAdapter(closeTicketsAdapter);
                    } else {
//                        M.T(getActivity(),"nothing");
                        Log.d("ddd", "" + sel_method);
                    }

                } else if (postMethod.contains(Constants.HELP_ADMIN)) {
                    Log.d("ddd", Constants.HELP_ADMIN);
                    try {
                        JSONArray array = new JSONArray(postResult);
                        Log.d("ddd", "length of array--" + array.length());

                        not_map = new HashMap<String, ArrayList<String>>();
                        ArrayList<String> not_attach_list = new ArrayList<String>();
                        ArrayList<String> not_issby_list = new ArrayList<String>();
                        ArrayList<String> not_isscode_list = new ArrayList<String>();
                        ArrayList<String> not_isson_list = new ArrayList<String>();
                        ArrayList<String> not_msg_list = new ArrayList<String>();
                        ArrayList<String> not_sub_list = new ArrayList<String>();

                        for (int i = 0; i < array.length(); i++) {

                        }

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject not_jobj = array.getJSONObject(i);

//                            String attach=not_jobj.getString("ATTCHEMENT");
//                            String issby=not_jobj.getString("ISSBY");
//                            String isscode=not_jobj.getString("ISSCODE");
//                            String isson=not_jobj.getString("ISSON");
//                            String msg=not_jobj.getString("MESSAGE");
//                            String sub=not_jobj.getString("SUBJECT");

                            not_attach_list.add(not_jobj.getString("ATTCHEMENT"));
                            not_issby_list.add(not_jobj.getString("ISSBY"));
                            not_isscode_list.add(not_jobj.getString("ISSCODE"));
                            not_isson_list.add(not_jobj.getString("ISSON"));
                            not_msg_list.add(not_jobj.getString("MESSAGE"));
                            not_sub_list.add(not_jobj.getString("SUBJECT"));

                        }

                        not_map.put("ATTCHEMENT", not_attach_list);
                        not_map.put("ISSBY", not_issby_list);
                        not_map.put("ISSCODE", not_isscode_list);
                        not_map.put("ISSON", not_isson_list);
                        not_map.put("MESSAGE", not_msg_list);
                        not_map.put("SUBJECT", not_sub_list);

                        notificationListAdapter = new NotificationListAdapter(getActivity(), array.length(), not_map);
                        notificationListview.setAdapter(notificationListAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (postMethod.contains(Constants.HELP_NEW)) {
                    Log.d("ddd", Constants.HELP_NEW);
                    img_name = "";
                    try {
                        JSONObject open_jobj = new JSONObject(postResult);
                        String str = open_jobj.getString("MSG");
                        String str2 = open_jobj.getString("MESSAGE");
                        Log.d("ddd STR", str);
                        Log.d("ddd STR2", str2);

                        if (open_jobj.getString("MSG").equalsIgnoreCase("SUCCESS")) {
                            tiketsName.setText("Open Tickets");
                            openticketlinear.setVisibility(View.VISIBLE);
                            notificattionlinear.setVisibility(View.GONE);
                            newTicketlinear.setVisibility(View.GONE);
                            closeticketlinear.setVisibility(View.GONE);

                            JSONObject obj = new JSONObject();
                            try {
                                obj.put("IDNO", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
                                obj.put("PWD", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd));
                                obj.put("TKTTYPE", "Open");
                                obj.put("SITE", "Fun2sh");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            sel_method = "open";
                            new HelpTask(getActivity(), Constants.HELP_OPEN_CLOSE).execute(obj);

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {

                }

            } else {
                Toast.makeText(context, "post result is empty", Toast.LENGTH_LONG).show();
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

    class HelpTask2 extends AsyncTask<JSONObject, Void, String> {

        private Context context;
        private String postMethod;
        private WebserviceCallback callback;
        private ProgressDialog progDialog;


        public HelpTask2(Context context, String postMethod) {
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
            cur_web_method = postMethod;
            return RestMethods.WSPost(jsonParam, postMethod);

        }

        @Override
        protected void onPostExecute(String postResult) {
            super.onPostExecute(postResult);
            dismissProgressDialog();

            if (postResult != null) {

                Log.e("zzz", postResult.toString());
                Log.d("result", "Department");
                Log.e("zzz", cur_web_method);

                ArrayList<String> deptlist = new ArrayList<String>();

                try {
                    JSONArray jsonArray = new JSONArray(postResult);
                    for (int index = 0; index < jsonArray.length(); index++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(index);
                        deptlist.add(jsonObject.getString("DEPTNAME"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ArrayAdapter<String> deptAdapter = new ArrayAdapter<String>(
                        getActivity(), android.R.layout.simple_spinner_item, deptlist);
                deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spin_dept.setAdapter(deptAdapter);


            } else {
                Toast.makeText(context, "post result is empty", Toast.LENGTH_LONG).show();
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

    class HelpTask3 extends AsyncTask<JSONObject, Void, String> {

        private Context context;
        private String postMethod;
        private WebserviceCallback callback;
        private ProgressDialog progDialog;

        public HelpTask3(Context context, String postMethod) {
            this.context = context;
            this.postMethod = postMethod;
        }

        private void showProgressDialog() {
            progDialog = new ProgressDialog(context);
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
            cur_web_method = postMethod;
            return RestMethods.WSPost(jsonParam, postMethod);
        }

        @Override
        protected void onPostExecute(String postResult) {
            super.onPostExecute(postResult);
            dismissProgressDialog();

            if (postResult != null) {
                Log.e("zzz", postResult.toString());
                Log.d("result", "Department");
                Log.e("zzz", cur_web_method);

                ArrayList<String> sublist = new ArrayList<String>();

                try {
                    JSONArray jsonArray = new JSONArray(postResult);
                    for (int index = 0; index < jsonArray.length(); index++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(index);
                        sublist.add(jsonObject.getString("SUBJECT"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ArrayAdapter<String> subAdapter = new ArrayAdapter<String>(
                        getActivity(), android.R.layout.simple_spinner_item, sublist);
                subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spin_sub.setAdapter(subAdapter);


            } else {
                Toast.makeText(context, Constants.NODATA, Toast.LENGTH_LONG).show();
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


    //end now use this mathod in you activity
    public String uploadIt(String url, final JSONParser.UploadData data) {
        //String part
        try {
            //multipart entity part


            reqEntity = new AndroidMultiPartEntity(
                    new AndroidMultiPartEntity.ProgressListener() {

                        public void transferred(long num) {
                            data.doProgress((int) ((num / (float) totalSize) * 100));
                        }
                    });


            //// file uploading tassk
            File mediaFile;
            M.E("fileuri " + filePath);
            mediaFile = new File(filePath);
            reqEntity.addPart("userfile", new FileBody(mediaFile));
            totalSize = reqEntity.getContentLength();
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url);
                httppost.setEntity(reqEntity);
                HttpParams httpParameters = new BasicHttpParams();
                int timeoutConnection = 5000;
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
// Set the default socket timeout (SO_TIMEOUT)
// in milliseconds which is the timeout for waiting for data.
                int timeoutSocket = 5000;

                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                //Log.e("Responce string", response.toString());
                HttpEntity r_entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();
                // Log.e("Responce string", " " + statusCode);
                if (statusCode == 200) {
                    // Server response
                    String responseString = EntityUtils.toString(r_entity);
                    M.E(responseString);
                    return responseString;
                }
            } catch (Exception en) {
                try {
                    Log.e("Error", en.getMessage());
                } catch (NullPointerException nen) {
                    Log.e("Error", "Null" + nen.getMessage());
                }
            }
            return null;
        } catch (Exception e) {
            M.T(getActivity(), e.getMessage());
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == PICKFILE_REQUEST_CODE
                && resultCode == RESULT_OK && null != intent) {
            filePath = intent.getDataString();
            M.E(filePath);
        }
    }

    public String getSelectedPath(Intent data) {

        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        // close cursor
        cursor.close();

        // create copy of image with timestamp
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File localFileStroageLocName = new File(
                Constants.LOCAL_FILE_STORAGE_PATH + File.separator + "IMAGE_"
                        + timeStamp + ".jpg");
        try {

            FileOutputStream fileOutputStream = new FileOutputStream(
                    localFileStroageLocName);
            fileOutputStream.write(bytes.toByteArray());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return localFileStroageLocName.getPath();
    }

    public String getCapturedImagePath(Intent intent) {
        Bundle bundle = intent.getExtras();
        Bitmap thumbnail = (Bitmap) bundle.get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File localFileStroageLocName = new File(
                Constants.LOCAL_FILE_STORAGE_PATH + File.separator + "IMAGE_"
                        + timeStamp + ".jpg");
        try {

            FileOutputStream fileOutputStream = new FileOutputStream(
                    localFileStroageLocName);
            fileOutputStream.write(bytes.toByteArray());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return localFileStroageLocName.getPath();
    }

    private void showProgressDialog() {
        progDialog = new ProgressDialog(getActivity());
        progDialog.setMessage("Uploading...");
        progDialog.setIndeterminate(true);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setCancelable(false);
        progDialog.show();
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

    private void callHandler(String photoUploadResult, String filePath) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("filepath", filePath);
        bundle.putString("photoUploadResult", photoUploadResult);
        message.setData(bundle);
        ftpFileUploadHandler.sendMessage(message);
    }

    final Handler ftpFileUploadHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message handlerMsg) {

            Bundle bundle = handlerMsg.getData();
            String loacalMessagePath = bundle.getString("filepath");
            String photoUploadResult = bundle.getString("photoUploadResult");

            if (photoUploadResult.equals(Constants.FILE_TRANS_COMPLETE)) {

                // JSONObject jsonObject = new JSONObject();
                File file = new File(loacalMessagePath);

//                showToastMsg("File Uploaded");
//                txt_preview.setText("file uploaded");
                Log.d("ddd", "file uploaded");
                dismissProgressDialog();

                //after succesful image uploading webservice is calling
                JSONObject param = new JSONObject();
                try {
                    param.put("IDNO", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
                    param.put("PWD", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd));
                    param.put("DEPTNAME", spin_dept.getSelectedItem().toString());
                    param.put("SUBJECT", spin_sub.getSelectedItem().toString());
                    param.put("MESSAGE", ed_msg.getText().toString());
                    param.put("ATTACHMENT", "" + img_name);
                    param.put("SITE", "Fun2sh");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (ConnectionDetector.isConnected(getActivity())) {
                    new HelpTask(getActivity(), Constants.HELP_NEW).execute(param);
                } else {
                    M.T(getActivity(), Constants.NO_INTERNET);
                }

            } else {
                dismissProgressDialog();
                showToastMsg(Constants.PHOTO_UPLOAD_FAILED);
                Log.d("ddd", "file upload failed");
            }// end of handleMessage
        }
    };

    public void showToastMsg(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private class MyTransferListener implements FTPDataTransferListener {

        String filePath;

        public MyTransferListener(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void started() {
//			Logger.log("ftp started called");
        }

        @Override
        public void transferred(int arg0) {
//			Logger.log("ftp transferred called");
        }

        @Override
        public void completed() {
//		Logger.log("ftp completed called");
            callHandler(Constants.FILE_TRANS_COMPLETE, filePath);
        }

        @Override
        public void aborted() {
//			Logger.log("ftp aborted called");
        }

        @Override
        public void failed() {
//			Logger.log("ftp failed called");
            callHandler(Constants.FILE_TRANS_FAILED, filePath);
        }// MyTransferListener
    }

    public void showDialogWithItems(String[] items, String title,
                                    DialogInterface.OnClickListener listener) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(title);
        alertDialog.setItems(items, listener);
        alertDialog.show();

    }
}
