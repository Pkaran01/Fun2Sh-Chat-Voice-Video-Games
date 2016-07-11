package com.ss.fun2sh.ui.fragments.fun;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.siyamed.shapeimageview.HexagonImageView;
import com.ss.fun2sh.R;

import java.util.List;


public class CallAdapter extends RecyclerView.Adapter<CallAdapter.DataObjectHolder> {
    private List<String> callList;
    private static MyClickListener myClickListener;
    Context context;

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView username;
        HexagonImageView profile_image;
        ImageView audio_call;

        public DataObjectHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.username);
            profile_image = (HexagonImageView) itemView.findViewById(R.id.profile_image);
            audio_call = (ImageView) itemView.findViewById(R.id.audio_call);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getPosition(), v);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public CallAdapter(Context context, List<String> callList) {
        this.context = context;
        this.callList = callList;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.call_row, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, final int position) {
        holder.username.setText(callList.get(position));
        holder.profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, callList.get(position) + " image click", Toast.LENGTH_LONG).show();
            }
        });

        holder.audio_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, callList.get(position) + " Call click", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return callList.size();
    }

    public interface MyClickListener {
        public void onItemClick(int position, View v);
    }

}

