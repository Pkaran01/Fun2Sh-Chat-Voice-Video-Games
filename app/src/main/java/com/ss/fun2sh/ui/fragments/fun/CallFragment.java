package com.ss.fun2sh.ui.fragments.fun;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.main.MainActivity;

import java.util.ArrayList;
import java.util.List;


public class CallFragment extends Fragment {

    CallAdapter callAdapter;
    RecyclerView callRecyclerView;
    List<String> callList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_call, container, false);

        callList = new ArrayList<String>();


        callAdapter = new CallAdapter(getActivity(), callList);

        callRecyclerView = (RecyclerView) rootView.findViewById(R.id.call_recycler_view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        callRecyclerView.setLayoutManager(mLayoutManager);
        callRecyclerView.setItemAnimator(new DefaultItemAnimator());
        callRecyclerView.setAdapter(callAdapter);

        callAdapter.setOnItemClickListener(new CallAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {


            }
        });


        getCallData();

        return rootView;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.dialogs_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                //launchContactsFragment();
                break;
            case R.id.action_more:
                MainActivity.drawer.openDrawer(MainActivity.drawer_view);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
    private void getCallData() {
        callList.add("call frank");
        callList.add("call ram");
        callList.add("call mohan");
        callList.add("call king");
        callList.add("call Mohan");
        callList.add("call ranu");
        callList.add("call shanu");
        callList.add("call ruch");
        callList.add("call hukum");

        callAdapter.notifyDataSetChanged();
    }


}
