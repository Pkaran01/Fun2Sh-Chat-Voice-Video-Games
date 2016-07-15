package com.ss.fun2sh.ui.activities.main;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.ss.fun2sh.Activity.DashBoardActivity;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;
import com.ss.fun2sh.gcm.GSMHelper;
import com.ss.fun2sh.ui.activities.base.BaseLoggableActivity;
import com.ss.fun2sh.ui.activities.chats.NewMessageActivity;
import com.ss.fun2sh.ui.activities.profile.MyProfileActivity;
import com.ss.fun2sh.ui.adapters.NavigationDrawerAdapter;
import com.ss.fun2sh.ui.adapters.ViewPagerAdapter;
import com.ss.fun2sh.ui.fragments.chats.DialogsListFragment;
import com.ss.fun2sh.ui.fragments.chats.GroupDialogsListFragment;
import com.ss.fun2sh.ui.fragments.fun.CallFragment;
import com.ss.fun2sh.ui.fragments.fun.FavouriteFragment;
import com.ss.fun2sh.utils.image.ImageLoaderUtils;

import butterknife.Bind;

public class MainActivity extends BaseLoggableActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static DrawerLayout drawer;
    public static LinearLayout drawer_view;
    @Bind(R.id.userImage)
    CircularImageView userImage;
    @Bind(R.id.userName)
    TextView userName;
    @Bind(R.id.textStatus)
    TextView textStatus;
    @Bind(R.id.toolbartTitle)
    TextView toolbartTitle;
    FragmentManager mFragmentManager;
    ArrayAdapter<String> navigationadapter;
    ListView navigation_drawer_list;
    NavigationDrawerAdapter navigationDrawerAdapter;
    Dialog navDialog;
    ViewPagerAdapter adapter;
    ImageView myCustomIcon;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.chaticonxml,
            R.drawable.groupxml,
            R.drawable.callxml,
            R.drawable.favrouitsxml,
            R.drawable.contactsxml
    };
    private GSMHelper gsmHelper;
    private ImportFriendsSuccessAction importFriendsSuccessAction;
    private ImportFriendsFailAction importFriendsFailAction;

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_qb_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields(savedInstanceState);

        setUpActionBarWithUpButton();
        checkGCMRegistration();
        M.E("MainActivity pe" + CoreSharedHelper.getInstance().getPushRegistrationId());
        if (!isChatInitializedAndUserLoggedIn()) {
            loginChat();
        } else {
            checkImportFriends();
        }
        setActionBarIcon();

        if (!isNetworkAvailable()) {
            setActionBarTitle(getString(R.string.dlg_internet_connection_is_missing));
            setActionBarIcon(null);
        } else {
            setActionBarTitle("");
            checkVisibilityUserIcon();
        }

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //imageZoomDialog();
                MyProfileActivity.start(MainActivity.this);
            }
        });


    }

    private void initFields(Bundle savedInstanceState) {
        title = " " + AppSession.getSession().getUser().getFullName();
        userName.setText(title);
        gsmHelper = new GSMHelper(this);
        importFriendsSuccessAction = new ImportFriendsSuccessAction();
        importFriendsFailAction = new ImportFriendsFailAction();
        // searchIcon = (SearchView) toolbar.findViewById(R.id.searchIcon);
        mFragmentManager = getSupportFragmentManager();
        viewPager = (ViewPager) findViewById(R.id.container_fragment);
        navigation_drawer_list = (ListView) findViewById(R.id.navigation_drawer_list);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer_view = (LinearLayout) findViewById(R.id.drawer_view);
        navigationDrawerAdapter = new NavigationDrawerAdapter(this, getResources().getStringArray(R.array.chatarray));
        navigation_drawer_list.setAdapter(navigationDrawerAdapter);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        toolbartTitle.setText("Chats");
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        int tabCount = adapter.getCount(); //Assuming you have already somewhere set the adapter for the ViewPager

        for (int i = 0; i < tabCount; i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                myCustomIcon = (ImageView) LayoutInflater.from(tabLayout.getContext()).inflate(R.layout.my_custom_tab, null);
                myCustomIcon.setImageResource(tabIcons[i]);
                tab.setCustomView(myCustomIcon);

            }
        }

        viewPager.setCurrentItem(0);
        tabLayout.getTabAt(0).getCustomView().setSelected(true);

        navigation_drawer_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 2) {
                    MyProfileActivity.start(MainActivity.this);
                } else if (position == 0) {
                    Intent intent = new Intent(MainActivity.this, DashBoardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    MainActivity.this.startActivity(intent);
                }

            }
        });

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                                               @Override
                                               public void onTabSelected(TabLayout.Tab tab) {
                                                   viewPager.setCurrentItem(tab.getPosition());
                                                   if (tab.getPosition() == 0) {

                                                       toolbartTitle.setText("Chats");
                                                       navigationDrawerAdapter = new NavigationDrawerAdapter(MainActivity.this, getResources().getStringArray(R.array.chatarray));
                                                       navigation_drawer_list.setAdapter(navigationDrawerAdapter);
                                                       navigation_drawer_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                           @Override
                                                           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                               if (position == 2) {
                                                                   MyProfileActivity.start(MainActivity.this);
                                                               } else if (position == 0) {
                                                                   Intent intent = new Intent(MainActivity.this, DashBoardActivity.class);
                                                                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                   MainActivity.this.startActivity(intent);

                                                               }
                                                           }
                                                       });
                                                   } else if (tab.getPosition() == 1) {
                                                       toolbartTitle.setText("Groups");
                                                       navigationDrawerAdapter = new NavigationDrawerAdapter(MainActivity.this, getResources().getStringArray(R.array.grouparray));
                                                       navigation_drawer_list.setAdapter(navigationDrawerAdapter);
                                                       navigation_drawer_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                           @Override
                                                           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                               if (position == 2) {
                                                                   MyProfileActivity.start(MainActivity.this);
                                                               } else if (position == 0) {
                                                                   Intent intent = new Intent(MainActivity.this, DashBoardActivity.class);
                                                                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                   MainActivity.this.startActivity(intent);

                                                               }
                                                           }
                                                       });
                                                   } else if (tab.getPosition() == 2) {
                                                       toolbartTitle.setText("Calls");
                                                       navigationDrawerAdapter = new NavigationDrawerAdapter(MainActivity.this, getResources().getStringArray(R.array.callarray));
                                                       navigation_drawer_list.setAdapter(navigationDrawerAdapter);
                                                       navigation_drawer_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                           @Override
                                                           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                               if (position == 3) {
                                                                   MyProfileActivity.start(MainActivity.this);
                                                               } else if (position == 0) {
                                                                   Intent intent = new Intent(MainActivity.this, DashBoardActivity.class);
                                                                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                   MainActivity.this.startActivity(intent);

                                                               }
                                                           }
                                                       });
                                                   } else if (tab.getPosition() == 3) {
                                                       toolbartTitle.setText("Favourite");
                                                       navigationDrawerAdapter = new NavigationDrawerAdapter(MainActivity.this, getResources().getStringArray(R.array.favroitearray));
                                                       navigation_drawer_list.setAdapter(navigationDrawerAdapter);
                                                       navigation_drawer_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                           @Override
                                                           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                               if (position == 3) {
                                                                   MyProfileActivity.start(MainActivity.this);
                                                               } else if (position == 0) {
                                                                   Intent intent = new Intent(MainActivity.this, DashBoardActivity.class);
                                                                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                   MainActivity.this.startActivity(intent);

                                                               }
                                                           }
                                                       });
                                                   } else if (tab.getPosition() == 4) {
                                                       toolbartTitle.setText("Contacts");
                                                       navigationDrawerAdapter = new NavigationDrawerAdapter(MainActivity.this, getResources().getStringArray(R.array.contactarray));
                                                       navigation_drawer_list.setAdapter(navigationDrawerAdapter);
                                                       navigation_drawer_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                           @Override
                                                           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                               if (position == 2) {
                                                                   MyProfileActivity.start(MainActivity.this);
                                                               } else if (position == 0) {
                                                                   Intent intent = new Intent(MainActivity.this, DashBoardActivity.class);
                                                                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                   MainActivity.this.startActivity(intent);

                                                               }
                                                           }
                                                       });
                                                   }

                                               }

                                               @Override
                                               public void onTabUnselected(TabLayout.Tab tab) {

                                               }

                                               @Override
                                               public void onTabReselected(TabLayout.Tab tab) {

                                               }
                                           }

        );
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        tabLayout.getTabAt(3).setIcon(tabIcons[3]);
        tabLayout.getTabAt(4).setIcon(tabIcons[4]);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new DialogsListFragment());
        adapter.addFragment(new GroupDialogsListFragment());
        adapter.addFragment(new CallFragment());
        adapter.addFragment(new FavouriteFragment());
        adapter.addFragment(new NewMessageActivity());
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    protected void onResume() {
        actualizeCurrentTitle();
        super.onResume();
        addActions();
        checkGCMRegistration();
    }

    private void actualizeCurrentTitle() {
        if (AppSession.getSession().getUser().getFullName() != null) {
            title = " " + AppSession.getSession().getUser().getFullName();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeActions();
    }

    @Override
    protected void checkShowingConnectionError() {
        if (!isNetworkAvailable()) {
            setActionBarTitle(getString(R.string.dlg_internet_connection_is_missing));
            setActionBarIcon(null);
        } else {
            setActionBarTitle("");
            checkVisibilityUserIcon();
        }
    }

    @Override
    protected void performLoginChatSuccessAction(Bundle bundle) {
        super.performLoginChatSuccessAction(bundle);
        actualizeCurrentTitle();
        checkImportFriends();
    }

    private void addActions() {
        addAction(QBServiceConsts.IMPORT_FRIENDS_SUCCESS_ACTION, importFriendsSuccessAction);
        addAction(QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION, importFriendsFailAction);
        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.IMPORT_FRIENDS_SUCCESS_ACTION);
        removeAction(QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION);
        updateBroadcastActionList();
    }

    private void setActionBarIcon() {
        setActionBarIcon(R.drawable.funchatloginlogo2);
    }

    private void checkVisibilityUserIcon() {
        UserCustomData userCustomData = Utils.customDataToObject(AppSession.getSession().getUser().getCustomData());
        if (!TextUtils.isEmpty(userCustomData.getAvatar_url())) {
            loadLogoActionBar(userCustomData.getAvatar_url(), userCustomData.getStatus());
        } else {
            userImage.setImageResource(R.drawable.avatarprofile);
        }
    }

    private void loadLogoActionBar(String logoUrl, String staus) {
        if(staus !=null || staus.length()>0) {
            textStatus.setText(staus);
        }else {
            textStatus.setText(getString(R.string.dummy_status));
        }
        userName.setText(title);
        ImageLoader.getInstance().loadImage(logoUrl, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedBitmap) {
                        userImage.setImageBitmap(loadedBitmap);
                    }
                });
    }

    private void performImportFriendsSuccessAction() {
        appSharedHelper.saveUsersImportInitialized(true);
        hideProgress();
    }

    private void checkGCMRegistration() {
        if (gsmHelper.checkPlayServices()) {
            if (!gsmHelper.isDeviceRegisteredWithUser()) {
                gsmHelper.registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    private void performImportFriendsFailAction(Bundle bundle) {
        performImportFriendsSuccessAction();
    }


    private void checkImportFriends() {
        Log.d("IMPORT_FRIENDS", "checkImportFriends()");
        if (!appSharedHelper.isUsersImportInitialized()) {
            showProgress();
        }
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private class ImportFriendsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            performImportFriendsSuccessAction();
        }
    }

    private class ImportFriendsFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            performImportFriendsFailAction(bundle);
        }
    }
}