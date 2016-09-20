package com.ss.fun2sh.ui.adapters.chats;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.ss.fun2sh.ui.adapters.base.BaseListAdapter;
import com.ss.fun2sh.utils.image.ImageLoaderUtils;

import java.util.ArrayList;
import java.util.List;

public class GroupDialogsListAdapter extends RecyclerView.Adapter<GroupDialogsListAdapter.ViewHolder>  {

    private DataManager dataManager;
    BaseActivity activity;
    List<Dialog> objectsList;
    protected Resources resources;

    protected QBUser currentUser;
    protected QBGroupChatHelper groupChatHelper;
    private QBUser qbUser;

    public GroupDialogsListAdapter(BaseActivity baseActivity, List<Dialog> objectsListCon) {
        dataManager = DataManager.getInstance();
        this.activity = baseActivity;
        this.objectsList = objectsListCon;
        resources = activity.getResources();
        currentUser = AppSession.getSession().getUser();
        groupChatHelper = activity.getGroupChatHelper();
        qbUser = AppSession.getSession().getUser();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dialog, parent, false);

        ViewHolder dataObjectHolder = new ViewHolder(view,activity);

        return dataObjectHolder;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager().getDialogOccupantsListByDialogId(
                objectsList.get(position).getDialogId());
        holder.nameTextView.setText(objectsList.get(position).getTitle());
        holder.avatarImageView.setImageResource(R.drawable.placeholder_group);
        displayGroupPhotoImage(objectsList.get(position).getPhoto(), holder.avatarImageView);
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

        holder.lastMessageTextView.setText(
                ChatUtils.getDialogLastMessage(resources.getString(R.string.cht_notification_message), message, dialogNotification));
        holder.timeTextView.setText(Utility.getTimeAgo(ChatUtils.getDialogMessageCreatedDate(true, message, dialogNotification) * 1000));

        holder.chat_lin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!activity.checkNetworkAvailableWithError() && isFirstOpeningDialog(objectsList.get(position).getDialogId())) {
                    return;
                }

                if (objectsList.get(position).getType() == Dialog.Type.PRIVATE) {
                    startPrivateChatActivity(objectsList.get(position));
                } else {
                    startGroupChatActivity(objectsList.get(position));
                }
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

    private void deleteDialog(Dialog dialog) {
        if (Dialog.Type.GROUP.equals(dialog.getType())) {
            if (groupChatHelper != null) {
                try {
                    QBDialog localDialog = ChatUtils.createQBDialogFromLocalDialogWithoutLeaved(dataManager,
                            dataManager.getDialogDataManager().getByDialogId(dialog.getDialogId()));
                    List<Integer> occupantsIdsList = new ArrayList<>();
                    occupantsIdsList.add(qbUser.getId());
                    groupChatHelper.sendGroupMessageToFriends(
                            localDialog,
                            DialogNotification.Type.OCCUPANTS_DIALOG, occupantsIdsList, true);
                    DbUtils.deleteDialogLocal(dataManager, dialog.getDialogId());
                } catch (QBResponseException e) {
                    ErrorUtils.logError(e);
                }
            }
        }
        QBDeleteChatCommand.start(activity, dialog.getDialogId(), dialog.getType());
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

    protected void displayGroupPhotoImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, ImageLoaderUtils.UIL_GROUP_AVATAR_DISPLAY_OPTIONS);
    }

    public void setFilter(List<Dialog> newData) {
        objectsList.clear();
        objectsList.addAll(newData);
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return objectsList.size() == 0;
    }

     @Override
    public int getItemCount() {
        return objectsList.size();
    }

    public void setNewData(List<Dialog> newData) {
        objectsList = newData;
        notifyDataSetChanged();
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
            unreadMessagesTextView = (TextView) itemView.findViewById(
                    R.id.unread_messages_textview);
            timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
            chat_lin = (LinearLayout) itemView.findViewById(R.id.lin_chat);
        }


    }
}