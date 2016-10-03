package com.ss.fun2sh.ui.activities.groupcall.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.TextUtils;

import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.base.BaseLoggableActivity;
import com.ss.fun2sh.ui.activities.groupcall.fragments.SettingsFragment;


/**
 * QuickBlox team
 */
public class SettingsActivity extends BaseLoggableActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    private static final int MAX_VIDEO_START_BITRATE = 2000;
    private String bitrateStringKey;
    private SettingsFragment settingsFragment;

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.group_activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, settingsFragment)
                .commit();
        bitrateStringKey = getString(R.string.pref_startbitratevalue_key);
    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences =
                settingsFragment.getPreferenceScreen().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences =
                settingsFragment.getPreferenceScreen().getSharedPreferences();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(bitrateStringKey)) {
            String bitrateValue = sharedPreferences.getString(bitrateStringKey,
                    getString(R.string.pref_startbitratevalue_default));
            if (TextUtils.isEmpty(bitrateValue)) {
                M.T(SettingsActivity.this, "Value can't be empty:");
                setDefaultstartingBitrate(sharedPreferences);
                return;
            }
            int startBitrate = Integer.parseInt(bitrateValue);
            if (startBitrate > MAX_VIDEO_START_BITRATE) {
                M.T(this, "Max value is:" + MAX_VIDEO_START_BITRATE);
                setDefaultstartingBitrate(sharedPreferences);
            }
        }
    }

    private void setDefaultstartingBitrate(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(bitrateStringKey,
                getString(R.string.pref_startbitratevalue_default));
        editor.apply();
        updateSummary(sharedPreferences, bitrateStringKey);
    }

    private void updateSummary(SharedPreferences sharedPreferences, String key) {
        Preference updatedPref = settingsFragment.findPreference(key);
        // Set summary to be the user-description for the selected value
        if (updatedPref instanceof EditTextPreference) {
            ((EditTextPreference) updatedPref).setText(sharedPreferences.getString(key, ""));
        } else {
            updatedPref.setSummary(sharedPreferences.getString(key, ""));
        }
    }
}
