package com.ruon2.android.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.ruon2.app.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Robolectric tests for {@link PushRegistration}: per-mode caching of the push id and
 * invalidating the server registration when the id changes.
 */
@RunWith(RobolectricTestRunner.class)
public class PushRegistrationRobolectricTest {

    private static final boolean TOKEN_MODE = false;
    private static final boolean INSTALLATION_ID_MODE = true;

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        MyPreferenceManager.getPrefs(context).edit().clear().commit();
    }

    @Test
    public void mode_followsBuildSwitch() {
        assertEquals(BuildConfig.FCM_USE_INSTALLATION_ID, PushRegistration.usesInstallationId());
    }

    @Test
    public void tokenMode_storesTokenOnly() {
        PushRegistration.onPushIdIssued(context, "fcm-token", TOKEN_MODE);

        assertEquals("fcm-token", PushRegistration.getCachedPushId(context, TOKEN_MODE));
        assertEquals("fcm-token", MyPreferenceManager.getFcmToken(context));
        assertNull(PushRegistration.getCachedPushId(context, INSTALLATION_ID_MODE));
    }

    @Test
    public void installationIdMode_storesInstallationIdOnly() {
        PushRegistration.onPushIdIssued(context, "fid-123", INSTALLATION_ID_MODE);

        assertEquals("fid-123", PushRegistration.getCachedPushId(context, INSTALLATION_ID_MODE));
        assertEquals("fid-123", MyPreferenceManager.getFcmInstallationId(context));
        assertNull(PushRegistration.getCachedPushId(context, TOKEN_MODE));
    }

    // Users switched from tokens to installation ids must not send their old token as a FID.
    @Test
    public void switchingModes_doesNotReuseTheOtherKindOfId() {
        PushRegistration.onPushIdIssued(context, "old-fcm-token", TOKEN_MODE);
        assertNull(PushRegistration.getCachedPushId(context, INSTALLATION_ID_MODE));
    }

    @Test
    public void newId_marksServerRegistrationStale() {
        MyPreferenceManager.setFcmRegisteredOnServer(context, true);

        PushRegistration.onPushIdIssued(context, "fid-new", INSTALLATION_ID_MODE);

        assertFalse(MyPreferenceManager.isFcmRegisteredOnOurServer(context));
    }

    // onRegistered() fires after every register() call, even when the id did not change.
    @Test
    public void sameId_keepsServerRegistration() {
        PushRegistration.onPushIdIssued(context, "fid-123", INSTALLATION_ID_MODE);
        MyPreferenceManager.setFcmRegisteredOnServer(context, true);

        PushRegistration.onPushIdIssued(context, "fid-123", INSTALLATION_ID_MODE);

        assertTrue(MyPreferenceManager.isFcmRegisteredOnOurServer(context));
    }
}
