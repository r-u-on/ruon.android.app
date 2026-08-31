package com.ruon2.android.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.preference.PreferenceManager;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Field;

/**
 * Robolectric tests for {@link MyPreferenceManager} - token persistence and the
 * FCM-registration / permission-rationale flags backed by SharedPreferences.
 */
@RunWith(RobolectricTestRunner.class)
public class MyPreferenceManagerRobolectricTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        // MyPreferenceManager caches SharedPreferences in a static field; reset it so
        // each test starts clean and isolated from any earlier test's state.
        Field sPrefs = MyPreferenceManager.class.getDeclaredField("sPrefs");
        sPrefs.setAccessible(true);
        sPrefs.set(null, null);
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit();
    }

    @Test
    public void token_defaultsToNull() {
        assertNull(MyPreferenceManager.getToken(context));
    }

    @Test
    public void token_savesAndReads() {
        MyPreferenceManager.saveToken(context, "session-token-123");
        assertEquals("session-token-123", MyPreferenceManager.getToken(context));
    }

    @Test
    public void firebaseToken_savesAndReads() {
        assertNull(MyPreferenceManager.getFcmToken(context));
        MyPreferenceManager.saveFirebaseToken(context, "fcm-abc");
        assertEquals("fcm-abc", MyPreferenceManager.getFcmToken(context));
    }

    @Test
    public void fcmRegisteredFlag_defaultsFalse_andToggles() {
        assertFalse(MyPreferenceManager.isFcmRegisteredOnOurServer(context));
        MyPreferenceManager.setFcmRegisteredOnServer(context, true);
        assertTrue(MyPreferenceManager.isFcmRegisteredOnOurServer(context));
        MyPreferenceManager.setFcmRegisteredOnServer(context, false);
        assertFalse(MyPreferenceManager.isFcmRegisteredOnOurServer(context));
    }

    @Test
    public void notificationRationalFlag_defaultsFalse_andToggles() {
        assertFalse(MyPreferenceManager.shouldShowNotificationPermissionRational(context));
        MyPreferenceManager.setShouldShowNotificationPermissionRational(context, true);
        assertTrue(MyPreferenceManager.shouldShowNotificationPermissionRational(context));
    }

    @Test
    public void tokensAreIndependent() {
        MyPreferenceManager.saveToken(context, "auth");
        MyPreferenceManager.saveFirebaseToken(context, "push");
        assertEquals("auth", MyPreferenceManager.getToken(context));
        assertEquals("push", MyPreferenceManager.getFcmToken(context));
    }
}
