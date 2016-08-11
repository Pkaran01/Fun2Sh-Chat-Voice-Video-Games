package com.ss.fun2sh.ui.adapters.chats;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.commands.chat.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.State;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.base.BaseActivity;
import com.ss.fun2sh.ui.adapters.base.BaseClickListenerViewHolder;
import com.ss.fun2sh.utils.DateUtils;
import com.ss.fun2sh.utils.FileUtils;
import com.ss.fun2sh.utils.listeners.ChatUIHelperListener;
import com.ss.fun2sh.utils.listeners.FriendOperationListener;

import java.io.File;
import java.util.List;

public class PrivateDialogMessagesAdapter extends BaseDialogMessagesAdapter {

    private static int EMPTY_POSITION = -1;

    private int lastRequestPosition = EMPTY_POSITION;
    private int lastInfoRequestPosition = EMPTY_POSITION;
    private FriendOperationListener friendOperationListener;

    public PrivateDialogMessagesAdapter(
            BaseActivity baseActivity,
            FriendOperationListener friendOperationListener,
            List<CombinationMessage> objectsList,
            ChatUIHelperListener chatUIHelperListener,
            Dialog dialog) {
        super(baseActivity, objectsList);
        this.friendOperationListener = friendOperationListener;
        this.chatUIHelperListener = chatUIHelperListener;
        this.dialog = dialog;
    }

    @Override
    public PrivateDialogMessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case TYPE_REQUEST_MESSAGE:
                return new ViewHolder(this, layoutInflater.inflate(R.layout.item_friends_notification_message, viewGroup, false));
            case TYPE_OWN_MESSAGE:
                return new ViewHolder(this, layoutInflater.inflate(R.layout.item_message_own, viewGroup, false));
            case TYPE_OPPONENT_MESSAGE:
                return new ViewHolder(this, layoutInflater.inflate(R.layout.item_private_message_opponent, viewGroup, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(BaseClickListenerViewHolder<CombinationMessage> baseClickListenerViewHolder, int position) {
        final CombinationMessage combinationMessage = getItem(position);
        final boolean ownMessage = !combinationMessage.isIncoming(currentUser.getId());

        ViewHolder viewHolder = (ViewHolder) baseClickListenerViewHolder;

        boolean friendsRequestMessage = DialogNotification.Type.FRIENDS_REQUEST.equals(
                combinationMessage.getNotificationType());
        boolean friendsInfoRequestMessage = combinationMessage
                .getNotificationType() != null && !friendsRequestMessage;

        String avatarUrl = null;


        if (viewHolder.verticalProgressBar != null) {
            viewHolder.verticalProgressBar.setProgressDrawable(baseActivity.getResources().getDrawable(R.drawable.vertical_progressbar));
        }

        if (friendsRequestMessage) {
            viewHolder.messageTextView.setText(combinationMessage.getBody());
            viewHolder.timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));

            setVisibilityFriendsActions(viewHolder, View.GONE);
        } else if (friendsInfoRequestMessage) {
            viewHolder.messageTextView.setText(combinationMessage.getBody());
            viewHolder.timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));

            setVisibilityFriendsActions(viewHolder, View.GONE);

            lastInfoRequestPosition = position;
        } else if (combinationMessage.getAttachment() != null) {
            resetUI(viewHolder);
            if (combinationMessage.getAttachment().getType().equals(Attachment.Type.PICTURE)) {
                setViewVisibility(viewHolder.progressRelativeLayout, View.VISIBLE);
                displayAttachImageById(combinationMessage.getAttachment().getAttachmentId(), viewHolder);
            } else {
                setViewVisibility(viewHolder.attachOtherFileRelativeLayout, View.VISIBLE);
                if (combinationMessage.getAttachment().getName() != null) {
                    final String[] tokens = combinationMessage.getAttachment().getName().split("\\.(?=[^\\.]+$)");
                    viewHolder.fileName.setText(tokens[0]);
                    viewHolder.fileType.setText(tokens[1].toUpperCase());
                    final String directory;
                    if (combinationMessage.getAttachment().getType().equals(Attachment.Type.AUDIO)) {
                        directory = FileUtils.audioFolderName;
                    } else if (combinationMessage.getAttachment().getType().equals(Attachment.Type.VIDEO)) {
                        directory = FileUtils.videoFolderName;
                    } else if (combinationMessage.getAttachment().getType().equals(Attachment.Type.DOC)) {
                        directory = FileUtils.docFolderName;
                    } else {
                        directory = FileUtils.otherFolderName;
                    }
                    final File file = new File(Environment.getExternalStorageDirectory().toString() + directory, combinationMessage.getAttachment().getName());
                    final boolean check = file.exists();
                    if (check)
                        viewHolder.downloadButton.setText("OPEN");
                    viewHolder.downloadButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //download file
                            if (!check) {
                                new DownloadFileAsync(directory).execute(combinationMessage.getAttachment().getRemoteUrl(), combinationMessage.getAttachment().getName());
                            } else {
                                MimeTypeMap myMime = MimeTypeMap.getSingleton();
                                Intent newIntent = new Intent(Intent.ACTION_VIEW);
                                String mimeType = myMime.getMimeTypeFromExtension(tokens[1]);
                                newIntent.setDataAndType(Uri.fromFile(file), mimeType);
                                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                try {
                                    baseActivity.startActivity(newIntent);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(baseActivity, "No handler for this type of file.", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                /*try {
                    String token;
                    String privateUrl;
                    token = QBAuth.getBaseService().getToken();
                    privateUrl = String.format("%s/blobs/%s?token=%s", BaseService.getServiceEndpointURL(), combinationMessage.getAttachment().getAttachmentId(), token);
                    M.E(privateUrl);
                } catch (BaseServiceException e) {
                    e.printStackTrace();
                }*/
                }
            }
            viewHolder.timeAttachMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));

            if (ownMessage && combinationMessage.getState() != null) {
                setMessageStatus(viewHolder.attachDeliveryStatusImageView, State.DELIVERED.equals(
                        combinationMessage.getState()), State.READ.equals(combinationMessage.getState()));
            }

            avatarUrl =  combinationMessage.getDialogOccupant().getUser().getAvatar();
            M.E("avatar url " + avatarUrl);
            displayAvatarImage(avatarUrl, viewHolder.avatarImageView);
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

            avatarUrl = combinationMessage.getDialogOccupant().getUser().getAvatar();
            displayAvatarImage(avatarUrl, viewHolder.avatarImageView);

        }


        if (ownMessage) {
            ownMessage(combinationMessage, viewHolder);
        } else {
            opponentMessage(combinationMessage, viewHolder);
        }


        if (!State.READ.equals(combinationMessage.getState()) && !ownMessage && baseActivity.isNetworkAvailable()) {
            combinationMessage.setState(State.READ);
            QBUpdateStatusMessageCommand.start(baseActivity, ChatUtils.createQBDialogFromLocalDialog(dataManager, dialog), combinationMessage, true);
        }

        // check if last messageCombination is request messageCombination
        boolean lastRequestMessage = (position == getAllItems().size() - 1 && friendsRequestMessage);
        if (lastRequestMessage) {
            findLastFriendsRequest();
        }

        // check if friend was rejected/deleted.
        if (lastRequestPosition != EMPTY_POSITION && lastRequestPosition < lastInfoRequestPosition) {
            lastRequestPosition = EMPTY_POSITION;
        } else if ((lastRequestPosition != EMPTY_POSITION && lastRequestPosition == position)) { // set visible friends actions
            setVisibilityFriendsActions(viewHolder, View.VISIBLE);
            initListeners(viewHolder, position, combinationMessage.getDialogOccupant().getUser().getUserId());
        }
    }


    @Override
    public int getItemViewType(int position) {
        return getItemViewType(getItem(position));
    }

    public void clearLastRequestMessagePosition() {
        lastRequestPosition = EMPTY_POSITION;
    }

    public void findLastFriendsRequestMessagesPosition() {
        new FindLastFriendsRequestThread().run();
    }

    private void setVisibilityFriendsActions(ViewHolder viewHolder, int visibility) {
        setViewVisibility(viewHolder.acceptFriendImageView, visibility);
        setViewVisibility(viewHolder.dividerView, visibility);
        setViewVisibility(viewHolder.rejectFriendImageView, visibility);
    }

    private void initListeners(ViewHolder viewHolder, final int position, final int userId) {
        viewHolder.acceptFriendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendOperationListener.onAcceptUserClicked(position, userId);
            }
        });

        viewHolder.rejectFriendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendOperationListener.onRejectUserClicked(position, userId);
            }
        });
    }

    private void findLastFriendsRequest() {
        for (int i = 0; i < getAllItems().size(); i++) {
            findLastFriendsRequest(i, getAllItems().get(i));
        }
    }

    private void findLastFriendsRequest(int position, CombinationMessage combinationMessage) {
        boolean ownMessage;
        boolean friendsRequestMessage;
        boolean isFriend;

        if (combinationMessage.getNotificationType() != null) {
            ownMessage = !combinationMessage.isIncoming(currentUser.getId());
            friendsRequestMessage = DialogNotification.Type.FRIENDS_REQUEST.equals(
                    combinationMessage.getNotificationType());

            if (friendsRequestMessage && !ownMessage) {
                isFriend = dataManager.getFriendDataManager().
                        getByUserId(combinationMessage.getDialogOccupant().getUser().getUserId()) != null;
                if (!isFriend) {
                    lastRequestPosition = position;
                }
            }
        }
    }

    private class FindLastFriendsRequestThread extends Thread {

        @Override
        public void run() {
            findLastFriendsRequest();
        }
    }
}