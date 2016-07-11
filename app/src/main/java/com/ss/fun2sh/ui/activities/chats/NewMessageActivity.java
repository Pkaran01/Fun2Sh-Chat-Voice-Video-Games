package com.ss.fun2sh.ui.activities.chats;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.chat.QBCreatePrivateChatCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.User;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.main.MainActivity;
import com.ss.fun2sh.ui.adapters.friends.FriendsAdapter;
import com.ss.fun2sh.ui.fragments.base.BaseFragment;
import com.ss.fun2sh.ui.fragments.search.SearchFragment;
import com.ss.fun2sh.utils.listeners.simple.SimpleOnRecycleItemClickListener;

import java.util.List;

import butterknife.Bind;

public class NewMessageActivity extends BaseFragment{

    @Bind(R.id.friends_recyclerview)
    RecyclerView friendsRecyclerView;

    private DataManager dataManager;
    private FriendsAdapter friendsAdapter;
    private User selectedUser;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_friends, container, false);

        activateButterKnife(view);
        setHasOptionsMenu(true);
        initFields();
        initRecyclerView();
        initCustomListeners();
        addActions();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeActions();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.new_message_menu, menu);
/*
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        final SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = null;

        if (searchMenuItem != null) {
            searchView = (SearchView) searchMenuItem.getActionView();
        }

        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setOnQueryTextListener(this);
            searchView.setOnCloseListener(this);
        }*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                launchContactsFragment();
                break;
            case R.id.action_create_group:
                MainActivity.drawer.openDrawer(MainActivity.drawer_view);
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    /*@Override
    public boolean onClose() {
        cancelSearch();
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String searchQuery) {
        KeyboardUtils.hideKeyboard(getActivity());
        search(searchQuery);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String searchQuery) {
        search(searchQuery);
        return true;
    }*/

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);
        if (friendListHelper != null) {
            friendsAdapter.setFriendListHelper(friendListHelper);
        }
    }

    @Override
    public void onChangedUserStatus(int userId, boolean online) {
        super.onChangedUserStatus(userId, online);
        friendsAdapter.notifyDataSetChanged();
    }

    private void initFields() {
        baseActivity.title = getString(R.string.new_message_title);
        dataManager = DataManager.getInstance();
    }

    private void initRecyclerView() {
        List<Friend> friendsList = dataManager.getFriendDataManager().getAllSorted();
        friendsAdapter = new FriendsAdapter(baseActivity, UserFriendUtils.getUsersFromFriends(friendsList), true);
        friendsAdapter.setFriendListHelper(friendListHelper);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        friendsRecyclerView.setAdapter(friendsAdapter);
    }

    private void initCustomListeners() {
        friendsAdapter.setOnRecycleItemClickListener(new SimpleOnRecycleItemClickListener<User>() {

            @Override
            public void onItemClicked(View view, User user, int position) {
                super.onItemClicked(view, user, position);
                selectedUser = user;
                checkForOpenChat(user);
            }
        });
    }

    private void addActions() {
        baseActivity.addAction(QBServiceConsts.CREATE_PRIVATE_CHAT_SUCCESS_ACTION, new CreatePrivateChatSuccessAction());
        baseActivity.addAction(QBServiceConsts.CREATE_PRIVATE_CHAT_FAIL_ACTION, failAction);

        baseActivity.updateBroadcastActionList();
    }

    private void removeActions() {
        baseActivity.removeAction(QBServiceConsts.CREATE_PRIVATE_CHAT_SUCCESS_ACTION);
        baseActivity.removeAction(QBServiceConsts.CREATE_PRIVATE_CHAT_FAIL_ACTION);

        baseActivity.updateBroadcastActionList();
    }

    private void checkForOpenChat(User user) {
        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupantForPrivateChat(user.getUserId());
        if (dialogOccupant != null && dialogOccupant.getDialog() != null) {
            startPrivateChat(dialogOccupant.getDialog());
        } else {
            if (baseActivity.checkNetworkAvailableWithError()) {
                baseActivity.showProgress();
                QBCreatePrivateChatCommand.start(baseActivity, user);
            }
        }
    }

    private void startPrivateChat(Dialog dialog) {
        PrivateDialogActivity.start(baseActivity, selectedUser, dialog);
        baseActivity.finish();
    }

    private void search(String searchQuery) {
        if (friendsAdapter != null) {
            friendsAdapter.setFilter(searchQuery);
        }
    }

    private void cancelSearch() {
        if (friendsAdapter != null) {
            friendsAdapter.flushFilter();
        }
    }

    private class CreatePrivateChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            baseActivity.hideProgress();
            QBDialog qbDialog = (QBDialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);
            startPrivateChat(ChatUtils.createLocalDialog(qbDialog));
        }
    }

    private void launchContactsFragment() {
        SearchFragment.start(getActivity());
    }
}