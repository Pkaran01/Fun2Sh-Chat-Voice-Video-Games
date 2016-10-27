package com.ss.fun2sh.ui.activities.groupcall.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;
import com.ss.fun2sh.oldutils.Constants;
import com.ss.fun2sh.ui.activities.groupcall.activities.GroupCallActivity;
import com.ss.fun2sh.ui.activities.groupcall.adapters.OpponentsAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QuickBlox team
 */
public class OpponentsFragment extends Fragment implements View.OnClickListener, Serializable {

    private static final String TAG = OpponentsFragment.class.getSimpleName();
    private OpponentsAdapter opponentsAdapter;
    private Button btnAudioCall;
    private Button btnVideoCall;
    private View view = null;
    private ListView opponentsList;

    public static OpponentsFragment getInstance() {
        return new OpponentsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((GroupCallActivity) getActivity()).initGroupActionBar();

        view = inflater.inflate(R.layout.group_fragment_opponents, container, false);

        initUI(view);

        initOpponentListAdapter();

        return view;
    }

    private void initOpponentListAdapter() {
        List<QBUser> userList = new ArrayList<>(((GroupCallActivity) getActivity()).getOpponentsList());
        prepareUserList(opponentsList, userList);
    }

    private void prepareUserList(ListView opponentsList, List<QBUser> users) {
        int i = searchIndexLogginedUser(users);
        if (i >= 0)
            users.remove(i);

        // Prepare users list for simple adapter.
        opponentsAdapter = new OpponentsAdapter(getActivity(), users);
        opponentsList.setAdapter(opponentsAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        Log.d(TAG, "onCreate() from OpponentsFragment");
        super.onCreate(savedInstanceState);
    }

    private void initUI(View view) {
        btnAudioCall = (Button) view.findViewById(R.id.btnAudioCall);
        btnVideoCall = (Button) view.findViewById(R.id.btnVideoCall);
        btnAudioCall.setOnClickListener(this);
        btnVideoCall.setOnClickListener(this);
        opponentsList = (ListView) view.findViewById(R.id.opponentsList);
    }

    @Override
    public void onClick(View v) {

        if (opponentsAdapter.getSelected().isEmpty()) {
            M.T(getActivity(), "Choose one opponent");
            return;
        }

        if (opponentsAdapter.getSelected().size() > QBRTCConfig.getMaxOpponentsCount()) {
            M.T(getActivity(), "Max number of opponents is 4");
            return;
        }
        QBRTCTypes.QBConferenceType qbConferenceType = null;
        //Init conference type
        switch (v.getId()) {
            case R.id.btnAudioCall:
                qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;
                CoreSharedHelper.getInstance().savePref(Constants.AUDIOGROUPCALL, "true");
                CoreSharedHelper.getInstance().savePref(Constants.VIDEOONETOONECALL,"false");
                CoreSharedHelper.getInstance().savePref(Constants.AUDIOONETOONECALL,"false");
                break;

            case R.id.btnVideoCall:
                // get call type
                qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;
                CoreSharedHelper.getInstance().savePref(Constants.VIDEOGROUPCALL, "true");
                CoreSharedHelper.getInstance().savePref(Constants.VIDEOONETOONECALL,"false");
                CoreSharedHelper.getInstance().savePref(Constants.AUDIOONETOONECALL,"false");
                break;
        }

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("any_custom_data", "some data");
        userInfo.put("my_avatar_url", "avatar_reference");

        ((GroupCallActivity) getActivity())
                .addConversationFragmentStartCall(opponentsAdapter.getSelected(),
                        qbConferenceType, userInfo);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_opponents, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                ((GroupCallActivity) getActivity()).showSettings();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static int searchIndexLogginedUser(List<QBUser> usersList) {
        int indexLogginedUser = -1;
        for (QBUser usr : usersList) {
            if (usr.getLogin().equals(AppSession.getSession().getUser().getLogin())) {
                indexLogginedUser = usersList.indexOf(usr);
                break;
            }
        }
        return indexLogginedUser;
    }
}
