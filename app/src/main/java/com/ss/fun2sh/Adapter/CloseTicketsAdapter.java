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
public class CloseTicketsAdapter extends BaseAdapter {
    Context context;
    int size;
    HashMap<String,ArrayList<String>> close_map=new  HashMap<String,ArrayList<String>>();

    public CloseTicketsAdapter(Context context,int size,HashMap close_map) {
        this.context = context;
        this.size=size;
        this.close_map=close_map;
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
        convertView = mInflater.inflate(R.layout.close_tickets_list_row, null);
        //  holder = new ViewHolder();
        //  holder.txtDesc = (TextView) convertView.findViewById(R.id.desc);

        TextView close_tkt_sno= (TextView) convertView.findViewById(R.id.close_tkt_sno);
        TextView close_tkt_num= (TextView) convertView.findViewById(R.id.close_tkt_num);
        TextView close_tkt_date= (TextView) convertView.findViewById(R.id.close_tkt_date);
        TextView close_tkt_sub= (TextView) convertView.findViewById(R.id.close_tkt_sub);
        TextView close_tkt_resp= (TextView) convertView.findViewById(R.id.close_tkt_resp);



        Log.d("ddd",""+close_map.size());

        ArrayList<String> close_tktslno_list = close_map.get(Constants.TKTSLNO);
        ArrayList<String> close_tktno_list = close_map.get(Constants.TKTNO);
        ArrayList<String> close_lastupdate_list = close_map.get(Constants.LASTUPDATE);
        ArrayList<String> close_sub_list = close_map.get(Constants.SUBJECT);
        ArrayList<String> close_resawait_list = close_map.get(Constants.RESAWAIT);
        ArrayList<String> close_deptname_list = close_map.get(Constants.DEPTNAM);

//        Log.d("ddd",""+close_tktslno_list.get(0));
//        Log.d("ddd",""+close_tktno_list.get(0));
//        Log.d("ddd",""+close_lastupdate_list.get(0));
//        Log.d("ddd",""+close_sub_list.get(0));
//        Log.d("ddd",""+close_resawait_list.get(0));
//        Log.d("ddd",""+close_resawait_list.get(0));


        close_tkt_sno.setText("S.No. "+position+1);

        close_tkt_num.setText(close_tktno_list.get(position).toString());
        close_tkt_date.setText(close_lastupdate_list.get(position).toString());
        close_tkt_sub.setText(close_sub_list.get(position).toString());
        close_tkt_resp.setText(close_resawait_list.get(position).toString());

//        close_tkt_num.setText("1");
//        close_tkt_date.setText("1");
//        close_tkt_sub.setText("1");
//        close_tkt_resp.setText("1");


        return convertView;
    }
}
