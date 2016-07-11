package com.ss.fun2sh.Adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ss.fun2sh.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by rahul on 09-Jun-16.
 */
public class NotificationListAdapter extends BaseAdapter {
    Context context;
    int size;
    HashMap<String,ArrayList<String>> not_map=new  HashMap<String,ArrayList<String>>();

    public NotificationListAdapter(Context context, int size,HashMap not_map) {
        this.context = context;
        this.size = size;
        this.not_map=not_map;
    }

    @Override
    public int getCount() {
        return size;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    class Holder {

        TextView not_tkt_sno,not_tkt_date,not_tkt_sub,not_tkt_resp;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        convertView = mInflater.inflate(R.layout.notification_ticket_row, null);

        TextView not_tkt_sno= (TextView) convertView.findViewById(R.id.not_tkt_sno);
        TextView not_tkt_date= (TextView) convertView.findViewById(R.id.not_tkt_date);
        TextView not_tkt_sub= (TextView) convertView.findViewById(R.id.not_tkt_sub);
        TextView not_tkt_resp= (TextView) convertView.findViewById(R.id.not_tkt_resp);

        ArrayList<String> not_attach_list =  not_map.get("ATTACHMENT");
        ArrayList<String> not_issby_list =  not_map.get("ISSBY");
        ArrayList<String> not_isscode_list =  not_map.get("ISSCODE");
        ArrayList<String> not_isson_list =  not_map.get("ISSON");
        ArrayList<String> not_msg_list =  not_map.get("MESSAGE");
        ArrayList<String> not_sub_list =  not_map.get("SUBJECT");

//        not_tkt_sno.setText(not_isscode_list.get(0).toString());
//        not_tkt_date.setText(not_isson_list.get(0).toString());
//        not_tkt_sub.setText(not_sub_list.get(0).toString());
//        not_tkt_resp.setText(not_issby_list.get(0).toString());

        not_tkt_sno.setText(not_isscode_list.get(position).toString());
        not_tkt_date.setText(not_isson_list.get(position).toString());
        not_tkt_sub.setText(not_sub_list.get(position).toString());
        not_tkt_resp.setText(not_issby_list.get(position).toString());

        Log.d("ddd","isEmpty");
        Log.d("ddd","isEmpty"+not_map.isEmpty());
        Log.d("ddd","attach"+not_map.get("ATTCHEMENT"));
        Log.d("ddd","attach"+not_map.get("ATTCHEMENT").get(0));
//        Log.d("ddd","attach empty"+not_map.get("ATTACHMENT").isEmpty());
//        Log.d("ddd",""+not_map.get("ISSCODE"));
//        Log.d("ddd",""+not_map.get("ISSON"));
//        Log.d("ddd",""+not_map.get("MESSAGE"));
//        Log.d("ddd",""+not_map.get("SUBJECT"));

//        Log.d("not_attach_list",""+not_attach_list.size());
//        Log.d("not_issby_list",""+not_issby_list.size());
//        Log.d("not_isscode_list",""+not_isscode_list.size());
//        Log.d("not_isson_list",""+not_isson_list.size());
//        Log.d("not_msg_list",""+not_msg_list.size());
//        Log.d("not_sub_list",""+not_sub_list.size());

//        not_tkt_sno.setText("1");
//        not_tkt_date.setText("1");
//        not_tkt_sub.setText("1");
//        not_tkt_resp.setText("1");

//        not_tkt_sno= (TextView) convertView.findViewById(R.id.not_tkt_sno);
//        not_tkt_date= (TextView) convertView.findViewById(R.id.not_tkt_date);
//        not_tkt_sub= (TextView) convertView.findViewById(R.id.not_tkt_sub);
//        not_tkt_resp= (TextView) convertView.findViewById(R.id.not_tkt_resp);
//
//        not_tkt_sno.setText("1");
//        not_tkt_date.setText("1");
//        not_tkt_sub.setText("1");
//        not_tkt_resp.setText("1");

        return convertView;
    }
}