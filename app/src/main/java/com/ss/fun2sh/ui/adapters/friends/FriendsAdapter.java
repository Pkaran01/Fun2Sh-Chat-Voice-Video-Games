package com.ss.fun2sh.ui.adapters.friends;

import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.siyamed.shapeimageview.HexagonImageView;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivacyListsManager;
import com.quickblox.chat.listeners.QBPrivacyListListener;
import com.quickblox.chat.model.QBPrivacyList;
import com.quickblox.chat.model.QBPrivacyListItem;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.commands.chat.QBCreatePrivateChatCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBDeleteChatCommand;
import com.quickblox.q_municate_core.qb.commands.friend.QBRemoveFriendCommand;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConnectivityUtils;
import com.quickblox.q_municate_core.utils.OnlineStatusUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.CRUD.NetworkUtil;
import com.ss.fun2sh.CRUD.Utility;
import com.ss.fun2sh.R;
import com.ss.fun2sh.oldutils.Constants;
import com.ss.fun2sh.ui.activities.base.BaseActivity;
import com.ss.fun2sh.ui.activities.call.CallActivity;
import com.ss.fun2sh.ui.activities.chats.PrivateDialogActivity;
import com.ss.fun2sh.ui.activities.groupcall.activities.GroupCallActivity;
import com.ss.fun2sh.ui.activities.main.MainActivity;
import com.ss.fun2sh.ui.activities.profile.UserProfileActivity;
import com.ss.fun2sh.ui.adapters.base.BaseClickListenerViewHolder;
import com.ss.fun2sh.ui.adapters.base.BaseFilterAdapter;
import com.ss.fun2sh.ui.adapters.base.BaseViewHolder;
import com.ss.fun2sh.ui.fragments.dialogs.base.ProgressDialogFragment;
import com.ss.fun2sh.utils.DateUtils;
import com.ss.fun2sh.utils.ToastUtils;
import com.ss.fun2sh.utils.helpers.TextViewHelper;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.ss.fun2sh.CRUD.Utility.getDirectoryName;

public class FriendsAdapter extends BaseFilterAdapter<User, BaseClickListenerViewHolder<User>> {

    private boolean withFirstLetter;
    private QBFriendListHelper qbFriendListHelper;
    private DataManager dataManager;
    static List<User> userList;
    private boolean removeContactAndChatHistory;

    public FriendsAdapter(BaseActivity baseActivity, List<User> userslist, boolean withFirstLetter) {
        super(baseActivity, userslist);
        this.withFirstLetter = withFirstLetter;
        dataManager = DataManager.getInstance();
        this.userList = userslist;
    }


    @Override
    protected boolean isMatch(User item, String query) {
        return item.getFullName() != null && item.getFullName().toLowerCase().contains(query);
    }

    @Override
    public BaseClickListenerViewHolder<User> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(this, layoutInflater.inflate(R.layout.item_friend, parent, false));
    }

    @Override
    public void onBindViewHolder(BaseClickListenerViewHolder<User> baseClickListenerViewHolder, final int position) {
        User user = getItem(position);
        ViewHolder viewHolder = (ViewHolder) baseClickListenerViewHolder;

        if (withFirstLetter) {
            initFirstLetter(viewHolder, position, user);
        } else {
            viewHolder.firstLatterTextView.setVisibility(View.GONE);
        }

        viewHolder.nameTextView.setText(Utility.capitalize(user.getFullName()));

        if (DataManager.getInstance().getUserDataManager().isBlocked(user.getUserId())) {
            viewHolder.nameTextView.setTextColor(Color.parseColor("#999999"));
        }

        displayAvatarImage(user.getAvatar(), viewHolder.avatarImageView);

        if (!TextUtils.isEmpty(query)) {
            TextViewHelper.changeTextColorView(baseActivity, viewHolder.nameTextView, query);
        }

        setLabel(viewHolder, user);
    }

    public void setFriendListHelper(QBFriendListHelper qbFriendListHelper) {
        this.qbFriendListHelper = qbFriendListHelper;
        notifyDataSetChanged();
    }

    private void initFirstLetter(ViewHolder viewHolder, int position, User user) {
        if (TextUtils.isEmpty(user.getFullName())) {
            return;
        }

        viewHolder.firstLatterTextView.setVisibility(View.INVISIBLE);

        Character firstLatter = user.getFullName().toUpperCase().charAt(0);
        if (position == 0) {
            setLetterVisible(viewHolder, firstLatter);
        } else {
            Character beforeFirstLatter;
            User beforeUser = getItem(position - 1);
            if (beforeUser != null && beforeUser.getFullName() != null) {
                beforeFirstLatter = beforeUser.getFullName().toUpperCase().charAt(0);

                if (!firstLatter.equals(beforeFirstLatter)) {
                    setLetterVisible(viewHolder, firstLatter);
                }
            }
        }
    }

    private void setLetterVisible(ViewHolder viewHolder, Character character) {
        viewHolder.firstLatterTextView.setText(String.valueOf(character));
        viewHolder.firstLatterTextView.setVisibility(View.VISIBLE);
    }

    private void setLabel(ViewHolder viewHolder, User user) {
        boolean online = qbFriendListHelper != null && qbFriendListHelper.isUserOnline(user.getUserId());

        if (isMe(user)) {
            online = true;
        }
        if (!DataManager.getInstance().getUserDataManager().isBlocked(user.getUserId())) {
            if (online) {
                viewHolder.labelTextView.setText(OnlineStatusUtils.getOnlineStatus(online));
                viewHolder.labelTextView.setTextColor(baseActivity.getResources().getColor(R.color.green));
            } else {
                viewHolder.labelTextView.setText(baseActivity.getString(R.string.last_seen,
                        DateUtils.toTodayYesterdayShortDateWithoutYear2(user.getLastLogin()),
                        DateUtils.formatDateSimpleTime(user.getLastLogin())));
                viewHolder.labelTextView.setTextColor(baseActivity.getResources().getColor(R.color.dark_gray));
            }
        } else {
            viewHolder.labelTextView.setText("Communication Blocked");
            viewHolder.labelTextView.setTextColor(baseActivity.getResources().getColor(R.color.dark_gray));
        }
    }

    private boolean isMe(User inputUser) {
        QBUser currentUser = AppSession.getSession().getUser();
        return currentUser.getId() == inputUser.getUserId();
    }

    protected class ViewHolder extends BaseViewHolder<User> {

        @Bind(R.id.first_latter_textview)
        TextView firstLatterTextView;

        @Bind(R.id.avatar_imageview)
        HexagonImageView avatarImageView;

        @Bind(R.id.name_textview)
        TextView nameTextView;

        @Bind(R.id.label_textview)
        TextView labelTextView;

        public ViewHolder(final FriendsAdapter adapter, View view) {
            super(adapter, view);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    friendOption(adapter, userList.get(getAdapterPosition()).getUserId());

                    return false;
                }
            });
        }
    }

    private void friendOption(final FriendsAdapter adapter, final int userId) {

        if (dataManager.getUserDataManager().isBlocked(userId)) {
            new MaterialDialog.Builder(adapter.baseActivity)
                    .title(R.string.new_message_select_option)
                    .items(R.array.new_messages_friend_unblock)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            if (which == 0) {
                                sendMessage(userId, adapter);
                            } else if (which == 1) {
                                callToUser(dataManager.getUserDataManager().get(userId), QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO);
                                CoreSharedHelper.getInstance().savePref(Constants.AUDIOONETOONECALL, "true");
                                // audioCall(userId, adapter);
                            } else if (which == 2) {
                                callToUser(dataManager.getUserDataManager().get(userId), QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);
                                CoreSharedHelper.getInstance().savePref(Constants.VIDEOONETOONECALL, "true");
                                // videoCall(userId, adapter);
                            } else if (which == 3) {
                                blockUnblock(userId, adapter, "Unblock ");
                            } else if (which == 4) {
                                deleteChat(adapter, userId);
                            } else if (which == 5) {
                                removeContactChat(adapter, userId);
                            }
                        }
                    }).show();
        } else {
            new MaterialDialog.Builder(adapter.baseActivity)
                    .title(R.string.new_message_select_option)
                    .items(R.array.new_messages_friend)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            if (which == 0) {
                                sendMessage(userId, adapter);
                            } else if (which == 1) {
                                callToUser(dataManager.getUserDataManager().get(userId), QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO);
                                CoreSharedHelper.getInstance().savePref(Constants.AUDIOONETOONECALL, "true");
                            } else if (which == 2) {
                                callToUser(dataManager.getUserDataManager().get(userId), QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);
                                CoreSharedHelper.getInstance().savePref(Constants.VIDEOONETOONECALL, "true");
                            } else if (which == 3) {
                                blockUnblock(userId, adapter, "Block ");
                            } else if (which == 4) {
                                deleteChat(adapter, userId);
                            } else if (which == 5) {
                                removeContactChat(adapter, userId);
                            }
                        }
                    }).show();
        }

    }


    private void callToUser(User user, QBRTCTypes.QBConferenceType qbConferenceType) {
        if (!isChatInitializedAndUserLoggedIn()) {
            ToastUtils.longToast(R.string.call_chat_service_is_initializing);
            return;
        }
        List<QBUser> qbUserList = new ArrayList<>(1);
        qbUserList.add(UserFriendUtils.createQbUser(user));
        GroupCallActivity.start(baseActivity, qbUserList, qbConferenceType, null);

    }


    private void removeContactChat(FriendsAdapter adapter, int userId) {
        if (checkNetworkAvailableWithError(adapter)) {
            removeContactAndChatHistory = true;
            showRemoveContactAndChatHistoryDialog(adapter, userId);
        }
    }

    private void deleteChat(FriendsAdapter adapter, int userId) {
        if (checkNetworkAvailableWithError(adapter)) {
            removeContactAndChatHistory = false;
            showRemoveChatHistoryDialog(userId, adapter);
        }
    }

    private void blockUnblock(int userId, FriendsAdapter adapter, String status) {
        if (status.equalsIgnoreCase("Block ")) {
            blockContactMessage(status + dataManager.getUserDataManager().get(userId).toString() + "? Blocked contacts will no longer be able to call you or send you messages", adapter, userId);
        } else {
            blockContactMessage(status + dataManager.getUserDataManager().get(userId).toString() + "? This contacts will be able to call you or send you messages", adapter, userId);
        }
    }

    private void videoCall(int userId, FriendsAdapter adapter) {
        if (!dataManager.getUserDataManager().isBlocked(userId)) {
            callToUser(QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO, adapter, userId);
        } else {
            Utility.blockContactMessage(adapter.baseActivity, "Unblock " + dataManager.getUserDataManager().get(userId).toString() + " to place a FunChat video call", userId);
        }
    }

    private void audioCall(int userId, FriendsAdapter adapter) {
        if (!dataManager.getUserDataManager().isBlocked(userId)) {
            callToUser(QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO, adapter, userId);
        } else {
            Utility.blockContactMessage(adapter.baseActivity, "Unblock " + dataManager.getUserDataManager().get(userId).getFullName().toString() + " to place a FunChat voice call", userId);
        }
    }

    private void sendMessage(int userId, FriendsAdapter adapter) {
        boolean isFriend = DataManager.getInstance().getFriendDataManager().existsByUserId(dataManager.getUserDataManager().get(userId).getUserId());
        if (!isFriend) {
            ToastUtils.longToast(R.string.dialog_user_is_not_friend);
            return;
        }
        if (!dataManager.getUserDataManager().isBlocked(userId)) {
            DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupantForPrivateChat(dataManager.getUserDataManager().get(userId).getUserId());
            if (dialogOccupant != null && dialogOccupant.getDialog() != null) {
                PrivateDialogActivity.start(adapter.baseActivity, dataManager.getUserDataManager().get(userId), dialogOccupant.getDialog());
            } else {
                //   showProgress();
                QBCreatePrivateChatCommand.start(adapter.baseActivity, dataManager.getUserDataManager().get(userId));
            }
        } else {
            Utility.blockContactMessage(adapter.baseActivity, "Unblock " + dataManager.getUserDataManager().get(userId).getFullName().toString() + " to send a message", userId);
        }
    }

    private void callToUser(QBRTCTypes.QBConferenceType qbConferenceType, final FriendsAdapter adapter, final int userId) {
        if (!isChatInitializedAndUserLoggedIn()) {
            ToastUtils.longToast(R.string.call_chat_service_is_initializing);
            return;
        }

        boolean isFriend = DataManager.getInstance().getFriendDataManager().existsByUserId(dataManager.getUserDataManager().get(userId).getUserId());
        if (!isFriend) {
            ToastUtils.longToast(R.string.dialog_user_is_not_friend);
            return;
        }

        List<QBUser> qbUserList = new ArrayList<>(1);
        qbUserList.add(UserFriendUtils.createQbUser(dataManager.getUserDataManager().get(userId)));
        CallActivity.start(adapter.baseActivity, qbUserList, qbConferenceType, null);
    }

    protected boolean isChatInitializedAndUserLoggedIn() {
        return isChatInitialized() && QBChatService.getInstance().isLoggedIn();
    }

    protected boolean isChatInitialized() {
        return QBChatService.isInitialized() && AppSession.getSession().isSessionExist();
    }

    private void blockContactMessage(String message, final FriendsAdapter adapter, final int userId) {
        SweetAlertDialog sweetAlertDialog = M.dConfirem(adapter.baseActivity, message, "OK", "CANCEL");
        sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                if (NetworkUtil.getConnectivityStatus(adapter.baseActivity)) {
                    blockContact(userId);
                } else {
                    M.T(adapter.baseActivity, adapter.baseActivity.getString(R.string.dlg_internet_connection_is_missing));
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

    private void blockContact(int userId) {
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
                item1.setAllow(true);
            } else {
                dataManager.getUserDataManager().updateFriend(userId, 1);
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
            //    hideProgress();
            M.E(e.getMessage());
        }
    }

    public boolean checkNetworkAvailableWithError(FriendsAdapter adapter) {
        if (!isNetworkAvailable(adapter)) {
            ToastUtils.longToast(R.string.dlg_fail_connection);
            return false;
        } else {
            return true;
        }
    }

    public boolean isNetworkAvailable(FriendsAdapter adapter) {
        return ConnectivityUtils.isNetworkAvailable(adapter.baseActivity);
    }

    private void showRemoveChatHistoryDialog(final int userId, final FriendsAdapter adapter) {
        if (isChatExists(userId)) {
            SweetAlertDialog sweetAlertDialog = M.dConfirem(adapter.baseActivity, adapter.baseActivity.getString(R.string.user_profile_delete_chat_history, dataManager.getUserDataManager().get(userId).getFullName()), "OK", "CANCEL");
            sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    // showProgress();
                    deleteChat(userId, adapter);
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

    private boolean isChatExists(int userId) {
        return dataManager.getDialogOccupantDataManager().getDialogOccupantForPrivateChat(dataManager.getUserDataManager().get(userId).getUserId()) != null;
    }

    private void deleteChat(int userId, FriendsAdapter adapter) {
        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupantForPrivateChat(dataManager.getUserDataManager().get(userId).getUserId());
        if (dialogOccupant == null) {
            adapter.baseActivity.finish();
        } else {
            String dialogId = dialogOccupant.getDialog().getDialogId();
            QBDeleteChatCommand.start(adapter.baseActivity, dialogId, Dialog.Type.PRIVATE);
        }
    }

    private void showRemoveContactAndChatHistoryDialog(final FriendsAdapter adapter, final int userId) {
        SweetAlertDialog sweetAlertDialog = M.dConfirem(adapter.baseActivity, adapter.baseActivity.getString(R.string.user_profile_remove_contact_and_chat_history, dataManager.getUserDataManager().get(userId).getFullName()), "OK", "CANCEL");
        sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                // showProgress();
                if (isUserFriendOrUserRequest(userId)) {
                    QBRemoveFriendCommand.start(adapter.baseActivity, dataManager.getUserDataManager().get(userId).getUserId());
                } else {
                    deleteChat(userId, adapter);
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

    private boolean isUserFriendOrUserRequest(int userId) {
        boolean isFriend = dataManager.getFriendDataManager().existsByUserId(dataManager.getUserDataManager().get(userId).getUserId());
        boolean isUserRequest = dataManager.getUserRequestDataManager().existsByUserId(dataManager.getUserDataManager().get(userId).getUserId());
        return isFriend || isUserRequest;
    }


}