
package com.ss.fun2sh.ui.adapters.chats;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.siyamed.shapeimageview.HexagonImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.commands.chat.QBDeleteChatCommand;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBPrivateChatHelper;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DbUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;
import com.ss.fun2sh.CRUD.Utility;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.base.BaseActivity;
import com.ss.fun2sh.ui.activities.chats.GroupDialogActivity;
import com.ss.fun2sh.ui.activities.chats.PrivateDialogActivity;
import com.ss.fun2sh.utils.image.ImageLoaderUtils;

import java.util.ArrayList;
import java.util.List;


public class DialogsListAdapter extends RecyclerView.Adapter<DialogsListAdapter.ViewHolder>  {
    private DataManager dataManager;
    BaseActivity activity;
    List<Dialog> objectsList;
    protected Resources resources;

    protected QBUser currentUser;
    protected QBPrivateChatHelper privateChatHelper;
    private QBUser qbUser;

    public DialogsListAdapter(BaseActivity baseActivity, List<Dialog> objectsListCon) {
        dataManager = DataManager.getInstance();
        this.activity = baseActivity;
        this.objectsList = objectsListCon;
        resources = activity.getResources();
        currentUser = AppSession.getSession().getUser();
        privateChatHelper = activity.getPrivateChatHelper();
        qbUser = AppSession.getSession().getUser();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dialog, parent, false);

        ViewHolder dataObjectHolder = new ViewHolder(view,activity);

        return dataObjectHolder;

    }

    public boolean isEmpty() {
        return objectsList.size() == 0;
    }


    @Override
    public void onBindViewHolder(final DialogsListAdapter.ViewHolder holder, final int position) {


        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager().getDialogOccupantsListByDialogId(
                objectsList.get(position).getDialogId());
        User opponentUser = ChatUtils.getOpponentFromPrivateDialog(
                UserFriendUtils.createLocalUser(currentUser), dialogOccupantsList);
        if (Dialog.Type.PRIVATE.equals(objectsList.get(position).getType())) {
            if (opponentUser.getFullName() != null) {
                holder.nameTextView.setText(Utility.capitalize(opponentUser.getFullName()));
                displayAvatarImage(opponentUser.getAvatar(), holder.avatarImageView);
            } else {
                holder.nameTextView.setText(resources.getString(R.string.deleted_user));
                dataManager.getDialogDataManager().deleteById(objectsList.get(position).getDialogId());
            }
        } else {
            holder.nameTextView.setText(objectsList.get(position).getTitle());
            holder.avatarImageView.setImageResource(R.drawable.placeholder_group);
            displayGroupPhotoImage(objectsList.get(position).getPhoto(), holder.avatarImageView);
        }

        List<Long> dialogOccupantsIdsList = ChatUtils.getIdsFromDialogOccupantsList(dialogOccupantsList);
        long unreadMessages = dataManager.getMessageDataManager().getCountUnreadMessages(dialogOccupantsIdsList, currentUser.getId());
        long unreadDialogNotifications = dataManager.getDialogNotificationDataManager().getCountUnreadDialogNotifications(dialogOccupantsIdsList, currentUser.getId());

        long totalCount = unreadMessages + unreadDialogNotifications;

        if (totalCount > ConstsCore.ZERO_INT_VALUE) {
            holder.unreadMessagesTextView.setText(totalCount + ConstsCore.EMPTY_STRING);
            holder.unreadMessagesTextView.setVisibility(View.VISIBLE);
        } else {
            holder.unreadMessagesTextView.setVisibility(View.GONE);
        }

        Message message = dataManager.getMessageDataManager().getLastMessageWithTempByDialogId(dialogOccupantsIdsList);
        DialogNotification dialogNotification = dataManager.getDialogNotificationDataManager().getLastDialogNotificationByDialogId(dialogOccupantsIdsList);
        if (Dialog.Type.PRIVATE.equals(objectsList.get(position).getType())) {
            holder.lastMessageTextView.setText(
                    ChatUtils.getDialogLastMessage(resources.getString(R.string.cht_contact_request_received), message, dialogNotification));
        } else {
            holder.lastMessageTextView.setText(
                    ChatUtils.getDialogLastMessage(resources.getString(R.string.cht_notification_message), message, dialogNotification));
        }

        holder.timeTextView.setText(Utility.getTimeAgo(ChatUtils.getDialogMessageCreatedDate(true, message, dialogNotification) * 1000));

        holder.chat_lin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChat(position);
            }
        });

        holder.chat_lin.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (activity.checkNetworkAvailableWithError()) {
                    new MaterialDialog.Builder(activity)
                            .items(R.array.deleteDilaog)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    if (which == 0) {
                                        Dialog mdialog = objectsList.get(position);
                                        deleteDialog(mdialog);
                                    }
                                }
                            })
                            .show();
                }
                return true;
            }
        });

    }



    @Override
    public int getItemCount() {
        return objectsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public HexagonImageView avatarImageView;
        public TextView nameTextView;
        public TextView lastMessageTextView;
        public TextView unreadMessagesTextView;
        public TextView timeTextView;
        public LinearLayout chat_lin;
        Context context;

        public ViewHolder(View drawerItem, Context context) {

            super(drawerItem);
            this.context = context;
            avatarImageView = (HexagonImageView) itemView.findViewById(R.id.avatar_imageview);
            nameTextView = (TextView) itemView.findViewById(R.id.name_textview);
            lastMessageTextView = (TextView) itemView.findViewById(R.id.last_message_textview);
            timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
            unreadMessagesTextView = (TextView) itemView.findViewById(
                    R.id.unread_messages_textview);
            chat_lin=(LinearLayout)itemView.findViewById(R.id.lin_chat);
        }






    }

    protected void displayAvatarImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS);
    }

    protected void displayGroupPhotoImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, ImageLoaderUtils.UIL_GROUP_AVATAR_DISPLAY_OPTIONS);
    }

    void startChat(int position) {
        Dialog dialog = objectsList.get(position);

        if (!activity.checkNetworkAvailableWithError() && isFirstOpeningDialog(dialog.getDialogId())) {
            return;
        }

        if (dialog.getType() == Dialog.Type.PRIVATE) {
            startPrivateChatActivity(dialog);
        } else {
            startGroupChatActivity(dialog);
        }
    }

    private void startPrivateChatActivity(Dialog dialog) {
        List<DialogOccupant> occupantsList = dataManager.getDialogOccupantDataManager()
                .getDialogOccupantsListByDialogId(dialog.getDialogId());
        User opponent = ChatUtils.getOpponentFromPrivateDialog(UserFriendUtils.createLocalUser(qbUser), occupantsList);

        if (!TextUtils.isEmpty(dialog.getDialogId())) {
            PrivateDialogActivity.start(activity, opponent, dialog);
        }
    }

    private void startGroupChatActivity(Dialog dialog) {
        GroupDialogActivity.start(activity, dialog);
    }

    private boolean isFirstOpeningDialog(String dialogId) {
        return !dataManager.getMessageDataManager().getTempMessagesByDialogId(dialogId).isEmpty();
    }

    private void deleteDialog(Dialog dialog) {
        if (Dialog.Type.PRIVATE.equals(dialog.getType())) {
            if (privateChatHelper != null) {
                 List<Integer> occupantsIdsList = new ArrayList<>();
                occupantsIdsList.add(qbUser.getId());
                DbUtils.deleteDialogLocal(dataManager, dialog.getDialogId());
            }
        }
        QBDeleteChatCommand.start(activity, dialog.getDialogId(), dialog.getType());
    }

    public void setFilter(List<Dialog> newData) {
        objectsList.clear();
        objectsList.addAll(newData);
        notifyDataSetChanged();
    }

    public void setNewData(List<Dialog> newData) {
        objectsList = newData;
        notifyDataSetChanged();
    }

}