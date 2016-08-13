package com.ss.fun2sh.ui.fragments.fun;


import android.os.Bundle;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Call;
import com.quickblox.q_municate_db.models.User;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.main.MainActivity;
import com.ss.fun2sh.ui.fragments.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;


public class CallFragment extends BaseFragment implements SearchView.OnQueryTextListener {

    CallAdapter callAdapter;
    RecyclerView callRecyclerView;
    List<Call> callList;
    Spinner callFilter, callFilterByType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    TextView tv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_call, container, false);

        callList = DataManager.getInstance().getCallDataManager().getAllSorted();
        callAdapter = new CallAdapter(getActivity(), callList);
        tv = (TextView) rootView.findViewById(R.id.empty_list_textview);
        initCallRecyclerView(rootView);
        checkEmptyList(callList);
        return rootView;
    }

    private void checkEmptyList(List<Call> callList) {
        if (callList.size() <= 0) {
            tv.setText("No call log found");
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    private void initCallRecyclerView(View rootView) {
        callRecyclerView = (RecyclerView) rootView.findViewById(R.id.call_recycler_view);
        callFilter = (Spinner) rootView.findViewById(R.id.callFilter);
        callFilterByType = (Spinner) rootView.findViewById(R.id.callFilterByType);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        callRecyclerView.setLayoutManager(mLayoutManager);
        callRecyclerView.setItemAnimator(new DefaultItemAnimator());
        callRecyclerView.setAdapter(callAdapter);

        callFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here

                callList = DataManager.getInstance().getCallDataManager().getAllByStatus(callFilterByType.getSelectedItemPosition(), position);
                callAdapter.setFilter(callList);
                checkEmptyList(callList);
                callAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        callFilterByType.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // your code here

                        callList = DataManager.getInstance().getCallDataManager().getAllByStatus(position, callFilter.getSelectedItemPosition());
                        callAdapter.setFilter(callList);
                        checkEmptyList(callList);
                        callAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }

                }

        );
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
                        callAdapter.setFilter(callList);
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
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Call> filteredModelList = filter(callList, newText);
        callAdapter.setFilter(filteredModelList);
        checkEmptyList(filteredModelList);
        return true;
    }

    private List<Call> filter(List<Call> models, String query) {
        query = query.toLowerCase();

        final List<Call> filteredModelList = new ArrayList<>();
        for (Call model : models) {
            User user = DataManager.getInstance().getUserDataManager().get(model.getUser().getUserId());
            final String text = user.getFullName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }
}
