package com.ss.fun2sh.ui.activities.chats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.quickblox.q_municate_db.models.User;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.others.BaseFriendsListActivity;
import com.ss.fun2sh.ui.adapters.friends.FriendsAdapter;
import com.ss.fun2sh.ui.adapters.friends.SelectableFriendsAdapter;
import com.ss.fun2sh.ui.views.recyclerview.SimpleDividerItemDecoration;
import com.ss.fun2sh.utils.ToastUtils;
import com.ss.fun2sh.utils.listeners.SelectUsersListener;
import com.ss.fun2sh.utils.listeners.simple.SimpleOnRecycleItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class NewGroupDialogActivity extends BaseFriendsListActivity implements SelectUsersListener {

    @Bind(R.id.members_edittext)
    EditText membersEditText;

    @Bind(R.id.edittextsearch)
    EditText searchEditText;
    List<User> friendsList;
    List<User> friendsListTemporary = new ArrayList<>();

    public static void start(Context context) {
        Intent intent = new Intent(context, NewGroupDialogActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_new_group;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        friendsList = new ArrayList<>();
        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                friendsList = getFriendsList();
                if (friendsList != null) {
                    friendsListTemporary.clear();
                    if (cs.equals("")) {
                        friendsListTemporary.addAll(friendsList);
                        friendsAdapter = new SelectableFriendsAdapter(NewGroupDialogActivity.this, friendsListTemporary, true);
                        friendsRecyclerView.setAdapter(friendsAdapter);
                        friendsAdapter.notifyDataSetChanged();
                    } else {
                        if (friendsList != null) {
                            for (int i = 0; i < friendsList.size(); i++) {
                                if (friendsList.get(i).getFullName().toLowerCase()
                                        .contains(cs)) {
                                    friendsListTemporary.add(friendsList.get(i));
                                }
                            }
                        }
                        friendsAdapter = new SelectableFriendsAdapter(NewGroupDialogActivity.this, friendsListTemporary, true);
                        friendsRecyclerView.setAdapter(friendsAdapter);
                        friendsAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        setUpActionBarWithUpButton();
        initCustomListeners();
    }

    @Override
    protected void initRecyclerView() {
        super.initRecyclerView();
        ((SelectableFriendsAdapter) friendsAdapter).setSelectUsersListener(this);
        friendsRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

    }

    @Override
    protected FriendsAdapter getFriendsAdapter() {
        return new SelectableFriendsAdapter(this, getFriendsList(), true);
    }

    @Override
    protected void performDone() {
        List<User> selectedFriendsList = ((SelectableFriendsAdapter) friendsAdapter).getSelectedFriendsList();

        if (!selectedFriendsList.isEmpty()) {

            CreateGroupDialogActivity.start(this, selectedFriendsList);

        }else {
            ToastUtils.longToast(R.string.new_group_no_friends_for_creating_group);
        }
    }

    @Override
    public void onSelectedUsersChanged(int count, String fullNames) {
        membersEditText.setText(fullNames);
    }

    private void initFields() {
        title = getString(R.string.create_group_title);
    }

    private void initCustomListeners() {
        friendsAdapter.setOnRecycleItemClickListener(new SimpleOnRecycleItemClickListener<User>() {

            @Override
            public void onItemClicked(View view, User entity, int position) {
                ((SelectableFriendsAdapter) friendsAdapter).selectFriend(position);
            }
        });
    }
}