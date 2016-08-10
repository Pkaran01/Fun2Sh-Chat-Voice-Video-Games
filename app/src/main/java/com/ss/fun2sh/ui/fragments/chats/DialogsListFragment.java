package com.ss.fun2sh.ui.fragments.chats;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.core.loader.BaseLoader;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.commands.chat.QBDeleteChatCommand;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.DbUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.managers.DialogDataManager;
import com.quickblox.q_municate_db.managers.DialogOccupantDataManager;
import com.quickblox.q_municate_db.managers.MessageDataManager;
import com.quickblox.q_municate_db.managers.UserDataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.chats.GroupDialogActivity;
import com.ss.fun2sh.ui.activities.chats.PrivateDialogActivity;
import com.ss.fun2sh.ui.activities.main.MainActivity;
import com.ss.fun2sh.ui.adapters.chats.DialogsListAdapter;
import com.ss.fun2sh.ui.fragments.base.BaseLoaderFragment;
import com.ss.fun2sh.ui.fragments.search.SearchFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.OnItemClick;

public class DialogsListFragment extends BaseLoaderFragment<List<Dialog>> implements SearchView.OnQueryTextListener {

    private static final String TAG = DialogsListFragment.class.getSimpleName();
    private static final int LOADER_ID = DialogsListFragment.class.hashCode();

    @Bind(R.id.chats_listview)
    ListView dialogsListView;

    @Bind(R.id.empty_list_textview)
    TextView emptyListTextView;

    private DialogsListAdapter dialogsListAdapter;
    private DataManager dataManager;
    private QBUser qbUser;
    private Observer commonObserver;

    public static DialogsListFragment newInstance() {
        return new DialogsListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialogs_list, container, false);

        activateButterKnife(view);

        initFields();
        initChatsDialogs();

        registerForContextMenu(dialogsListView);

        return view;
    }

    @Override
    public void initActionBar() {
        super.initActionBar();
        actionBarBridge.setActionBarUpButtonEnabled(false);

    }

    private void initFields() {
        dataManager = DataManager.getInstance();
        commonObserver = new CommonObserver();
        qbUser = AppSession.getSession().getUser();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initDataLoader(LOADER_ID);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.dialogs_list_menu, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        MenuItemCompat.setOnActionExpandListener(item,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        // Do something when collapsed
                        dialogsListAdapter.setFilter(ChatUtils.fillTitleForPrivateDialogsList(getContext().getResources().getString(R.string.deleted_user),
                                dataManager, dataManager.getDialogDataManager().getAllSorted()));
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        // Do something when expanded
                        return true; // Return true to expand action view
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_more:
                MainActivity.drawer.openDrawer(MainActivity.drawer_view);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater menuInflater = baseActivity.getMenuInflater();
        menuInflater.inflate(R.menu.dialogs_list_ctx_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_delete:
                if (baseActivity.checkNetworkAvailableWithError()) {
                    Dialog dialog = dialogsListAdapter.getItem(adapterContextMenuInfo.position);
                    deleteDialog(dialog);
                }
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        addObservers();

        if (dialogsListAdapter != null) {
            checkVisibilityEmptyLabel();
        }

        if (dialogsListAdapter != null) {
            dialogsListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deleteObservers();
    }

    @OnItemClick(R.id.chats_listview)
    void startChat(int position) {
        Dialog dialog = dialogsListAdapter.getItem(position);

        if (!baseActivity.checkNetworkAvailableWithError() && isFirstOpeningDialog(dialog.getDialogId())) {
            return;
        }

        if (dialog.getType() == Dialog.Type.PRIVATE) {
            startPrivateChatActivity(dialog);
        } else {
            startGroupChatActivity(dialog);
        }
    }

    private boolean isFirstOpeningDialog(String dialogId) {
        return !dataManager.getMessageDataManager().getTempMessagesByDialogId(dialogId).isEmpty();
    }

    @Override
    public void onConnectedToService(QBService service) {
        if (groupChatHelper == null) {
            if (service != null) {
                groupChatHelper = (QBGroupChatHelper) service.getHelper(QBService.GROUP_CHAT_HELPER);
            }
        }
    }

    @Override
    protected Loader<List<Dialog>> createDataLoader() {
        return new DialogsListLoader(baseActivity, dataManager);
    }

    @Override
    public void onLoadFinished(Loader<List<Dialog>> loader, List<Dialog> dialogsList) {
        dialogsListAdapter.setNewData(dialogsList);
        dialogsListAdapter.notifyDataSetChanged();
        checkEmptyList(dialogsList.size());
    }

    private void checkVisibilityEmptyLabel() {
        emptyListTextView.setVisibility(dialogsListAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void addObservers() {
        dataManager.getDialogDataManager().addObserver(commonObserver);
        dataManager.getMessageDataManager().addObserver(commonObserver);
        dataManager.getUserDataManager().addObserver(commonObserver);
        dataManager.getDialogOccupantDataManager().addObserver(commonObserver);
    }

    private void deleteObservers() {
        dataManager.getDialogDataManager().deleteObserver(commonObserver);
        dataManager.getMessageDataManager().deleteObserver(commonObserver);
        dataManager.getUserDataManager().deleteObserver(commonObserver);
        dataManager.getDialogOccupantDataManager().deleteObserver(commonObserver);
    }

    private void initChatsDialogs() {
        List<Dialog> dialogsList = Collections.emptyList();
        dialogsListAdapter = new DialogsListAdapter(baseActivity, dialogsList);
        dialogsListView.setAdapter(dialogsListAdapter);
    }

    private void startPrivateChatActivity(Dialog dialog) {
        List<DialogOccupant> occupantsList = dataManager.getDialogOccupantDataManager()
                .getDialogOccupantsListByDialogId(dialog.getDialogId());
        User opponent = ChatUtils.getOpponentFromPrivateDialog(UserFriendUtils.createLocalUser(qbUser), occupantsList);

        if (!TextUtils.isEmpty(dialog.getDialogId())) {
            PrivateDialogActivity.start(baseActivity, opponent, dialog);
        }
    }

    private void startGroupChatActivity(Dialog dialog) {
        GroupDialogActivity.start(baseActivity, dialog);
    }

    private void updateDialogsList() {
        onChangedData();
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
        QBDeleteChatCommand.start(baseActivity, dialog.getDialogId(), dialog.getType());
    }

    private void checkEmptyList(int listSize) {
        if (listSize > 0) {
            emptyListTextView.setVisibility(View.GONE);
        } else {
            emptyListTextView.setVisibility(View.VISIBLE);
        }
    }

    private void launchContactsFragment() {
        SearchFragment.start(getActivity());
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Dialog> filteredModelList = filter(ChatUtils.fillTitleForPrivateDialogsList(getContext().getResources().getString(R.string.deleted_user),
                dataManager, dataManager.getDialogDataManager().getAllSorted()), newText);
        dialogsListAdapter.setFilter(filteredModelList);
        return true;
    }

    private List<Dialog> filter(List<Dialog> models, String query) {
        query = query.toLowerCase();

        final List<Dialog> filteredModelList = new ArrayList<>();
        for (Dialog model : models) {
            final String text = model.getTitle().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    private static class DialogsListLoader extends BaseLoader<List<Dialog>> {

        public DialogsListLoader(Context context, DataManager dataManager) {
            super(context, dataManager);
        }

        @Override
        protected List<Dialog> getItems() {
            return ChatUtils.fillTitleForPrivateDialogsList(getContext().getResources().getString(R.string.deleted_user),
                    dataManager, dataManager.getDialogDataManager().getAllSorted());
        }
    }

    private class CommonObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null) {
                if (data.equals(DialogDataManager.OBSERVE_KEY) || data.equals(MessageDataManager.OBSERVE_KEY)
                        || data.equals(UserDataManager.OBSERVE_KEY) || data.equals(DialogOccupantDataManager.OBSERVE_KEY)) {
                    updateDialogsList();
                }
            }
        }
    }
}