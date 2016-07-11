package com.ss.fun2sh.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ss.fun2sh.R;

/**
 * Created by rahul on 09-Jun-16.
 */
public class NewTicketsAdapter extends BaseAdapter {
    Context context;

    public NewTicketsAdapter(Context context) {
        this.context = context;

    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {

        TextView txtTitle;
        TextView txtDesc;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        //  if (convertView == null) {
        convertView = mInflater.inflate(R.layout.newtickets_list_row, null);
        //  holder = new ViewHolder();
        //  holder.txtDesc = (TextView) convertView.findViewById(R.id.desc);

        return convertView;
    }
}
