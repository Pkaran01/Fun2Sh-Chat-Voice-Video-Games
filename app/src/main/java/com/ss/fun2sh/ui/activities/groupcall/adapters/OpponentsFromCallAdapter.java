package com.ss.fun2sh.ui.activities.groupcall.adapters;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBPeerChannel;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.ss.fun2sh.R;
import com.ss.fun2sh.oldutils.Constants;
import com.ss.fun2sh.ui.activities.groupcall.utils.QBRTCSessionUtils;
import com.ss.fun2sh.utils.image.ImageLoaderUtils;
import com.ss.fun2sh.utils.image.ImageUtils;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * QuickBlox team
 */
public class OpponentsFromCallAdapter extends RecyclerView.Adapter<OpponentsFromCallAdapter.ViewHolder> {

    private static final String TAG = OpponentsFromCallAdapter.class.getSimpleName();
    private final int itemHeight;
    private final int itemWidth;
    private int paddingLeft = 0;

    private Context context;
    private final QBRTCSession qbrtcSession;
    private List<QBUser> opponents;
    private int gridWidth;
    private boolean showVideoView;
    private LayoutInflater inflater;
    private int columns;

    public OpponentsFromCallAdapter(Context context, QBRTCSession qbrtcSession,
                                    List<QBUser> users, int width, int height,
                                    int gridWidth, int columns, int itemMargin,
                                    boolean showVideoView) {
        this.context = context;
        this.qbrtcSession = qbrtcSession;
        this.opponents = users;
        this.gridWidth = gridWidth;
        this.columns = columns;
        this.showVideoView = showVideoView;
        this.inflater = LayoutInflater.from(context);
        itemWidth = width;
        if (showVideoView) {
            itemHeight = 150;
        } else {
            itemHeight = 150;
        }
        //   setPadding(itemMargin);

        Log.e(TAG, "item width=" + itemWidth + ", item height=" + itemHeight);
    }

    private void setPadding(int itemMargin) {
        int allCellWidth = (itemWidth + (itemMargin * 2)) * columns;
        if ((allCellWidth < gridWidth) && ((gridWidth - allCellWidth) > (itemMargin * 2))) { //set padding if it makes sense to do it
            paddingLeft = (gridWidth - allCellWidth) / 2;
        }
    }

    @Override
    public int getItemCount() {
       /* return opponents.size();*/
        return 4;
    }

    public Integer getItem(int position) {

        return opponents.get(position).getId();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.group_list_item_opponent_from_call, null);
        //   v.findViewById(R.id.innerLayout).setLayoutParams(new FrameLayout.LayoutParams(itemWidth, itemHeight));
        //  if (paddingLeft != 0) {
        //   v.setPadding(0, 0, 0, 0);
        //   }

        ViewHolder vh = new ViewHolder(v);

        SurfaceViewRenderer opponentView = (SurfaceViewRenderer) v.findViewById(R.id.opponentView);

        //  Log.e("opponents ki values-=", opponents.toString());
        updateVideoView(opponentView, false);
        vh.showOpponentView(showVideoView);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        try {
            final QBUser user = opponents.get(position);

            if (!showVideoView) {
                if (CoreSharedHelper.getInstance().getPref(Constants.USERPICAUDIOPREF).toString().equalsIgnoreCase("true")) {
                    holder.userimagetView.setVisibility(View.VISIBLE);

                    if (DataManager.getInstance().getUserDataManager().get(opponents.get(position).getId()).getAvatar().toString().trim().equalsIgnoreCase("")) {
                        holder.userimagetView.setBackgroundResource(R.drawable.placeholder_user);
                    } else {
                        loadusersLogo(DataManager.getInstance().getUserDataManager().get(opponents.get(position).getId()).getAvatar(), holder.userimagetView);
                    }
                }
            }

            holder.opponentsName.setText(user.getFullName());
            holder.setUserId(user.getId());
            QBPeerChannel peerChannel = qbrtcSession.getPeerChannel(user.getId());
            Integer statusRes = QBRTCSessionUtils.getStatusDescriptionReosuurce(
                    QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED == peerChannel.getState() ?
                            peerChannel.getDisconnectReason() : peerChannel.getState());
            if (statusRes == null) {
                statusRes = R.string.unDefined;
            }
            Log.e("statusRes**-", " " + statusRes);
            holder.opponentsName.setVisibility(View.GONE);
            holder.setStatus(context.getString(statusRes));
        } catch (IndexOutOfBoundsException e) {
            holder.opponentsName.setVisibility(View.VISIBLE);
            holder.opponentsName.setText("User is Not Available");
        }
    }

    protected void loadusersLogo(String logoUrl, final ImageView userimageView) {
        ImageLoader.getInstance().loadImage(logoUrl, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedBitmap) {
                        userimageView.setImageBitmap(loadedBitmap);
                    }
                });
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    protected void updateVideoView(SurfaceViewRenderer surfaceViewRenderer, boolean mirror) {
        surfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        surfaceViewRenderer.setMirror(mirror);
        surfaceViewRenderer.requestLayout();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView opponentsName;
        TextView connectionStatus;
        SurfaceViewRenderer opponentView;
        private int userId;
        ImageView userimagetView;

        public ViewHolder(View itemView) {
            super(itemView);
            opponentsName = (TextView) itemView.findViewById(R.id.opponentName);
            connectionStatus = (TextView) itemView.findViewById(R.id.connectionStatus);
            opponentView = (SurfaceViewRenderer) itemView.findViewById(R.id.opponentView);
            userimagetView = (ImageView) itemView.findViewById(R.id.userimagetView);
        }

        public void setStatus(String status) {
            connectionStatus.setText(status);
        }

        public TextView getConnectionStatus() {
            return connectionStatus;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getUserId() {
            return userId;
        }

        public TextView getOpponentsName() {
            return opponentsName;
        }

        public SurfaceViewRenderer getOpponentView() {
            return opponentView;
        }

        public void showOpponentView(boolean show) {
            opponentView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
