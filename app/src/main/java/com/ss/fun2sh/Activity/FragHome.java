package com.ss.fun2sh.Activity;


import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.CRUD.NetworkUtil;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.ss.fun2sh.R;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by CRUD Technology on 5/27/2016.
 */
public class FragHome extends Fragment {

    String user, pwd;
    private GifImageView game;
    private GifImageView tube, chat, updateProfile, upgradePackage;
    private VideoView videoView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_home, container, false);
        this.tube = (GifImageView) v.findViewById(R.id.funTube);
        this.game = (GifImageView) v.findViewById(R.id.funGame);
        this.chat = (GifImageView) v.findViewById(R.id.funChat);
        this.updateProfile = (GifImageView) v.findViewById(R.id.funProfile);
        this.upgradePackage = (GifImageView) v.findViewById(R.id.dashBordUpgradePackage);
        this.videoView = (VideoView) v.findViewById(R.id.videoView);

        String path = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.dashbord;
        videoView.setVideoURI(Uri.parse(path));
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        final String reg_type = PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.reg_type);


        user = PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId);
        pwd = PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd);

        game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.getConnectivityStatus(getActivity())) {
                    Intent i = new Intent(getActivity(), WebsiteLandScap.class);
                    i.putExtra("url", "http://game.fun-joy.co.uk/");
//                    i.putExtra("url", "http://game.fun-joy.co.uk?IDNO="+user+"&PWD="+pwd);
                    getActivity().startActivity(i);
                } else {
                    M.dError(getActivity(), "Unable to connect internet !");
                }
            }
        });
        tube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.getConnectivityStatus(getActivity())) {
                    Intent i = new Intent(getActivity(), WebsiteLandScap.class);
                    i.putExtra("url", " http://ftvox.fun2shmedia.com/login_new.php?username=" + user + "&password=" + pwd);
                    getActivity().startActivity(i);
                } else {
                    M.dError(getActivity(), "Unable to connect internet !");
                }
            }
        });
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                M.I(getActivity(), com.ss.fun2sh.ui.activities.authorization.SplashActivity.class, null);

            }

        });
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.getConnectivityStatus(getActivity())) {
                    FragProfile profile = new FragProfile();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    android.support.v4.app.FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.addToBackStack("FragProfile");
                    ft.replace(R.id.frame_container, profile);
                    ft.commit();
                } else {
                    M.dError(getActivity(), "Unable to connect internet !");
                }
            }
        });
        upgradePackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reg_type.equals("PREMIUM")) {
                    M.dError(getActivity(), "No further Upgrade is available");
                } else {
                    FragPackageUpgrade profile = new FragPackageUpgrade();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.addToBackStack("FragPackageUpgrade");
                    ft.replace(R.id.frame_container, profile);
                    ft.commit();
                }

            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.videoView.start();
    }
}
