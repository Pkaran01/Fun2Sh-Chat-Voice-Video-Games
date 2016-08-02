package com.ss.fun2sh.ui.adapters.chats;

import android.view.View;
import android.view.ViewGroup;

import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.commands.chat.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.State;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.base.BaseActivity;
import com.ss.fun2sh.ui.adapters.base.BaseClickListenerViewHolder;
import com.ss.fun2sh.utils.DateUtils;
import com.ss.fun2sh.utils.listeners.ChatUIHelperListener;

import java.util.List;

public class GroupDialogMessagesAdapter extends BaseDialogMessagesAdapter {

    public GroupDialogMessagesAdapter(BaseActivity baseActivity, List<CombinationMessage> objectsList,
                                      ChatUIHelperListener chatUIHelperListener, Dialog dialog) {
        super(baseActivity, objectsList);
        this.chatUIHelperListener = chatUIHelperListener;
        this.dialog = dialog;
    }

    @Override
    public PrivateDialogMessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case TYPE_REQUEST_MESSAGE:
                return new ViewHolder(this, layoutInflater.inflate(R.layout.item_notification_message, viewGroup, false));
            case TYPE_OWN_MESSAGE:
                return new ViewHolder(this, layoutInflater.inflate(R.layout.item_message_own, viewGroup, false));
            case TYPE_OPPONENT_MESSAGE:
                return new ViewHolder(this, layoutInflater.inflate(R.layout.item_group_message_opponent, viewGroup, false));
            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewType(getItem(position));
    }

    @Override
    public void onBindViewHolder(BaseClickListenerViewHolder<CombinationMessage> baseClickListenerViewHolder, int position) {
        ViewHolder viewHolder = (ViewHolder) baseClickListenerViewHolder;

        final CombinationMessage combinationMessage = getItem(position);
        boolean ownMessage = !combinationMessage.isIncoming(currentUser.getId());
        boolean notificationMessage = combinationMessage.getNotificationType() != null;

        String avatarUrl = null;
        String senderName;
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataManager.getInstance().getMessageDataManager().updateFav(combinationMessage.getMessageId(), 1) > 0) {
                    M.T(v.getContext(), "Added to favourite");
                } else {
                    M.E("Error in add to favourite");
                }
            }
        });
        if (viewHolder.verticalProgressBar != null) {
            viewHolder.verticalProgressBar.setProgressDrawable(baseActivity.getResources().getDrawable(R.drawable.vertical_progressbar));
        }

        if (notificationMessage) {
            viewHolder.messageTextView.setText(combinationMessage.getBody());
            viewHolder.timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));
        } else {

            resetUI(viewHolder);

            if (ownMessage) {
                avatarUrl = combinationMessage.getDialogOccupant().getUser().getAvatar();
            } else {
                senderName = combinationMessage.getDialogOccupant().getUser().getFullName();
                avatarUrl = combinationMessage.getDialogOccupant().getUser().getAvatar();
                viewHolder.nameTextView.setTextColor(colorUtils.getRandomTextColorById(combinationMessage.getDialogOccupant().getUser().getUserId()));
                viewHolder.nameTextView.setText(senderName);
            }

            if (combinationMessage.getAttachment() != null) {
                viewHolder.timeAttachMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));
                setViewVisibility(viewHolder.progressRelativeLayout, View.VISIBLE);
                displayAttachImageById(combinationMessage.getAttachment().getAttachmentId(), viewHolder);
            } else {
                setViewVisibility(viewHolder.textMessageView, View.VISIBLE);
                viewHolder.timeTextMessageTextView.setText(
                        DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));
                viewHolder.messageTextView.setText(combinationMessage.getBody());
            }
        }

        if (!State.READ.equals(combinationMessage.getState()) && !ownMessage && baseActivity.isNetworkAvailable()) {
            combinationMessage.setState(State.READ);
            QBUpdateStatusMessageCommand.start(baseActivity,
                    ChatUtils.createQBDialogFromLocalDialog(dataManager, dialog), combinationMessage, false);
        } else if (ownMessage) {
            combinationMessage.setState(State.READ);
            dataManager.getMessageDataManager().update(combinationMessage.toMessage(), false);
        }

        displayAvatarImage(avatarUrl, viewHolder.avatarImageView);
    }
}