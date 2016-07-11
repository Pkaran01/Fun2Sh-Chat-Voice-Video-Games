package com.ss.fun2sh.ui.fragments.fun;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ss.fun2sh.R;

import java.util.List;

/**
 * Created by tbwebnet on 23/4/16.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder> {

private List<String> contactList;

public class MyViewHolder extends RecyclerView.ViewHolder {
    public TextView username;

    public MyViewHolder(View view) {
        super(view);
        username = (TextView) view.findViewById(R.id.username);
    }
}


    public ContactsAdapter(List<String> contactList) {
        this.contactList = contactList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.username.setText(contactList.get(position).toString());
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }
}