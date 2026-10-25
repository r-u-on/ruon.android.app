package com.ruon2.android.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Ivan on 6/26/2015.
 */
public class MyPreferenceManager {

    private static SharedPreferences sPrefs;
    public static final String TOKEN = "token";
    public static final String FCM_TOKEN = "fcm_token";
    public static final String REGISTERED_ON_SERVER = "REGISTERED_ON_SERVER";

    private static final String SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE = "shouldShowRequestPermissionRationale";

    public static void saveToken(Context context, String token) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(TOKEN, token);
        editor.commit();
    }

    public static String getToken(Context context) {
        return getPrefs(context).getString(TOKEN, null);
    }

    public static void saveFirebaseToken(Context context, String token) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(FCM_TOKEN, token);
        editor.commit();
    }

    public static boolean isFcmRegisteredOnOurServer(Context context) {
        return getPrefs(context).getBoolean(REGISTERED_ON_SERVER, false);
    }

    public static void setFcmRegisteredOnServer(Context context, boolean state) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean(REGISTERED_ON_SERVER, state);
        editor.commit();
    }

    public static String getFcmToken(Context context) {
        return getPrefs(context).getString(FCM_TOKEN, null);
    }

    /**
     * Returns the app's default preferences file. Uses the same file name as the deprecated
     * {@code android.preference.PreferenceManager.getDefaultSharedPreferences()}, so data
     * saved by earlier versions (login token, FCM state) is kept on upgrade.
     */
    public static SharedPreferences getPrefs(Context context) {
        if (sPrefs == null) {
            sPrefs = context.getSharedPreferences(
                    context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        }
        return sPrefs;
    }

    public static boolean shouldShowNotificationPermissionRational(Context context) {
        return getPrefs(context).getBoolean(SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE, false);
    }

    public static void setShouldShowNotificationPermissionRational(Context context, boolean state) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean(SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE, state);
        editor.commit();
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getPrefs(context).edit();
    }
}
