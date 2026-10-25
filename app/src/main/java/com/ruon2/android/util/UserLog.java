package com.ruon2.android.util;

import android.util.Log;

import com.ruon2.app.BuildConfig;

public class UserLog {
    public static void i(String TAG, String message) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, message);
        }
    }

    public static void v(String TAG, String message) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, message);
        }
    }

    public static void e(String TAG, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, message);
        }
    }

    public static void printLongMessage(String tag, String message, String title) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        int step = message.length() / 500;
        for (int i = 0; i < step; i++) {
            UserLog.i(tag, title + " - part - " + (i + 1) * 500 + " - " + message.substring(i * 500, (i + 1) * 500));
        }
        UserLog.i(tag, title + " - part - " + step * 500 + " - " + message.substring(step * 500));
    }
}
