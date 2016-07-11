package com.ss.fun2sh.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ss.fun2sh.R;

/**
 * Created by ajaybabup on 6/20/2016.
 */
public class NotificationAdapter extends ArrayAdapter {


    public NotificationAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);

//        holder.open_tkt_sno = (TextView) rowView.findViewById(R.id.open_tkt_sno);

    }
}
