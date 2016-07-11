package com.ss.fun2sh.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.rest.QBLogoutCompositeCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.ss.fun2sh.Adapter.NavDrawerListAdapter;
import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.Helper;
import com.ss.fun2sh.CRUD.JSONParser;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.CRUD.NetworkUtil;
import com.ss.fun2sh.CRUD.PrefsHelper;
import com.ss.fun2sh.CRUD.Utility;
import com.ss.fun2sh.Model.NavDrawerItem;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.base.BaseLoggableActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DashBoardActivity extends BaseLoggableActivity {

    ImageView navigationDrawer;
    DrawerLayout mDrawerLayout;
    ListView mDrawerList;

    // slide menu items
    String[] navMenuTitles;
    TypedArray navMenuIcons;

    ArrayList<NavDrawerItem> navDrawerItems;
    NavDrawerListAdapter adapter;
    String regType;
    private android.widget.TextView dashBordUserName;
    private android.widget.TextView dashBordDateTime;
    private ImageView dashBordPkgImage;

    public static void start(Context context) {
        Intent intent = new Intent(context, DashBoardActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.dashBordPkgImage = (ImageView) findViewById(R.id.dashBordPkgImage);
        this.dashBordDateTime = (TextView) findViewById(R.id.dashBordDateTime);
        this.dashBordUserName = (TextView) findViewById(R.id.dashBordUserName);
        this.navigationDrawer = (ImageView) findViewById(R.id.navigationDrawer);
        M.E(PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userData).toString());
//        Bundle b=getIntent().getExtras();
//        String regType=b.getString("regType");
//        M.T(this,regType);

        regType = PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.reg_type);
//        M.T(DashBoardActivity.this,regType);
        addActions();
        if (regType.equals("FREE")) {
            dashBordPkgImage.setImageResource(R.drawable.cardfree);
        } else if (regType.equals("STANDARD")) {
            dashBordPkgImage.setImageResource(R.drawable.standard);
        } else {
            dashBordPkgImage.setImageResource(R.drawable.premium);
        }

        dashBordDateTime.setText(Utility.getHomeCurrentDateTime());
        dashBordUserName.setText(PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId).toString());
        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[7], navMenuIcons.getResourceId(7, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[8], navMenuIcons.getResourceId(8, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[9], navMenuIcons.getResourceId(9, -1)));
        // Recycle the typed array
        navMenuIcons.recycle();

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                Fragment fragment;

                if (position == 0) {
                    if (NetworkUtil.getConnectivityStatus(DashBoardActivity.this)) {
                        fragment = null;
                        startActivity(new Intent(DashBoardActivity.this, DashBoardActivity.class));
                        DashBoardActivity.this.finish();
                    } else {
                        fragment = null;
                        M.dError(DashBoardActivity.this, "Unable to connect internet !");
                    }
                } else if (position == 1) {
                    if (NetworkUtil.getConnectivityStatus(DashBoardActivity.this)) {
                        fragment = new FragProfile();
                        ft.addToBackStack("FragProfile");
                    } else {
                        fragment = null;
                        M.dError(DashBoardActivity.this, "Unable to connect internet !");
                    }
                } else if (position == 2) {
                    fragment = null;
                    if (NetworkUtil.getConnectivityStatus(DashBoardActivity.this)) {
                        Intent i = new Intent(DashBoardActivity.this, WebsiteLandScap.class);
                        i.putExtra("url", "http://game.fun-joy.co.uk/");
//                        String user=PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId);
//                        String pwd=PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd);
//                        i.putExtra("url", "http://game.fun-joy.co.uk?IDNO="+user+"&PWD="+pwd);
//                        i.putExtra("url", "http://game.fun-joy.co.uk?IDNO=a4abim&PWD=1234");
//                        i.putExtra("url", "http://game.fun-joy.co.uk?IDNO="+user+"&PWD="+pwd);
                        DashBoardActivity.this.startActivity(i);
                    } else {
                        M.dError(DashBoardActivity.this, "Unable to connect internet !");
                    }
                } else if (position == 3) {
                    fragment = null;
                    if (NetworkUtil.getConnectivityStatus(DashBoardActivity.this)) {
                        Intent i = new Intent(DashBoardActivity.this, WebsiteLandScap.class);
                        String user = PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId);
                        String pwd = PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd);
                        i.putExtra("url", " http://ftvox.fun2shmedia.com/login_new.php?username=" + user + "&password=" + pwd);
                        DashBoardActivity.this.startActivity(i);
                    } else {
                        M.dError(DashBoardActivity.this, "Unable to connect internet !");
                    }
                } else if (position == 4) {
                    fragment = null;
                    M.I(DashBoardActivity.this, com.ss.fun2sh.ui.activities.authorization.SplashActivity.class, null);
                } else if (position == 5) {
                    fragment = new FragHelp();
                    ft.addToBackStack("FragHelp");
                } else if (position == 6) {
                    fragment = new FragTerms();
                    ft.addToBackStack("FragTerms");
                } else if (position == 7) {
                    fragment = new FragAboutUs();
                    ft.addToBackStack("FragAboutUs");
                } else if (position == 8) {
                    fragment = new FragPrivacy();
                    ft.addToBackStack("FragPrivacy");
                } else {
                    fragment = null;
                    SweetAlertDialog sweetAlertDialog = M.dConfirem(DashBoardActivity.this, "You are Signing Out", "Yes", "No");
                    sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                            JSONObject param = new JSONObject();
                            try {
                                param.put("IDNO", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
                                param.put("APPTYPE", "Fun2sh");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            M.E(param.toString());
                            new JSONParser(DashBoardActivity.this).parseVollyJSONObject(Const.URL.logout, 1, param, new Helper() {
                                @Override
                                public void backResponse(String response) {
                                    try {
                                        JSONObject res = new JSONObject(response);
                                        if (res.getString("MSG").equals("SUCCESS")) {
                                            QBLogoutCompositeCommand.start(DashBoardActivity.this);
                                            PrefsHelper.getPrefsHelper().delete(Const.App_Ver.isFirstTimeLogin);
                                            PrefsHelper.getPrefsHelper().delete(Const.App_Ver.firstTimeProfile);
                                            PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.secondTimeLogin, true);
                                           // M.T(DashBoardActivity.this, res.getString("MESSAGE"));
                                            /*M.I(DashBoardActivity.this, LogoutActivity.class, null);
                                            DashBoardActivity.this.finish();*/

                                        } else {
                                            M.T(DashBoardActivity.this, res.getString("MESSAGE"));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                    sweetAlertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                        }
                    });

                }
                if (fragment != null) {
                    ft.replace(R.id.frame_container, fragment);
                    ft.commit();
                }
                DashBoardActivity.this.mDrawerLayout.closeDrawer(GravityCompat.END);

                String reg_type = PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.reg_type);
//                M.T(getActivity(),"reg_type==="+reg_type);
            }
        });
        navigationDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        });

        DashBoardActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                getPackageDetails();
            }
        });

        clearBackStack();
//        load_home();
    }

    //qb
    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    private void addActions() {
        addAction(QBServiceConsts.LOGOUT_SUCCESS_ACTION, new LogoutSuccessAction());
        addAction(QBServiceConsts.LOGOUT_FAIL_ACTION, failAction);

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.LOGOUT_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOGOUT_FAIL_ACTION);

        updateBroadcastActionList();
    }

    private void getPackageDetails() {

//        M.T(DashBoardActivity.this,"getPackageDetails");
        new JSONParser(DashBoardActivity.this).parseVollyJSONObjectL(Const.URL.profilePackage, 1, Utility.getParam(), new Helper() {
            @Override
            public void backResponse(String response) {
                try {
                    JSONObject data = new JSONObject(response);
                    M.E("profile pkg " + response);
                    if (data.getString("MSG").equals("SUCCESS")) {
//                        if (data.getString("PREMIUM").equals("FREE")) {
//                            dashBordPkgImage.setImageResource(R.drawable.cardfree);
////                            dashBordPkgImage.setImageResource(R.drawable.standard);
//                        } else if (data.getString("PREMIUM").equals("STANDARD")) {
//                            dashBordPkgImage.setImageResource(R.drawable.premium);
//                        } else {
//                            dashBordPkgImage.setImageResource(R.drawable.premium);
//                        }
//                        if (regType.equals("FREE")) {
//                            dashBordPkgImage.setImageResource(R.drawable.cardfree);
////                            dashBordPkgImage.setImageResource(R.drawable.standard);
//                        } else if (regType.equals("STANDARD")) {
//                            dashBordPkgImage.setImageResource(R.drawable.premium);
//                        } else {
//                            dashBordPkgImage.setImageResource(R.drawable.premium);
//                        }
                        dashBordUserName.setText(data.getString("FULLNAME"));
                    }
                } catch (JSONException e) {
                    M.E(e.getMessage());
                }
            }
        });
    }

    public void load_home() {
        FragHome home = new FragHome();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.frame_container, home);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    public void clearBackStack() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        while (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStackImmediate();
        }

        load_home();
    }

    private class LogoutSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            startLandingScreen();
            M.T(DashBoardActivity.this, "Logout successfully");
        }
    }
}
