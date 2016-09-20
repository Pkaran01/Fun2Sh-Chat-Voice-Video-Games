package com.ss.fun2sh.ui.fragments.chats;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.core.loader.BaseLoader;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.commands.chat.QBDeleteChatCommand;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.DbUtils;
import com.quickblox.q_municate_core.utils.PrefsHelper;
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
import com.ss.fun2sh.Activity.PackageUpgradeActivity;
import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.CRUD.Utility;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.chats.GroupDialogActivity;
import com.ss.fun2sh.ui.activities.chats.NewGroupDialogActivity;
import com.ss.fun2sh.ui.activities.chats.PrivateDialogActivity;
import com.ss.fun2sh.ui.activities.main.MainActivity;
import com.ss.fun2sh.ui.adapters.chats.DialogsListAdapter;
import com.ss.fun2sh.ui.adapters.chats.GroupDialogsListAdapter;
import com.ss.fun2sh.ui.fragments.base.BaseLoaderFragment;
import com.ss.fun2sh.ui.fragments.search.SearchFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.OnItemClick;

import static com.ss.fun2sh.CRUD.Const.App_Ver.reg_type;

public class GroupDialogsListFragment extends BaseLoaderFragment<List<Dialog>> implements SearchView.OnQueryTextListener {

    private static final String TAG = GroupDialogsListFragment.class.getSimpleName();
    private static final int LOADER_ID = GroupDialogsListFragment.class.hashCode();

    @Nullable
    @Bind(R.id.chats_listview)
    RecyclerView groupDialogsListView;

    @Nullable
    @Bind(R.id.empty_list_textview)
    TextView emptyListTextView;

    @Nullable
    @Bind(R.id.createGroup)
    LinearLayout createGroup;


    private GroupDialogsListAdapter dialogsListAdapter;
    private DataManager dataManager;
    private QBUser qbUser;
    private Observer commonObserver;
    List<Dialog> dialogsList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_group_dialogs_list, container, false);
        activateButterKnife(view);
        initFields();
        initChatsDialogs();
        registerForContextMenu(groupDialogsListView);
        if (Utility.getTodayDate().equals(PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.expire_date))) {
            if (!PrefsHelper.getPrefsHelper().getPref(reg_type).equals("PREMIUM")) {
                view = inflater.inflate(R.layout.fragment_upgrade, container, false);
                Button packageUpgrade = (Button) view.findViewById(R.id.package_upgrade);
                packageUpgrade.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();
                        args.putString("reg_type", String.valueOf(PrefsHelper.getPrefsHelper().getPref(reg_type)));
                        M.I(baseActivity, PackageUpgradeActivity.class, args);
                    }
                });
            }
        }
        checkVisibilityEmptyLabel();
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
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewGroupDialogActivity.start(getActivity());
            }
        });
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
                        dialogsListAdapter.setFilter(ChatUtils.fillTitleForGroupDialogsList(getContext().getResources().getString(R.string.deleted_user),
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
        return new DialogsListLoader(getActivity(), dataManager);
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



        dialogsList = Collections.emptyList();
        groupDialogsListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        dialogsListAdapter = new GroupDialogsListAdapter(baseActivity, dialogsList);
        groupDialogsListView.setAdapter(dialogsListAdapter);
    }



    private void updateDialogsList() {
        onChangedData();
    }


    private void checkEmptyList(int listSize) {
        if (listSize > 0) {
            emptyListTextView.setVisibility(View.GONE);
        } else {
            emptyListTextView.setVisibility(View.VISIBLE);
        }
    }



    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Dialog> filteredModelList = filter(ChatUtils.fillTitleForGroupDialogsList(getContext().getResources().getString(R.string.deleted_user),
                dataManager, dataManager.getDialogDataManager().getAllSorted()), newText);
        dialogsListAdapter.setFilter(filteredModelList);
        checkEmptyList(filteredModelList.size());
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
            return ChatUtils.fillTitleForGroupDialogsList(getContext().getResources().getString(R.string.deleted_user),
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