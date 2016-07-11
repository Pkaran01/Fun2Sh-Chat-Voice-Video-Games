package com.ss.fun2sh.CRUD;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.ss.fun2sh.R;

/**
 * Created by CRUD Technology on 11/25/2015.
 */
public class Utility {

    public static String getCurrentDateTime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd-yyyy");//dd/MM/yyyy
        Calendar cal = Calendar.getInstance();
        String strDate = sdfDate.format(cal.getTime());
        return strDate;
    }

    public static String getHomeCurrentDateTime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy hh:mm aa");//dd/MM/yyyy
        Calendar cal = Calendar.getInstance();
        String strDate = sdfDate.format(cal.getTime());
        return strDate;
    }


    public static void loginVideo(VideoView videoView, Context cx) {
        String path = "android.resource://" + cx.getPackageName() + "/" + R.raw.login;
        videoView.setVideoURI(Uri.parse(path));
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
    }

    /**
     * Returns MAC address of the given interface name.
     *
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return mac address or empty string
     */
    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) return "";
                StringBuilder buf = new StringBuilder();
                for (int idx = 0; idx < mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
        /*try {
            // this is so Linux hack
            return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
        } catch (IOException ex) {
            return null;
        }*/
    }

    /**
     * Get IP address from first non-localhost interface
     * param useipv4  true=return ipv4, false=return ipv6
     *
     * @return address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }

    public static String getIMEINO(Context cx) {
        TelephonyManager TelephonyMgr = (TelephonyManager) cx.getSystemService(cx.TELEPHONY_SERVICE);
        return TelephonyMgr.getDeviceId();  // Mobile IMEI No
    }

    public static String getDeviceId(Context cx) {
        return Settings.Secure.getString(cx.getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    public static String getAppVersion(Context cx) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = cx.getPackageManager().getPackageInfo(cx.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo.versionName;
    }

    public static JSONObject getBlankParam() {
        JSONObject param = new JSONObject();
        try {
            param.put("IDNO", "");
            param.put("PWD", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return param;

    }

    public static JSONObject getParam() {
        JSONObject param = new JSONObject();
        try {
            param.put("IDNO", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId));
            param.put("PWD", PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return param;

    }
}
