package com.ss.fun2sh.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ss.fun2sh.R;


public class NavigationDrawerAdapter extends BaseAdapter {
    Context context;
    String data[];

    public NavigationDrawerAdapter(Context context, String data[]) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.length;
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

        TextView navtitle;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        convertView = mInflater.inflate(R.layout.navigation_drawer_row, null);
        holder.navtitle = (TextView) convertView.findViewById(R.id.navtitle);
        holder.navtitle.setText(data[position]);

        return convertView;
    }
}