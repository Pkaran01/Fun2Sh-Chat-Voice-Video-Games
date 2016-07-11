package com.ss.fun2sh.ui.fragments.fun;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ss.fun2sh.R;

import java.util.List;

/**
 * Created by rahul on 02-May-16.
 */
public class Announcementadapter extends RecyclerView.Adapter<Announcementadapter.MyViewHolder> {

    private List<String> favList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView announcement;

        public MyViewHolder(View view) {
            super(view);
            announcement = (TextView) view.findViewById(R.id.announcement);
        }
    }


    public Announcementadapter(List<String> favList) {
        this.favList = favList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.announce_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.announcement.setText(favList.get(position).toString());
    }

    @Override
    public int getItemCount() {
        return favList.size();
    }
}