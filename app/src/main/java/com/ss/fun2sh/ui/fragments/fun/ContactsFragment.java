package com.ss.fun2sh.ui.fragments.fun;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ss.fun2sh.CRUD.ClickListener;
import com.ss.fun2sh.CRUD.RecyclerTouchListener;
import com.ss.fun2sh.R;

import java.util.ArrayList;


public class ContactsFragment extends Fragment {

    ContactsAdapter contactsAdapter;
    RecyclerView contactRecyclerView;
    ArrayList<String> contactList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

        contactRecyclerView = (RecyclerView) rootView.findViewById(R.id.contactRecyclerView);

        contactList = new ArrayList<>();

        contactList.add("contact frank");
        contactList.add("contact ram");
        contactList.add("contact mohan");
        contactList.add("contact king");
        contactList.add("contact Mohan");
        contactList.add("contact ranu");
        contactList.add("contact shanu");
        contactList.add("contact ruch");
        contactList.add("contact hukum");
        contactList.add("contact sarkar");
        contactList.add("contact ashok");
        contactList.add("contact manjay");



        contactsAdapter = new ContactsAdapter(contactList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        contactRecyclerView.setLayoutManager(mLayoutManager);
        contactRecyclerView.setItemAnimator(new DefaultItemAnimator());
        contactRecyclerView.setAdapter(contactsAdapter);

        contactRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), contactRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {

                Toast.makeText(getActivity(), contactList.get(position) + " is selected!", Toast.LENGTH_SHORT).show();

              /*  Intent intenforchat = new Intent(getActivity(), ChatActivity.class);
                getActivity().startActivity(intenforchat);*/
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }) );

        return rootView;
    }


}
