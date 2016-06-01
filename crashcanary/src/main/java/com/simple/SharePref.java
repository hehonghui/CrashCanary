package com.simple;

import android.content.Context;

/**
 * Created by mrsimple on 1/6/16.
 */
public final class SharePref {

    private SharePref() {
    }

    static final String LOG_PREF = "crash.log";

    /**
     * @param context
     * @param fileName
     */
    public static void saveLastCrashLog(Context context, String fileName) {
        context.getSharedPreferences(LOG_PREF, Context.MODE_PRIVATE).edit()
                .putString("last_crash", fileName).commit();
    }

    public static String getLastCrashLog(Context context) {
        return context.getSharedPreferences(LOG_PREF, Context.MODE_PRIVATE).getString("last_crash", "");
    }
}
