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
import com.ss.fun2sh.oldutils.Constants;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by rahul on 09-Jun-16.
 */
public class OpenTicketAdapter extends BaseAdapter {
    Context context;
    int size;
    HashMap<String,ArrayList<String>> open_map=new  HashMap<String,ArrayList<String>>();

    public OpenTicketAdapter(Context context,int size,HashMap open_map) {
        this.context = context;
        this.size=size;
        this.open_map=open_map;
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
        return position;
    }

    private class ViewHolder {

        TextView txtTitle;
        TextView txtDesc;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        //  if (convertView == null) {
        convertView = mInflater.inflate(R.layout.open_ticket_list_row, null);
        //  holder = new ViewHolder();
        //  holder.txtDesc = (TextView) convertView.findViewById(R.id.desc);

        TextView open_tkt_num= (TextView) convertView.findViewById(R.id.open_tkt_num);
        TextView open_tkt_date= (TextView) convertView.findViewById(R.id.open_tkt_date);
        TextView open_tkt_sub= (TextView) convertView.findViewById(R.id.open_tkt_sub);
        TextView open_tkt_resp= (TextView) convertView.findViewById(R.id.open_tkt_resp);
        TextView open_tkt_sno= (TextView) convertView.findViewById(R.id.open_tkt_sno);


        Log.d("ddd",""+open_map.size());

        ArrayList<String> open_tktslno_list = open_map.get(Constants.TKTSLNO);
        ArrayList<String> open_tktno_list = open_map.get(Constants.TKTNO);
        ArrayList<String> open_lastupdate_list = open_map.get(Constants.LASTUPDATE);
        ArrayList<String> open_sub_list = open_map.get(Constants.SUBJECT);
        ArrayList<String> open_resawait_list = open_map.get(Constants.RESAWAIT);
        ArrayList<String> open_deptnam_list = open_map.get(Constants.DEPTNAM);


//        Log.d("ddd",""+open_tktslno_list.get(0));
//        Log.d("ddd",""+open_tktno_list.get(0));
//        Log.d("ddd",""+open_lastupdate_list.get(0));
//        Log.d("ddd",""+open_sub_list.get(0));
//        Log.d("ddd",""+open_resawait_list.get(0));
//        Log.d("ddd",""+open_deptnam_list.get(0));



//        int pos=position+1;
        int poss=position;
        int pos=poss+1;
        open_tkt_sno.setText("S.No. "+pos);
        open_tkt_num.setText(open_tktno_list.get(position).toString());
        open_tkt_date.setText(open_lastupdate_list.get(position).toString());
        open_tkt_sub.setText(open_sub_list.get(position).toString());
        open_tkt_resp.setText(open_resawait_list.get(position).toString());

//        open_tkt_num.setText("1");
//        open_tkt_date.setText("1");
//        open_tkt_sub.setText("1");
//        open_tkt_resp.setText("1");


        return convertView;
    }
}
