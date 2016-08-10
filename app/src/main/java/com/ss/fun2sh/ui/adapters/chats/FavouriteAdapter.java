package com.ss.fun2sh.ui.adapters.chats;

import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.commands.chat.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.State;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.CRUD.Utility;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.base.BaseActivity;
import com.ss.fun2sh.ui.adapters.base.BaseClickListenerViewHolder;
import com.ss.fun2sh.utils.DateUtils;

import java.util.List;

public class FavouriteAdapter extends BaseDialogMessagesAdapter {


    public FavouriteAdapter(
            BaseActivity baseActivity,
            List<CombinationMessage> objectsList) {
        super(baseActivity, objectsList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case TYPE_OWN_MESSAGE:
                return new ViewHolder(this, layoutInflater.inflate(R.layout.item_message_own, viewGroup, false));
            case TYPE_OPPONENT_MESSAGE:
                return new ViewHolder(this, layoutInflater.inflate(R.layout.item_group_message_opponent, viewGroup, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(BaseClickListenerViewHolder<CombinationMessage> baseClickListenerViewHolder, final int position) {
        final CombinationMessage combinationMessage = getItem(position);
        boolean ownMessage = !combinationMessage.isIncoming(currentUser.getId());

        ViewHolder viewHolder = (ViewHolder) baseClickListenerViewHolder;


        String avatarUrl = null;
        String senderName;
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new MaterialDialog.Builder(baseActivity)
                        .title(R.string.new_message_select_option)
                        .items(R.array.new_messages_fav)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                if (which == 0) {
                                    if (DataManager.getInstance().getMessageDataManager().updateFav(combinationMessage.getMessageId(), 0) > 0) {
                                        removeItem(combinationMessage);
                                        M.T(baseActivity, "Removed from favourite");
                                    } else {
                                        M.E("Error in remove to favourite");
                                    }
                                } else if (which == 1) {
                                    //Copy
                                    if (combinationMessage.getAttachment() != null) {
                                        //forward images or file ka code
                                        M.T(baseActivity, "Coming soon");
                                    } else {
                                        Utility.msgInClipBoard(baseActivity, combinationMessage.getBody());
                                    }
                                }
                            }
                        })
                        .show();
                return false;
            }
        });

        if (viewHolder.verticalProgressBar != null) {
            viewHolder.verticalProgressBar.setProgressDrawable(baseActivity.getResources().getDrawable(R.drawable.vertical_progressbar));
        }

        if (combinationMessage.getAttachment() != null) {
            resetUI(viewHolder);

            setViewVisibility(viewHolder.progressRelativeLayout, View.VISIBLE);
            viewHolder.timeAttachMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));

            if (ownMessage && combinationMessage.getState() != null) {
                setMessageStatus(viewHolder.attachDeliveryStatusImageView, State.DELIVERED.equals(
                        combinationMessage.getState()), State.READ.equals(combinationMessage.getState()));
            }
            displayAttachImageById(combinationMessage.getAttachment().getAttachmentId(), viewHolder);
        } else {
            resetUI(viewHolder);

            setViewVisibility(viewHolder.textMessageView, View.VISIBLE);
            viewHolder.messageTextView.setText(combinationMessage.getBody());
            viewHolder.timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));

            if (ownMessage && combinationMessage.getState() != null) {
                setMessageStatus(viewHolder.messageDeliveryStatusImageView, State.DELIVERED.equals(
                        combinationMessage.getState()), State.READ.equals(combinationMessage.getState()));
            } else if (ownMessage && combinationMessage.getState() == null) {
                viewHolder.messageDeliveryStatusImageView.setImageResource(android.R.color.transparent);
            }

        }
        if (ownMessage) {
            avatarUrl = combinationMessage.getDialogOccupant().getUser().getAvatar();
            displayAvatarImage(avatarUrl, viewHolder.avatarImageView);
        } else {
            senderName = combinationMessage.getDialogOccupant().getUser().getFullName();
            avatarUrl = combinationMessage.getDialogOccupant().getUser().getAvatar();
            viewHolder.nameTextView.setTextColor(colorUtils.getRandomTextColorById(combinationMessage.getDialogOccupant().getUser().getUserId()));
            viewHolder.nameTextView.setText(senderName);
            displayAvatarImage(avatarUrl, viewHolder.avatarImageView);
        }

        if (!State.READ.equals(combinationMessage.getState()) && !ownMessage && baseActivity.isNetworkAvailable()) {
            combinationMessage.setState(State.READ);
            QBUpdateStatusMessageCommand.start(baseActivity, ChatUtils.createQBDialogFromLocalDialog(dataManager, dialog), combinationMessage, true);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewType(getItem(position));
    }

}