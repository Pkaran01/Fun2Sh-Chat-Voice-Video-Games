package com.ss.fun2sh.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.ss.fun2sh.Adapter.SupportTicketsAdapter;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;
import com.ss.fun2sh.oldutils.ConnectionDetector;
import com.ss.fun2sh.oldutils.Constants;
import com.ss.fun2sh.oldutils.PostTask;
import com.ss.fun2sh.oldutils.WebserviceCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SupportTicketActivity extends AppCompatActivity implements WebserviceCallback{
    ListView suport_ticket_list;
    TextView replyticket, backtolistTV,txt_sub,txt_dept,txt_support_header;
    Context con=SupportTicketActivity.this;
    HashMap<String,ArrayList<String>> view_map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_ticket);

        suport_ticket_list = (ListView) findViewById(R.id.suport_ticket_list);
        replyticket = (TextView) findViewById(R.id.replyticket);
        txt_support_header= (TextView) findViewById(R.id.txt_support_header);

//        SupportTicketsAdapter supportTicketsAdapter = new SupportTicketsAdapter(this);
//        suport_ticket_list.setAdapter(supportTicketsAdapter);

        backtolistTV = (TextView) findViewById(R.id.backtolistTV);
        txt_sub= (TextView) findViewById(R.id.txt_sub);
        txt_dept= (TextView) findViewById(R.id.txt_dept);

        final Bundle b=this.getIntent().getExtras();

        txt_dept.setText(b.getString(Constants.DEPTNAM));
        txt_sub.setText(b.getString(Constants.SUBJECT));
        txt_support_header.setText("Support Tickets # "+b.getString(Constants.TKTNO));

        Log.d("ddd support",b.getString(Constants.TKTSLNO));
        Log.d("ddd",b.getString(Constants.DEPTNAM));
        Log.d("ddd",b.getString(Constants.SUBJECT));

        replyticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent suportintent = new Intent(SupportTicketActivity.this, ReplyActivity.class);

                Bundle bn=new Bundle();

                bn.putString(Constants.TKTSLNO,b.getString(Constants.TKTSLNO));
                bn.putString(Constants.TKTNO,b.getString(Constants.TKTNO));
                bn.putString(Constants.DEPTNAM,b.getString(Constants.DEPTNAM));
                bn.putString(Constants.SUBJECT,b.getString(Constants.SUBJECT));
                bn.putString("sel_method",b.getString("sel_method"));

                suportintent.putExtras(bn);
                startActivity(suportintent);

                SupportTicketActivity.this.finish();

            }
        });
        backtolistTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.TKTSLNO, b.getString(Constants.TKTSLNO));

            if (ConnectionDetector.isConnected(this)) {
                PostTask postTask = new PostTask(SupportTicketActivity.this,Constants.HELP_VIEW,SupportTicketActivity.this);
                postTask.execute(jsonObject);
            } else {
                M.T(this,Constants.NO_INTERNET);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postResult(String postResult, String postMethod) {

//        M.T(con,"postResult");
        Log.d("ddd support result",""+postResult);

        view_map=new  HashMap<String,ArrayList<String>>();
        ArrayList<String> support_attach_list = new ArrayList<String>();
        ArrayList<String> support_head_list = new ArrayList<String>();
        ArrayList<String> support_msg_list = new ArrayList<String>();

        JSONArray support_array = null;
        try {
            support_array=new JSONArray(postResult);
            Log.d("ddd","length of array--"+support_array.length());

            JSONObject open_jobj;
            for(int j=0;j<support_array.length();j++) {
                open_jobj = support_array.getJSONObject(j);

                support_attach_list.add(open_jobj.getString(Constants.ATTCHEMENT));
                support_head_list.add(open_jobj.getString(Constants.HEADING));
                support_msg_list.add(open_jobj.getString(Constants.MESSAGE));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        view_map.put(Constants.ATTACHMENT,support_attach_list);
        view_map.put(Constants.HEADING,support_head_list);
        view_map.put(Constants.MESSAGE,support_msg_list);

//                        ticketAdapter = new SupportTicketsAdapter(con,support_array.length(),view_map);
        SupportTicketsAdapter ticketAdapter = new SupportTicketsAdapter(con,support_array.length(),view_map);
        suport_ticket_list.setAdapter(ticketAdapter);
    }
}
