package com.ss.fun2sh.ui.fragments.fun;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.Message;
import com.ss.fun2sh.Activity.PackageUpgradeActivity;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.main.MainActivity;
import com.ss.fun2sh.ui.adapters.chats.FavouriteAdapter;
import com.ss.fun2sh.ui.fragments.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import static com.quickblox.q_municate_core.utils.ChatUtils.getCombinationMessagesListFromMessagesList;
import static com.ss.fun2sh.CRUD.Const.App_Ver.reg_type;


public class FavouriteFragment extends BaseFragment implements SearchView.OnQueryTextListener {

    FavouriteAdapter favouriteAdapter;
    RecyclerView messagesRecyclerView;
    DataManager dataManager;
    protected List<CombinationMessage> combinationMessagesList;
    private Handler mainThreadHandler;
    Spinner filter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mainThreadHandler = new Handler(Looper.getMainLooper());
        dataManager = DataManager.getInstance();
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
                        favouriteAdapter.setFilter(combinationMessagesList);
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

    TextView tv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView;
        //if (PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.reg_type).equals("PREMIUM")) {
        if (true) {
            rootView = inflater.inflate(R.layout.fragment_favourite, container, false);
            messagesRecyclerView = (RecyclerView) rootView.findViewById(R.id.messages_recycleview);
            filter = (Spinner) rootView.findViewById(R.id.filterByType);
            tv = (TextView) rootView.findViewById(R.id.empty_list_textview);
            favouriteAdapter = new FavouriteAdapter(baseActivity, createCombinationMessagesList());
            initMessagesRecyclerView();
            filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    // your code here
                    if (position == 0) {
                        combinationMessagesList = createCombinationMessagesList();
                    } else if (position == 1) {
                        combinationMessagesList = filterTextMessage(createCombinationMessagesList());
                    } else if (position == 2) {
                        combinationMessagesList = filterByType(createCombinationMessagesList(), Attachment.Type.PICTURE);
                    } else if (position == 3) {
                        combinationMessagesList = filterByType(createCombinationMessagesList(), Attachment.Type.AUDIO);
                    } else if (position == 4) {
                        combinationMessagesList = filterByType(createCombinationMessagesList(), Attachment.Type.VIDEO);
                    } else if (position == 5) {
                        combinationMessagesList = filterByType(createCombinationMessagesList(), Attachment.Type.DOC);
                    } else if (position == 6) {
                        combinationMessagesList = filterByType(createCombinationMessagesList(), Attachment.Type.OTHER);
                    }
                    if (favouriteAdapter != null) {
                        favouriteAdapter.setFilter(combinationMessagesList);
                        checkEmpyList(combinationMessagesList);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                    combinationMessagesList = createCombinationMessagesList();
                    favouriteAdapter.setFilter(combinationMessagesList);
                    checkEmpyList(combinationMessagesList);
                }

            });
            ;

        } else {
            rootView = inflater.inflate(R.layout.fragment_upgrade, container, false);
            Button packageUpgrade = (Button) rootView.findViewById(R.id.package_upgrade);
            packageUpgrade.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putString("reg_type", String.valueOf(PrefsHelper.getPrefsHelper().getPref(reg_type)));
                    M.I(baseActivity, PackageUpgradeActivity.class, args);
                }
            });
        }
        return rootView;
    }

    private void checkEmpyList(List<CombinationMessage> combinationMessages) {
        if (combinationMessages.size() > 0) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setText("No favourite item found");
            tv.setVisibility(View.VISIBLE);
        }
    }

    protected List<CombinationMessage> createCombinationMessagesList() {

        List<Message> messagesList = dataManager.getMessageDataManager().getFavMessages();
        List<CombinationMessage> combinationMessagesList = getCombinationMessagesListFromMessagesList(messagesList);
        return combinationMessagesList;
    }

    protected void initMessagesRecyclerView() {
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        messagesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        messagesRecyclerView.setAdapter(favouriteAdapter);
        scrollMessagesWithDelay();
    }

    private void scrollMessagesWithDelay() {
        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                messagesRecyclerView.scrollToPosition(favouriteAdapter.getItemCount() - 1);
            }
        }, 300);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        List<CombinationMessage> filterList = filter(combinationMessagesList, newText);
        favouriteAdapter.setFilter(filterList);
        checkEmpyList(filterList);
        return true;
    }



    private List<CombinationMessage> filter(List<CombinationMessage> models, String query) {
        query = query.toLowerCase();

        final List<CombinationMessage> filteredModelList = new ArrayList<>();
        for (CombinationMessage model : models) {
            final String text = model.getDialogOccupant().getUser().getFullName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    private List<CombinationMessage> filterByType(List<CombinationMessage> models, Attachment.Type query) {

        final List<CombinationMessage> filteredModelList = new ArrayList<>();
        for (CombinationMessage model : models) {
            if (model.getAttachment() != null) {
                if (model.getAttachment().getType().equals(query)) {
                    filteredModelList.add(model);
                }
            }
        }
        return filteredModelList;
    }

    private List<CombinationMessage> filterTextMessage(List<CombinationMessage> models) {

        final List<CombinationMessage> filteredModelList = new ArrayList<>();
        for (CombinationMessage model : models) {
            if (model.getAttachment() == null) {
                filteredModelList.add(model);

            }
        }
        return filteredModelList;
    }
}
