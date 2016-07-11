package com.ss.fun2sh.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ss.fun2sh.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class NotificationActivity extends AppCompatActivity {
    TextView backtolistbtn,txt_notification,txt_issuecode,txt_issuedate,txt_sub,txt_msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        backtolistbtn = (TextView) findViewById(R.id.backtolistbtn);
        txt_notification = (TextView) findViewById(R.id.txt_notification);
        txt_issuecode = (TextView) findViewById(R.id.txt_issuecode);
        txt_issuedate = (TextView) findViewById(R.id.txt_issuedate);
        txt_sub = (TextView) findViewById(R.id.txt_sub);
        txt_msg = (TextView) findViewById(R.id.txt_msg);



        Bundle b=this.getIntent().getExtras();
//        HashMap<String,ArrayList<String>> map=b.getSerializable("not_map");

        HashMap<String,ArrayList<String>> map1= (HashMap<String, ArrayList<String>>) b.get("not_map");
        Log.d("ddd","size=="+map1.size());

        String str=map1.get("SUBJECT").get(0).toString();

//        b.getString("ATTCHEMENT");
//        b.getString("ISSBY");
//        b.getString("ISSCODE");
//        b.getString("ISSON");
//        b.getString("MESSAGE");
//        b.getString("SUBJECT");

        Log.d("ddd",b.getString("ATTCHEMENT"));
        Log.d("ddd",b.getString("ISSBY"));
        Log.d("ddd",b.getString("ISSCODE"));
        Log.d("ddd",b.getString("ISSON"));
        Log.d("ddd",b.getString("MESSAGE"));
        Log.d("ddd",b.getString("SUBJECT"));

        Log.d("ddd",str);

        txt_notification.setText("Notification/"+b.getString("ISSCODE"));
        txt_issuecode.setText(b.getString("ISSCODE"));

        String myDate=b.getString("ISSON");
        String st=myDate.split(" ")[0].toString();

        txt_issuedate.setText(st);
        txt_sub.setText("Subject : "+b.getString("SUBJECT"));
        txt_msg.setText(Html.fromHtml(b.getString("MESSAGE")));

        backtolistbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
