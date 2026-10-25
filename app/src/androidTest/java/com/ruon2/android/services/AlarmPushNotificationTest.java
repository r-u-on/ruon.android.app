package com.ruon2.android.services;

import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.messaging.RemoteMessage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

/**
 * Feeds a mock alarm push into {@link RUOnMessagingService#onMessageReceived} on a real
 * device, exactly as FCM would deliver it, and checks the alarm notification is shown.
 * It does not go through FCM or the R-U-On server.
 *
 * <p>Leaves the notification on the device so it can be inspected. Does not touch the
 * saved login.
 */
@RunWith(AndroidJUnit4.class)
public class AlarmPushNotificationTest {

    private static final String TITLE = "Mock alarm: CPU usage is high (test)";

    @Rule
    public GrantPermissionRule notificationPermission =
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    @Test
    public void alarmPush_showsAlarmNotification() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        RUOnMessagingService service = new RUOnMessagingService();
        attachBaseContext(service, context);

        service.onMessageReceived(new RemoteMessage.Builder("mock@fcm.googleapis.com")
                .addData("alarm", "{\"title\":\"" + TITLE + "\",\"sound\":\"umcpu\"}")
                .build());

        assertTrue("alarm notification was not shown", waitForNotification(context));
    }

    private static boolean waitForNotification(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        long deadline = SystemClock.uptimeMillis() + 5_000;
        while (SystemClock.uptimeMillis() < deadline) {
            for (StatusBarNotification sbn : manager.getActiveNotifications()) {
                CharSequence text = sbn.getNotification().extras
                        .getCharSequence(Notification.EXTRA_TEXT);
                if (text != null && TITLE.contentEquals(text)) {
                    return true;
                }
            }
            SystemClock.sleep(100);
        }
        return false;
    }

    // A service created outside the framework has no Context; give it the app's.
    private static void attachBaseContext(ContextWrapper wrapper, Context base) throws Exception {
        Method attach = ContextWrapper.class.getDeclaredMethod("attachBaseContext", Context.class);
        attach.setAccessible(true);
        attach.invoke(wrapper, base);
    }
}
