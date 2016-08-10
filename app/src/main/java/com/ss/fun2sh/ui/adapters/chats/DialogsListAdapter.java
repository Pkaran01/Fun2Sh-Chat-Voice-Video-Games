
package com.ss.fun2sh.ui.adapters.chats;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.HexagonImageView;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.User;
import com.ss.fun2sh.CRUD.Utility;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.base.BaseActivity;
import com.ss.fun2sh.ui.adapters.base.BaseListAdapter;

import java.util.ArrayList;
import java.util.List;

public class DialogsListAdapter extends BaseListAdapter<Dialog> {

    private DataManager dataManager;

    public DialogsListAdapter(BaseActivity baseActivity, List<Dialog> objectsList) {
        super(baseActivity, objectsList);
        dataManager = DataManager.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        Dialog dialog = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_dialog, null);

            viewHolder = new ViewHolder();

            viewHolder.avatarImageView = (HexagonImageView) convertView.findViewById(R.id.avatar_imageview);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name_textview);
            viewHolder.lastMessageTextView = (TextView) convertView.findViewById(R.id.last_message_textview);
            viewHolder.timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
            viewHolder.unreadMessagesTextView = (TextView) convertView.findViewById(
                    R.id.unread_messages_textview);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager().getDialogOccupantsListByDialogId(
                dialog.getDialogId());
        User opponentUser = ChatUtils.getOpponentFromPrivateDialog(
                UserFriendUtils.createLocalUser(currentUser), dialogOccupantsList);
        if (Dialog.Type.PRIVATE.equals(dialog.getType())) {
            if (opponentUser.getFullName() != null) {
                viewHolder.nameTextView.setText(Utility.capitalize(opponentUser.getFullName()));
                displayAvatarImage(opponentUser.getAvatar(), viewHolder.avatarImageView);
            } else {
                viewHolder.nameTextView.setText(resources.getString(R.string.deleted_user));
                dataManager.getDialogDataManager().deleteById(dialog.getDialogId());
            }
        } else {
            viewHolder.nameTextView.setText(dialog.getTitle());
            viewHolder.avatarImageView.setImageResource(R.drawable.placeholder_group);
            displayGroupPhotoImage(dialog.getPhoto(), viewHolder.avatarImageView);
        }

        List<Long> dialogOccupantsIdsList = ChatUtils.getIdsFromDialogOccupantsList(dialogOccupantsList);
        long unreadMessages = dataManager.getMessageDataManager().getCountUnreadMessages(dialogOccupantsIdsList, currentUser.getId());
        long unreadDialogNotifications = dataManager.getDialogNotificationDataManager().getCountUnreadDialogNotifications(dialogOccupantsIdsList, currentUser.getId());

        long totalCount = unreadMessages + unreadDialogNotifications;

        if (totalCount > ConstsCore.ZERO_INT_VALUE) {
            viewHolder.unreadMessagesTextView.setText(totalCount + ConstsCore.EMPTY_STRING);
            viewHolder.unreadMessagesTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.unreadMessagesTextView.setVisibility(View.GONE);
        }

        Message message = dataManager.getMessageDataManager().getLastMessageWithTempByDialogId(dialogOccupantsIdsList);
        DialogNotification dialogNotification = dataManager.getDialogNotificationDataManager().getLastDialogNotificationByDialogId(dialogOccupantsIdsList);
        if (Dialog.Type.PRIVATE.equals(dialog.getType())) {
            viewHolder.lastMessageTextView.setText(
                    ChatUtils.getDialogLastMessage(resources.getString(R.string.cht_contact_request_received), message, dialogNotification));
        } else {
            viewHolder.lastMessageTextView.setText(
                    ChatUtils.getDialogLastMessage(resources.getString(R.string.cht_notification_message), message, dialogNotification));
        }

        viewHolder.timeTextView.setText(Utility.getTimeAgo(ChatUtils.getDialogMessageCreatedDate(true, message, dialogNotification) * 1000));
        return convertView;
    }


    private static class ViewHolder {

        public HexagonImageView avatarImageView;
        public TextView nameTextView;
        public TextView lastMessageTextView;
        public TextView unreadMessagesTextView;
        public TextView timeTextView;
    }
}