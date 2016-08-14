package com.ss.fun2sh;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.karumi.dexter.Dexter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBSettings;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.ss.fun2sh.utils.ActivityLifecycleHandler;
import com.ss.fun2sh.utils.StringObfuscator;
import com.ss.fun2sh.utils.helpers.SharedHelper;
import com.ss.fun2sh.utils.image.ImageLoaderUtils;

public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();

    private RequestQueue mRequestQueue;
    private SharedHelper appSharedHelper;

    private static AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        //Fabric.with(this, new Crashlytics());
        Dexter.initialize(this);
        initApplication();
        new PrefsHelper(this);
        registerActivityLifecycleCallbacks(new ActivityLifecycleHandler());
    }

    private void initApplication() {
        mInstance = this;
        // initilize qb and database here
        initQb();
        initDb();
        initImageLoader(this);
    }

    private void initQb() {
        QBChatService.setDebugEnabled(StringObfuscator.getDebugEnabled());
        QBSettings.getInstance().fastConfigInit(
                StringObfuscator.getApplicationId(),
                StringObfuscator.getAuthKey(),
                StringObfuscator.getAuthSecret());
    }

    private void initDb() {
        DataManager.init(this);
    }

    private void initImageLoader(Context context) {
        ImageLoader.getInstance().init(ImageLoaderUtils.getImageLoaderConfiguration(context));
    }

    public synchronized SharedHelper getAppSharedHelper() {
        return appSharedHelper == null
                ? appSharedHelper = new SharedHelper(this)
                : appSharedHelper;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
