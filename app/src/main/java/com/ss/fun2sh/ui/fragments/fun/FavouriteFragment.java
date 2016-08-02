package com.ss.fun2sh.ui.fragments.fun;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Message;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.main.MainActivity;
import com.ss.fun2sh.ui.adapters.chats.FavouriteAdapter;
import com.ss.fun2sh.ui.fragments.base.BaseFragment;

import java.util.List;

import static com.quickblox.q_municate_core.utils.ChatUtils.getCombinationMessagesListFromMessagesList;


public class FavouriteFragment extends BaseFragment {

    FavouriteAdapter favouriteAdapter;
    RecyclerView messagesRecyclerView;
    DataManager dataManager;
    protected List<CombinationMessage> combinationMessagesList;
    private Handler mainThreadHandler;

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

        messagesRecyclerView = (RecyclerView) rootView.findViewById(R.id.messages_recycleview);
        combinationMessagesList = createCombinationMessagesList();
        if (combinationMessagesList.size() > 0) {
            initMessagesRecyclerView();
        } else {
            TextView tv=(TextView)rootView.findViewById(R.id.empty_list_textview);
            tv.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    protected List<CombinationMessage> createCombinationMessagesList() {

        List<Message> messagesList = dataManager.getMessageDataManager().getFavMessages();
        List<CombinationMessage> combinationMessagesList = getCombinationMessagesListFromMessagesList(messagesList);
        return combinationMessagesList;
    }

    protected void initMessagesRecyclerView() {
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        messagesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        favouriteAdapter = new FavouriteAdapter(baseActivity, combinationMessagesList);
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
}
