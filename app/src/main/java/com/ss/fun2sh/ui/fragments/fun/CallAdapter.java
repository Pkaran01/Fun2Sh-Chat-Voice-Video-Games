package com.ss.fun2sh.ui.fragments.fun;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.HexagonImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Call;
import com.ss.fun2sh.CRUD.Utility;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.profile.UserProfileActivity;
import com.ss.fun2sh.utils.image.ImageLoaderUtils;

import java.util.List;


public class CallAdapter extends RecyclerView.Adapter<CallAdapter.DataObjectHolder> {
    private List<Call> callList;
    Context context;

    public class DataObjectHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView callTime;
        HexagonImageView profile_image;
        ImageView audio_call;
        ImageView callType;

        public DataObjectHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.username);
            profile_image = (HexagonImageView) itemView.findViewById(R.id.profile_image);
            audio_call = (ImageView) itemView.findViewById(R.id.audio_call);
            callType = (ImageView) itemView.findViewById(R.id.callType);
            callTime = (TextView) itemView.findViewById(R.id.callTime);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isFriend = DataManager.getInstance().getFriendDataManager().existsByUserId(callList.get(getAdapterPosition()).getUser().getUserId());
                    if (isFriend) {
                        UserProfileActivity.start(context, callList.get(getAdapterPosition()).getUser().getUserId());
                    }
                }
            });
        }
    }


    public CallAdapter(Context context, List<Call> callList) {
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
        holder.username.setText(callList.get(position).getUser().getFullName());
        holder.callTime.setText(Utility.getTimeAgo(callList.get(position).getCreatedDate() * 1000));
        displayAvatarImage(callList.get(position).getUser().getAvatar(), holder.profile_image);
        if (callList.get(position).getCallType() == 1) {
            holder.audio_call.setImageResource(R.drawable.ic_videocam_black_24dp);
        } else {
            holder.audio_call.setImageResource(R.drawable.ic_call_black_24dp);
        }
        if (callList.get(position).getStatus() == 0) {
            //incoming call
            holder.callType.setImageResource(R.drawable.incoming);
        } else if (callList.get(position).getStatus() == 1) {
            //outgoing call
            holder.callType.setImageResource(R.drawable.outgoing);
        } else {
            // missed call
            holder.callType.setImageResource(R.drawable.missed_call);
        }
    }


    @Override
    public int getItemCount() {
        return callList.size();
    }

    protected void displayAvatarImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS);
    }

}
