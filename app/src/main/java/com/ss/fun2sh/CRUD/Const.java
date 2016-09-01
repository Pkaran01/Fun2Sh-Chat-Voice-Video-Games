package com.ss.fun2sh.CRUD;

import android.graphics.Bitmap;

/**
 * Created by CRUD Technology on 2/11/2016.
 */
public class Const {


    public static String NOTIF_IMGPATH = "http://image.dabank.co.uk/";
    public static String FORWARD_MESSAGE = null;
    public static String TIME_OUT = "No reply from server due to Internet Connection Problem";
    public static Bitmap previewImage;

    public interface App_Ver {
        String isFirstTime = "isFirstTime";
        String isFirstTimeLogin = "isFirstTimeLogin";
        String userId = "userId";
        String pwd = "pwd";
        String userData = "userData";
        String reg_id = "REGID";
        String reg_type = "REGTYPE";
        String expire_date = "expire_date";
        String secondTimeLogin = "secondtimelogin";
        String firstTimeProfile = "firsttimeProfile";
        String QBPassword = "1234567890";
        String LAST_AVATAR_URL = "last_avatar_url";
    }

    public interface URL {
        //        String host_url = "http://mobileapp.dabank.co.uk/Fun2ShNMobileServices/Fun2ShMService.svc/";
        String host_url = "http://mobileapp.dabank.co.uk/Fun2ShMobileServices/Fun2ShMService.svc/";
        String signUp = host_url + "Registration"; // post
        String login = host_url + "LoginStatus"; //post
        String profilePackage = host_url + "ProfilePackage"; //?get
        String forgotPwd = host_url + "Forgotpwd";
        String editPerInfo = host_url + "EditPerInfo";
        String packageDetails = host_url + "PackageDetails";
        String updatePersonalDetails = host_url + "UpdatePersonalDetails";
        String getCountry = host_url + "Country";
        String country2States = host_url + "County2States";
        String state2City = host_url + "State2City";
        String SITE_CONTENT = host_url + "SiteContent";
        String logout = host_url + "Logout";
        String SITE = "SiteContent";
        String TERMS = "Termsofuse_FUN";
        String POLICIES = "Policies_FUN";
        String ABOUT_US = "AboutUs_FUN";
        String PROFILE_DETAILS = host_url + "ProfilePackageDetails";


//        String COUNTRY2STATES_POSTMTD = "County2States";
//        String STATES2CITY_POSTMTD = "State2City";
    }
}
