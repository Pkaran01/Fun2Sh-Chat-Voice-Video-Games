package com.ss.fun2sh.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.PrefsHelper;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.authorization.BaseAuthActivity;

public class LogoutActivity extends BaseAuthActivity {


    private android.widget.TextView userName;

    public static void start(Context context, Intent intent) {
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_logout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.userName = (TextView) findViewById(R.id.userName);
        userName.setText(PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId) + " signing off");
    }
}
