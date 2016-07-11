package com.ss.fun2sh.utils;


import com.ss.fun2sh.BuildConfig;

public class StringObfuscator {

    public static String getApplicationId() {
        return BuildConfig.APP_ID;
    }

    public static String getAuthKey() {
        return BuildConfig.AUTH_KEY;
    }

    public static String getAuthSecret() {
        return BuildConfig.AUTH_SECRET;
    }

    public static String getAppVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public static boolean getDebugEnabled() {
        return BuildConfig.DEBUG;
    }

    public static String getPushRegistrationAppId(){
        return BuildConfig.PUSH_REGISTRATION_APP_ID;
    }

}