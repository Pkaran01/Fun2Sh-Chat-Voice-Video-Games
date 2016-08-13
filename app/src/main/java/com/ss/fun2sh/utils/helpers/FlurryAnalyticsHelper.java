package com.ss.fun2sh.utils.helpers;

import android.content.Context;


import com.quickblox.core.QBSettings;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tereha on 03.03.16.
 */
public class FlurryAnalyticsHelper {
    public static void pushAnalyticsData(Context context) {
        // init Flurry

        Map<String, String> params = new HashMap<>();

        //param keys and values have to be of String type
        params.put("app_id", QBSettings.getInstance().getApplicationId());
        params.put("chat_endpoint", QBSettings.getInstance().getChatServerDomain());

        //up to 10 params can be logged with each event

    }
}