package com.ruon2.android.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.robolectric.Shadows.shadowOf;

import android.app.NotificationManager;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.messaging.RemoteMessage;
import com.ruon2.android.model.MyPreferenceManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/**
 * Robolectric tests for {@link RUOnMessagingService}: FCM data-message handling,
 * notification-channel creation, sound mapping, resilience to bad payloads, and
 * push id persistence on {@code onNewToken} and {@code onRegistered}.
 */
@RunWith(RobolectricTestRunner.class)
public class RUOnMessagingServiceRobolectricTest {

    private RUOnMessagingService service;
    private NotificationManager notificationManager;

    private static RemoteMessage messageWith(String key, String value) {
        return new RemoteMessage.Builder("123456@fcm.googleapis.com")
                .addData(key, value)
                .build();
    }

    @Before
    public void setUp() {
        service = Robolectric.setupService(RUOnMessagingService.class);
        notificationManager = (NotificationManager) ApplicationProvider.getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        RUOnMessagingService.sNotificationId = 0;
    }

    @Test
    public void onMessageReceived_validAlarm_postsNotificationAndCreatesChannel() {
        service.onMessageReceived(
                messageWith("alarm", "{\"title\":\"CPU is high\",\"sound\":\"dcgen\"}"));

        assertEquals(1, shadowOf(notificationManager).size());
        assertNotNull("expected a channel for the dcgen sound",
                notificationManager.getNotificationChannel("ruon_notification_channel_dcgen"));
    }

    @Test
    public void onMessageReceived_unknownSound_stillPostsWithDefaultChannel() {
        service.onMessageReceived(
                messageWith("alarm", "{\"title\":\"Generic\",\"sound\":\"totally_unknown\"}"));

        // Unknown sound is tolerated (getSound returns null -> default sound) and a
        // channel keyed by the raw sound string is still created.
        assertEquals(1, shadowOf(notificationManager).size());
        assertNotNull(
                notificationManager.getNotificationChannel("ruon_notification_channel_totally_unknown"));
    }

    @Test
    public void onMessageReceived_noAlarmKey_postsNothing() {
        service.onMessageReceived(messageWith("somethingElse", "value"));
        assertEquals(0, shadowOf(notificationManager).size());
    }

    @Test
    public void onMessageReceived_malformedJson_isSwallowed() {
        service.onMessageReceived(messageWith("alarm", "this is not json"));
        assertEquals(0, shadowOf(notificationManager).size());
    }

    @Test
    public void onMessageReceived_missingSoundField_postsNothing() {
        // The service requires a "sound" field (getString throws if absent) and the
        // resulting exception is caught, so no notification is posted.
        service.onMessageReceived(messageWith("alarm", "{\"title\":\"no sound here\"}"));
        assertEquals(0, shadowOf(notificationManager).size());
    }

    @Test
    public void onMessageReceived_multipleAlarms_incrementNotificationId() {
        service.onMessageReceived(messageWith("alarm", "{\"title\":\"a\",\"sound\":\"dcgen\"}"));
        service.onMessageReceived(messageWith("alarm", "{\"title\":\"b\",\"sound\":\"umswp\"}"));
        assertEquals(2, shadowOf(notificationManager).size());
    }

    @Test
    public void onNewToken_persistsTokenAndResetsRegistration() {
        MyPreferenceManager.setFcmRegisteredOnServer(service, true);

        service.onNewToken("fresh-fcm-token");

        assertEquals("fresh-fcm-token", MyPreferenceManager.getFcmToken(service));
        assertFalse("registration must be reset so the app re-registers the new token",
                MyPreferenceManager.isFcmRegisteredOnOurServer(service));
    }

    @Test
    public void onRegistered_persistsInstallationIdAndResetsRegistration() {
        MyPreferenceManager.setFcmRegisteredOnServer(service, true);

        service.onRegistered("fresh-installation-id");

        assertEquals("fresh-installation-id", MyPreferenceManager.getFcmInstallationId(service));
        assertFalse("registration must be reset so the app re-registers the new id",
                MyPreferenceManager.isFcmRegisteredOnOurServer(service));
    }
}
