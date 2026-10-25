package com.ruon2.android.model;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ruon2.app.BuildConfig;

/**
 * Owns the "push id" the R-U-On server sends notifications to. Depending on the
 * {@code ruon.fcmUseInstallationId} build switch this is either:
 * <ul>
 *     <li>an FCM registration token ({@code getToken()}/{@code onNewToken()}), deprecated by
 *     Firebase, or</li>
 *     <li>a Firebase Installation ID ({@code register()}/{@code onRegistered()}), which the
 *     server must send to with the FCM HTTP v1 {@code fid} field.</li>
 * </ul>
 * The two kinds are cached under separate keys so switching modes never sends a stale id of
 * the wrong kind to the server.
 */
public final class PushRegistration {

    private PushRegistration() {
    }

    public static boolean usesInstallationId() {
        return BuildConfig.FCM_USE_INSTALLATION_ID;
    }

    /**
     * Asks Firebase for the current push id. In installation-id mode this also registers the
     * installation with FCM, which is what makes the id reachable.
     */
    public static Task<String> fetchPushId() {
        if (usesInstallationId()) {
            return FirebaseMessaging.getInstance().register()
                    .onSuccessTask(unused -> FirebaseInstallations.getInstance().getId());
        }
        return fetchLegacyToken();
    }

    // Token mode only. With the installation-id manifest flag on, getToken() fails.
    @SuppressWarnings("deprecation")
    private static Task<String> fetchLegacyToken() {
        return FirebaseMessaging.getInstance().getToken();
    }

    public static String getCachedPushId(Context context) {
        return getCachedPushId(context, usesInstallationId());
    }

    static String getCachedPushId(Context context, boolean installationId) {
        return installationId
                ? MyPreferenceManager.getFcmInstallationId(context)
                : MyPreferenceManager.getFcmToken(context);
    }

    /**
     * Stores a push id issued by Firebase. When it differs from the cached one, the server
     * registration is marked stale so the next login sends the new id to the server.
     */
    public static void onPushIdIssued(Context context, String pushId) {
        onPushIdIssued(context, pushId, usesInstallationId());
    }

    public static void onPushIdIssued(Context context, String pushId, boolean installationId) {
        if (pushId.equals(getCachedPushId(context, installationId))) {
            return;
        }
        if (installationId) {
            MyPreferenceManager.saveFcmInstallationId(context, pushId);
        } else {
            MyPreferenceManager.saveFirebaseToken(context, pushId);
        }
        MyPreferenceManager.setFcmRegisteredOnServer(context, false);
    }
}
