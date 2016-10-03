package com.ss.fun2sh.ui.activities.groupcall.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.ss.fun2sh.R;


/**
 * QuickBlox team
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
