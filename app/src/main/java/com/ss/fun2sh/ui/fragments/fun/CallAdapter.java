package com.ss.fun2sh.ui.fragments.fun;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.siyamed.shapeimageview.HexagonImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate_core.qb.commands.chat.QBDeleteChatCommand;
import com.quickblox.q_municate_core.qb.helpers.QBCallChatHelper;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConnectivityUtils;
import com.quickblox.q_municate_core.utils.DbUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Call;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.CRUD.Utility;
import com.ss.fun2sh.R;
import com.ss.fun2sh.oldutils.Constants;
import com.ss.fun2sh.ui.activities.base.BaseActivity;
import com.ss.fun2sh.ui.activities.groupcall.activities.GroupCallActivity;
import com.ss.fun2sh.ui.activities.profile.UserProfileActivity;
import com.ss.fun2sh.utils.ToastUtils;
import com.ss.fun2sh.utils.image.ImageLoaderUtils;

import java.util.ArrayList;
import java.util.List;


public class CallAdapter extends RecyclerView.Adapter<CallAdapter.DataObjectHolder> {
    private List<Call> callList;
    BaseActivity activity;
    //  protected QBCallChatHelper callChatHelper;
    private DataManager dataManager;

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
                        if (!dataManager.getUserDataManager().isBlocked(callList.get(getAdapterPosition()).getUser().getUserId())) {

                            if (callList.get(getAdapterPosition()).getCallType() == 1) {
                                callToUser(DataManager.getInstance().getUserDataManager().get(callList.get(getAdapterPosition()).getUser().getUserId()), QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);
                                CoreSharedHelper.getInstance().savePref(Constants.VIDEOONETOONECALL, "true");
                            } else {
                                callToUser(DataManager.getInstance().getUserDataManager().get(callList.get(getAdapterPosition()).getUser().getUserId()), QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO);
                                CoreSharedHelper.getInstance().savePref(Constants.AUDIOONETOONECALL, "true");
                            }
                        }
                        else {
                            Utility.blockContactMessage(activity, "Unblock " + callList.get(getAdapterPosition()).getUser().getFullName().toString() + " to place a FunChat video call", callList.get(getAdapterPosition()).getUser().getUserId());
                        }


                      //  UserProfileActivity.start(activity, callList.get(getAdapterPosition()).getUser().getUserId(),DataManager.getInstance().getUserDataManager().get(callList.get(getAdapterPosition()).getUser().getUserId()));
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (activity.checkNetworkAvailableWithError()) {
                        new MaterialDialog.Builder(activity)
                                .items(R.array.removeCallLog)
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                        if (which == 0) {
                                            Call call = callList.get(getAdapterPosition());
                                            deleteDialog(call);
                                        }
                                    }
                                })
                                .show();
                    }
                    return true;
                }
            });
        }
    }


    private void callToUser(User user, QBRTCTypes.QBConferenceType qbConferenceType) {
  /*      if (!activity.isChatInitializedAndUserLoggedIn()) {
            ToastUtils.longToast(R.string.call_chat_service_is_initializing);
            return;
        }*/
        List<QBUser> qbUserList = new ArrayList<>(1);
        qbUserList.add(UserFriendUtils.createQbUser(user));
        GroupCallActivity.start(activity, qbUserList, qbConferenceType, null);

    }


    private void deleteDialog(Call call) {

        DataManager.getInstance().getCallDataManager().deleteByCallId(call.getCallId());
        int position = callList.indexOf(call);
        if (position != -1) {
            callList.remove(call);
            notifyItemRemoved(position);
        }
    }

    public CallAdapter(BaseActivity activity, List<Call> callList) {
        this.activity = activity;
        this.callList = callList;
        dataManager = DataManager.getInstance();
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
        User user = DataManager.getInstance().getUserDataManager().get(callList.get(position).getUser().getUserId());
        holder.username.setText(user.getFullName());
        holder.callTime.setText(Utility.getTimeAgo(callList.get(position).getCreatedDate() * 1000));
        displayAvatarImage(user.getAvatar(), holder.profile_image);
        if (callList.get(position).getCallType() == 1) {
            holder.audio_call.setImageResource(R.drawable.ic_videocam_black_24dp);
        } else {
            holder.audio_call.setImageResource(R.drawable.ic_call_black_24dp);
        }
        if (callList.get(position).getStatus() == 1) {
            //incoming call blue
            holder.username.setTextColor(Color.parseColor("#0048ff"));
            holder.callType.setImageResource(R.drawable.incoming);
        } else if (callList.get(position).getStatus() == 2) {
            //outgoing call green
            holder.callType.setImageResource(R.drawable.outgoing);
            holder.username.setTextColor(Color.parseColor("#1cba48"));
        } else if (callList.get(position).getStatus() == 3) {
            // missed call rad
            holder.callType.setImageResource(R.drawable.missed_call);
            holder.username.setTextColor(Color.parseColor("#ff0000"));
        } else if (callList.get(position).getStatus() == 4) {
            holder.callType.setImageResource(R.drawable.call_faild);
            holder.username.setTextColor(Color.parseColor("#800080"));
        }
    }


    @Override
    public int getItemCount() {
        return callList.size();
    }

    public void setFilter(List<Call> newData) {
        callList.clear();
        callList.addAll(newData);
        notifyDataSetChanged();
    }

    protected void displayAvatarImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS);
    }

}

