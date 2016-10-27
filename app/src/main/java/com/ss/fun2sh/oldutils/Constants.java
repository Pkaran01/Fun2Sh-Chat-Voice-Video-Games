package com.ss.fun2sh.oldutils;

import android.os.Environment;

public class Constants {


//	public static String SERVER_ADDRESS = "http://mobileapp.dabank.co.uk/DBANKMobileServices/DBANKMService.svc/";

    public static String SERVER_ADDRESS = "http://mobileapp.dabank.co.uk/Fun2ShMobileServices/Fun2ShMService.svc/";


    // public static String ACTION_SIGNOUT = "action_signout";

    public static String SUPPORT = "Support";
    public static String TANDC = "T & C";
    public static String PRIVACY_POLICY = "Privacy Policy";

    public static String ACTION_SIGNOUT = "SignOut";
    public static String ACTION_HOME = "action_home";
    public static String ACTION_PROFILE = "action_profile";
    public static String ACTION_FUNGAMES = "action_fungames";
    public static String ACTION_FUNTUBE = "action_funtube";
    public static String ACTION_FUNCHAT = "action_funchat";

    public static String ACTION_SETTINGS = "action_settings";

    public static String ENCRIPTION_KEY = "8!c9r3ORIfeWr%^tt]nXQ";

    public static final String RECORDS_NOT_FOUND = "No Records Found";
    public static String CMNG_SOON = "Coming Soon...";

    public static String ACCEPT = "Accept";
    public static String CONTENT_TYPE = "Content-type";
    public static String CONTENT = "application/json";
    public static String NO_INTERNET = "Please connect to working Internet connection";
    public static String TIME_OUT = "No reply from server due to Internet Connection Problem";

    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";

    public static boolean BACKPRESS_STARTED = false;


    public static final String LOGINSTATUS_POSTMTD = "LoginStatus";
    public static final String REGISTRATION_POSTMTD = "Registration";

    public static final String FORGOTPWD_POSTMTD = "Forgotpwd";

    public static final String PROFILEPACKAGE_POSTMTD = "ProfilePackage";

    public static final String EDITPERINFO_POSTMTD = "EditPerInfo";

    public static final String UPDATEPERSONALDETAILS_POSTMTD = "UpdatePersonalDetails";


    public static final String PACKAGEDETAILS_POSTMTD = "PackageDetails";


    public static final String PHOTOUPLOAD_POSTMTD = "photoupload";

    public static final String ADDFRIEND_POSTMTD = "AddFriend";

    public static final String FRIENDSLIST_POSTMTD = "FriendsList";

    public static final String REMOVEFRIEND_POSTMTD = "RemoveFriend";


    public static final String COUNTRY_POSTMTD = "Country";
    public static final String COUNTRY2STATES_POSTMTD = "County2States";

    public static final String STATES2CITY_POSTMTD = "State2City";
    public static final String PROFILE_DETAILS = "ProfilePackageDetails";


//	public static final String PROFILEPKG_POSTMTD = "ProfilePackage";

    public static final String PERSONALINFO_POSTMTD = "Personalinfo";    // |  ONLY ONE
    public static final String EDITPERSONALINFO_POSTMTD = "EditPerInfo";//  |  USE


    public static String NOTIF_IMGPATH = "http://image.dabank.co.uk/";


    public static final String SELECT_COUNTRY = "Select your country residence";
    public static final String SELECT_STATE = "Select State / Province";
    public static final String SELECT_CITY = "Select City / Town / District";


    public static final int REQUEST_CHOOSE_PHOTO = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 3;

    public static String LOCAL_FILE_STORAGE_PATH = Environment
            .getExternalStorageDirectory() + "/Fun2sh";


//	public static String FTP_HOST="109.200.0.227";
//	public static String FTP_USER="Avatars";
//	public static String FTP_PWD= "Vpa8h2~0";

    public static String FILE_TRANS_COMPLETE = "completed";
    public static String FILE_TRANS_STARTED = "started";
    public static String FILE_TRANS_FAILED = "failed";
    public static String FILE_TRANS_FAILED_MSG = "File Transfer failed due to Internet Connection Problem";
    public static String PHOTO_UPLOAD_FAILED = "Photo upload failed";

    //FTP
    public static final String FTP_PHOTOHOST = "31.3.229.82";
    public static final String FTP_PHOTOUSER = "FunjoyMail";
    public static final String FTP_PHOTOPASS = "Waol1!12";

    //Help

    public static final String HELP_DEPT = "Department";
    public static final String HELP_SUB = "Subject";
    public static final String HELP_OPEN_CLOSE = "Openclosetickets";
    public static final String HELP_ADMIN = "Adminnotification";
    public static final String HELP_NEW = "Newticket";
    public static final String HELP_VIEW = "Viewticket";
    public static final String HELP_REPLY = "Replyticket";


    public static final String TKTSLNO = "TKTSLNO";
    public static final String TKTNO = "TKTNO";
    public static final String LASTUPDATE = "LASTUPDATE";
    public static final String SUBJECT = "SUBJECT";
    public static final String RESAWAIT = "RESAWAIT";
    public static final String DEPTNAM = "DEPTNAME";

    //support--view tickets
    public static final String ATTCHEMENT = "ATTCHEMENT";
    public static final String ATTACHMENT = "ATTACHMENT";
    public static final String HEADING = "HEADING";
    public static final String MESSAGE = "MESSAGE";

    public static final String ATTNAME = "ATTACHMENT";
    public static final String TKTACTION = "TKTACTION";
    public static final String SITE = "SITE";
    public static final String NODATA = "Data is not getting from the server";

    public static final String AUDIOONETOONECALL = "audioonetoonecall";
    public static final String VIDEOONETOONECALL = "videoonetoonecall";
    public static final String AUDIOGROUPCALL = "audiogroupcall";
    public static final String VIDEOGROUPCALL = "videogroupcall";
    public static final String FUULSCREENPREF = "forclick";
    public static final String USERPICAUDIOPREF = "audiogroupuserpic";
    public static final String USERFULLVIDEO = "userfullvideo";
    public static final String USERORIENTATIONAUDIOPREF = "orientationforgroup";


}
