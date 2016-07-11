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
import android.widget.Toast;

import com.ss.fun2sh.CRUD.ClickListener;
import com.ss.fun2sh.CRUD.RecyclerTouchListener;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.main.MainActivity;

import java.util.ArrayList;
import java.util.List;


public class FavouriteFragment extends Fragment {

    FavouriteAdapter favouriteAdapter;
    RecyclerView favouriteRecyclerView;
    List<String> favList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
               // launchContactsFragment();
                break;
            case R.id.action_more:
                MainActivity.drawer.openDrawer(MainActivity.drawer_view);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_favourite, container, false);

        favouriteRecyclerView = (RecyclerView) rootView.findViewById(R.id.favouriteRecyclerView);

        favList = new ArrayList<>();

        favList.add("fav frank");
        favList.add("fav ram");
        favList.add("fav mohan");
        favList.add("fav king");
        favList.add("fav Mohan");
        favList.add("fav ranu");
        favList.add("fav shanu");
        favList.add("fav ruch");
        favList.add("fav hukum");
        favList.add("fav sarkar");
        favList.add("fav ashok");
        favList.add("fav manjay");


        favouriteAdapter = new FavouriteAdapter(favList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        favouriteRecyclerView.setLayoutManager(mLayoutManager);
        favouriteRecyclerView.setItemAnimator(new DefaultItemAnimator());
        favouriteRecyclerView.setAdapter(favouriteAdapter);

        favouriteRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), favouriteRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {

                Toast.makeText(getActivity(), favList.get(position) + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        return rootView;
    }


}
