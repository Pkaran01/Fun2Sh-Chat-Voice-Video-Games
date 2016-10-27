package com.ss.fun2sh.ui.activities.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.HexagonImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivacyListsManager;
import com.quickblox.chat.listeners.QBPrivacyListListener;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBPrivacyList;
import com.quickblox.chat.model.QBPrivacyListItem;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.chat.QBCreatePrivateChatCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBDeleteChatCommand;
import com.quickblox.q_municate_core.qb.commands.friend.QBRemoveFriendCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.OnlineStatusUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.managers.UserDataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.CRUD.NetworkUtil;
import com.ss.fun2sh.CRUD.Utility;
import com.ss.fun2sh.R;
import com.ss.fun2sh.oldutils.Constants;
import com.ss.fun2sh.ui.activities.base.BaseLoggableActivity;
import com.ss.fun2sh.ui.activities.call.CallActivity;
import com.ss.fun2sh.ui.activities.chats.PrivateDialogActivity;
import com.ss.fun2sh.ui.activities.groupcall.activities.GroupCallActivity;
import com.ss.fun2sh.ui.activities.others.PreviewProfileImageActivity;
import com.ss.fun2sh.utils.DateUtils;
import com.ss.fun2sh.utils.ToastUtils;
import com.ss.fun2sh.utils.image.ImageLoaderUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.ss.fun2sh.R.id.block_contact;

public class UserProfileActivity extends BaseLoggableActivity {

    @Bind(R.id.avatar_imageview)
    HexagonImageView avatarImageView;

    @Bind(R.id.name_textview)
    TextView nameTextView;


    @Bind(R.id.timestamp_textview)
    TextView timestampTextView;

    @Bind(block_contact)
    TextView blockContact;

    @Bind(R.id.phone_view)
    View phoneView;

    @Bind(R.id.phone_textview)
    TextView phoneTextView;


    @Bind(R.id.status_textview)
    TextView statusTextView;


    private DataManager dataManager;
    private int userId;
    private User user;
    private Observer userObserver;
    private boolean removeContactAndChatHistory;
    User opponentUser;

 /*   public static void start(Context context, int friendId) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIEND_ID, friendId);
        context.startActivity(intent);
    }*/
    public static void start(Context context, int friendId, User opponent) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIEND_ID, friendId);
        intent.putExtra(QBServiceConsts.EXTRA_OPPONENT, opponent);
        context.startActivity(intent);
    }
    @Override
    protected int getContentResId() {
        return R.layout.activity_user_profile;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        setUpActionBarWithUpButton();

        initUIWithUsersData();
        addActions();
    }

    private void initFields() {
        opponentUser = (User) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_OPPONENT);
        title = getString(R.string.user_profile_title);
        dataManager = DataManager.getInstance();
        canPerformLogout.set(true);
        userId = getIntent().getExtras().getInt(QBServiceConsts.EXTRA_FRIEND_ID);
        user = dataManager.getUserDataManager().get(userId);
        title = user.getFullName() + " " + getString(R.string.user_profile_title);
        userObserver = new UserObserver();
        if (dataManager.getUserDataManager().isBlocked(userId)) {
            blockContact.setText(getString(R.string.user_unblock_contact));
        }
    }

    private void initUIWithUsersData() {
        loadAvatar();
        setName();
        setPhone();
        setStatus();
    }

    private void setStatus() {
        if (user.getStatus() != null) {
            statusTextView.setText(user.getStatus());
        } else {
            statusTextView.setText(getString(R.string.dummy_status));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        addObservers();
        setOnlineStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        deleteObservers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    @OnClick(R.id.send_message_button)
    void sendMessage(View view) {
        boolean isFriend = DataManager.getInstance().getFriendDataManager().existsByUserId(user.getUserId());
        if (!isFriend) {
            ToastUtils.longToast(R.string.dialog_user_is_not_friend);
            return;
        }
        if (!dataManager.getUserDataManager().isBlocked(userId)) {
            DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupantForPrivateChat(user.getUserId());
            if (dialogOccupant != null && dialogOccupant.getDialog() != null) {
                PrivateDialogActivity.start(UserProfileActivity.this, user, dialogOccupant.getDialog());
            } else {
                showProgress();
                QBCreatePrivateChatCommand.start(this, user);
            }
        } else {
            Utility.blockContactMessage(this, "Unblock " + nameTextView.getText().toString() + " to send a message", userId);
        }
    }

    @OnClick(R.id.audio_call_button)
    void audioCall(View view) {
        if (!dataManager.getUserDataManager().isBlocked(userId)) {
            //  callToUser(QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO);
            callToUser(opponentUser, QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO);
            CoreSharedHelper.getInstance().savePref(Constants.AUDIOONETOONECALL, "true");
        } else {
            Utility.blockContactMessage(this, "Unblock " + nameTextView.getText().toString() + " to place a FunChat voice call", userId);
        }
    }

    @OnClick(R.id.video_call_button)
    void videoCall(View view) {
        if (!dataManager.getUserDataManager().isBlocked(userId)) {
            //  callToUser(QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);
            callToUser(opponentUser, QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);
            CoreSharedHelper.getInstance().savePref(Constants.VIDEOONETOONECALL, "true");
        } else {
            Utility.blockContactMessage(this, "Unblock " + nameTextView.getText().toString() + " to place a FunChat video call", userId);
        }
    }

    private void callToUser(User user, QBRTCTypes.QBConferenceType qbConferenceType) {
        if (!isChatInitializedAndUserLoggedIn()) {
            ToastUtils.longToast(R.string.call_chat_service_is_initializing);
            return;
        }
        List<QBUser> qbUserList = new ArrayList<>(1);
        qbUserList.add(UserFriendUtils.createQbUser(user));
        GroupCallActivity.start(UserProfileActivity.this, qbUserList, qbConferenceType, null);

    }


    @OnClick(R.id.delete_chat_history_button)
    void deleteChatHistory(View view) {
        if (checkNetworkAvailableWithError()) {
            removeContactAndChatHistory = false;
            showRemoveChatHistoryDialog();
        }
    }

    @OnClick(block_contact)
    void blockContact(View view) {
        //Block Contact
        blockContactMessage("Block " + nameTextView.getText().toString() + "? Blocked contacts will no longer be able to call you or send you messages");


    }

    private void blockContactMessage(String message) {
        SweetAlertDialog sweetAlertDialog = M.dConfirem(UserProfileActivity.this, message, "OK", "CANCEL");
        sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                if (NetworkUtil.getConnectivityStatus(UserProfileActivity.this)) {
                    UserProfileActivity.this.blockContact();
                } else {
                    M.T(UserProfileActivity.this, getString(R.string.dlg_internet_connection_is_missing));
                }
                sweetAlertDialog.dismiss();
            }
        });
        sweetAlertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
            }
        });
    }

    private void blockContact() {
        try {
            QBPrivacyListsManager privacyListsManager = QBChatService.getInstance().getPrivacyListsManager();
            privacyListsManager.addPrivacyListListener(new QBPrivacyListListener() {
                @Override
                public void setPrivacyList(String s, List<QBPrivacyListItem> list) {
                    M.E("setPrivacyList");
                }

                @Override
                public void updatedPrivacyList(String s) {
                    M.E("updatedPrivacyList");
                }
            });
            QBPrivacyList list = new QBPrivacyList();
            list.setName("public");

            ArrayList<QBPrivacyListItem> items = new ArrayList<QBPrivacyListItem>();

            QBPrivacyListItem item1 = new QBPrivacyListItem();
            if (dataManager.getUserDataManager().isBlocked(userId)) {
                dataManager.getUserDataManager().updateFriend(userId, 0);
                blockContact.setText(getString(R.string.user_block_contact));
                item1.setAllow(true);
            } else {
                dataManager.getUserDataManager().updateFriend(userId, 1);
                blockContact.setText(getString(R.string.user_unblock_contact));
                item1.setAllow(false);
            }
            item1.setType(QBPrivacyListItem.Type.USER_ID);
            item1.setValueForType(String.valueOf(userId));
            items.add(item1);
            list.setItems(items);
            privacyListsManager.setPrivacyListAsDefault("public");
            privacyListsManager.setPrivacyListAsActive("public");
            privacyListsManager.setPrivacyList(list);
        } catch (SmackException.NotConnectedException e) {
            M.E(e.getMessage());
        } catch (XMPPException.XMPPErrorException e) {
            M.E(e.getMessage());
        } catch (SmackException.NoResponseException e) {
            hideProgress();
            M.E(e.getMessage());
        }
    }


    @OnClick(R.id.avatar_imageview)
    void avatarImageview(View view) {
        Const.previewImage = ((BitmapDrawable) avatarImageView.getDrawable()).getBitmap();
        view.startAnimation(AnimationUtils.loadAnimation(UserProfileActivity.this, R.anim.chat_attached_file_click));
        PreviewProfileImageActivity.start(UserProfileActivity.this, nameTextView.getText().toString());
    }

    @OnClick(R.id.remove_contact_and_chat_history_button)
    void removeContactAndChatHistory(View view) {
        if (checkNetworkAvailableWithError()) {
            removeContactAndChatHistory = true;
            showRemoveContactAndChatHistoryDialog();
        }
    }

    @Override
    public void notifyChangedUserStatus(int userId, boolean online) {
        super.notifyChangedUserStatus(userId, online);
        if (user.getUserId() == userId) {
            setOnlineStatus(online);
        }
    }

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);
        setOnlineStatus();
    }

    private void addObservers() {
        dataManager.getUserDataManager().addObserver(userObserver);
    }

    private void deleteObservers() {
        dataManager.getUserDataManager().deleteObserver(userObserver);
    }

    private void addActions() {
        addAction(QBServiceConsts.REMOVE_FRIEND_SUCCESS_ACTION, new RemoveFriendSuccessAction());
        addAction(QBServiceConsts.REMOVE_FRIEND_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.DELETE_DIALOG_SUCCESS_ACTION, new RemoveChatSuccessAction());
        addAction(QBServiceConsts.DELETE_DIALOG_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.CREATE_PRIVATE_CHAT_SUCCESS_ACTION, new CreatePrivateChatSuccessAction());
        addAction(QBServiceConsts.CREATE_PRIVATE_CHAT_FAIL_ACTION, failAction);

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.REMOVE_FRIEND_SUCCESS_ACTION);
        removeAction(QBServiceConsts.REMOVE_FRIEND_FAIL_ACTION);

        removeAction(QBServiceConsts.DELETE_DIALOG_SUCCESS_ACTION);
        removeAction(QBServiceConsts.DELETE_DIALOG_FAIL_ACTION);

        removeAction(QBServiceConsts.CREATE_PRIVATE_CHAT_SUCCESS_ACTION);
        removeAction(QBServiceConsts.CREATE_PRIVATE_CHAT_FAIL_ACTION);

        updateBroadcastActionList();
    }

    private void setOnlineStatus() {
        if (friendListHelper != null) {
            setOnlineStatus(friendListHelper.isUserOnline(user.getUserId()));
        }
    }

    private void setOnlineStatus(boolean online) {
        String offlineStatus = getString(R.string.last_seen,
                DateUtils.toTodayYesterdayShortDateWithoutYear2(user.getLastLogin()),
                DateUtils.formatDateSimpleTime(user.getLastLogin()));
        timestampTextView.setText(OnlineStatusUtils.getOnlineStatus(this, online, offlineStatus));
    }

    private void setName() {
        nameTextView.setText(user.getFullName());
    }

    private void setPhone() {
        if (user.getPhone() != null) {
            phoneView.setVisibility(View.GONE);
        } else {
            phoneView.setVisibility(View.GONE);
        }
        phoneTextView.setText(user.getPhone());
    }

    private void loadAvatar() {
        if (user.getAvatar() != null) {
            String url = user.getAvatar();
            ImageLoader.getInstance().displayImage(url, avatarImageView,
                    ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS);
        } else {
            avatarImageView.setImageResource(R.drawable.userimgdefault);
        }
    }

    private void showRemoveContactAndChatHistoryDialog() {
        SweetAlertDialog sweetAlertDialog = M.dConfirem(UserProfileActivity.this, getString(R.string.user_profile_remove_contact_and_chat_history, user.getFullName()), "OK", "CANCEL");
        sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                showProgress();
                if (isUserFriendOrUserRequest()) {
                    QBRemoveFriendCommand.start(UserProfileActivity.this, user.getUserId());
                } else {
                    deleteChat();
                }
                sweetAlertDialog.dismiss();
            }
        });
        sweetAlertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
            }
        });
    }

    private void showRemoveChatHistoryDialog() {
        if (isChatExists()) {
            SweetAlertDialog sweetAlertDialog = M.dConfirem(UserProfileActivity.this, getString(R.string.user_profile_delete_chat_history, user.getFullName()), "OK", "CANCEL");
            sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    showProgress();
                    deleteChat();
                    sweetAlertDialog.dismiss();
                }
            });
            sweetAlertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();
                }
            });
        } else {
            ToastUtils.longToast(R.string.user_profile_chat_does_not_exists);
        }
    }

    private void deleteChat() {
        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupantForPrivateChat(user.getUserId());
        if (dialogOccupant == null) {
            finish();
        } else {
            String dialogId = dialogOccupant.getDialog().getDialogId();
            QBDeleteChatCommand.start(this, dialogId, Dialog.Type.PRIVATE);
        }
    }

    private void startPrivateChat(QBDialog qbDialog) {
        PrivateDialogActivity.start(UserProfileActivity.this, user, ChatUtils.createLocalDialog(qbDialog));
    }

    private boolean isUserFriendOrUserRequest() {
        boolean isFriend = dataManager.getFriendDataManager().existsByUserId(user.getUserId());
        boolean isUserRequest = dataManager.getUserRequestDataManager().existsByUserId(user.getUserId());
        return isFriend || isUserRequest;
    }

    private boolean isChatExists() {
        return dataManager.getDialogOccupantDataManager().getDialogOccupantForPrivateChat(user.getUserId()) != null;
    }

    private void callToUser(QBRTCTypes.QBConferenceType qbConferenceType) {
        if (!isChatInitializedAndUserLoggedIn()) {
            ToastUtils.longToast(R.string.call_chat_service_is_initializing);
            return;
        }

        boolean isFriend = DataManager.getInstance().getFriendDataManager().existsByUserId(user.getUserId());
        if (!isFriend) {
            ToastUtils.longToast(R.string.dialog_user_is_not_friend);
            return;
        }

        List<QBUser> qbUserList = new ArrayList<>(1);
        qbUserList.add(UserFriendUtils.createQbUser(user));
        CallActivity.start(this, qbUserList, qbConferenceType, null);
    }

    private class UserObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null && data.equals(UserDataManager.OBSERVE_KEY)) {
                user = DataManager.getInstance().getUserDataManager().get(userId);
                initUIWithUsersData();
            }
        }
    }

    private class RemoveFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            deleteChat();
        }
    }

    private class RemoveChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();

            if (removeContactAndChatHistory) {
                ToastUtils.longToast(getString(R.string.user_profile_success_contacts_deleting, user.getFullName()));
                finish();
            } else {
                ToastUtils.longToast(getString(R.string.user_profile_success_chat_history_deleting, user.getFullName()));
            }
        }
    }

    private class CreatePrivateChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            hideProgress();
            QBDialog qbDialog = (QBDialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);
            startPrivateChat(qbDialog);
        }
    }
}