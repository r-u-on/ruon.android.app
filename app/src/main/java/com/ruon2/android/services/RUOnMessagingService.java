package com.ruon2.android.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ruon2.android.activities.MainActivity;
import com.ruon2.android.model.MyPreferenceManager;
import com.ruon2.android.util.TheAlarm;
import com.ruon2.android.util.UserLog;
import com.ruon2.app.R;

import org.json.JSONObject;

import java.util.Map;

public class RUOnMessagingService extends FirebaseMessagingService {
    private static final String TAG = RUOnMessagingService.class.getSimpleName();
    private static final String ALARM_KEY = "alarm";
    private static final String TITLE_KEY = "title";
    private static final String SOUND_KEY = "sound";
    public static int sNotificationId;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        Map<String, String> data = message.getData();
        String alarmMessage = data.get(ALARM_KEY);
        if (alarmMessage == null) return;

        JSONObject rawAlarm;
        try {
            rawAlarm = new JSONObject(alarmMessage);
            String title = rawAlarm.optString(TITLE_KEY).trim();
            String sound = rawAlarm.getString(SOUND_KEY).trim();
            UserLog.i(TAG, "Fcm alarm - " + title + " and sound - " + sound);

            /*
             * In some cases it may be useful to show a notification indicating to the user
             * that a message was received.
             */
            sendNotification(title, sound);
        } catch (Exception e) {
            UserLog.e(TAG, "Could not parse FCM message - " + message);
            e.printStackTrace();
        }
    }

    private void sendNotification(String title, String soundRaw) {
        createNotificationChannel(soundRaw);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, getString(R.string.channel_id) + "_" + soundRaw)
                        .setSmallIcon(R.drawable.notification)
                        .setContentTitle(getString(R.string.app_name))
                        .setAutoCancel(true)
                        .setColor(getResources().getColor(R.color.app_green))
                        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                        .setContentText(title);
        String sound = getSound(soundRaw);
        if (sound != null) {
            mBuilder.setSound(Uri.parse(sound));
        } else {
            mBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);
        }

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            resultPendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        } else {
            resultPendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        }
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        sNotificationId++;

        Notification ntf = mBuilder.build();
        mNotificationManager.notify(sNotificationId, ntf);
        Intent intent = new Intent(MainActivity.NOTIFICATION_EVENT);
        // Scope the broadcast to this app so it is delivered reliably under the
        // Android 14+ implicit-broadcast restrictions (target SDK 36).
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }

    private void createNotificationChannel(String soundRaw) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name, soundRaw);
            String description = getString(R.string.channel_description, soundRaw);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            String sound = getSound(soundRaw);
            NotificationChannel channel = new NotificationChannel(getString(R.string.channel_id) + "_" + soundRaw, name, importance);
            channel.setDescription(description);
            if (sound != null) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();

                channel.setSound(Uri.parse(sound), audioAttributes);
            }
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            deleteUnusedNotificationChannels(soundRaw, notificationManager);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void deleteUnusedNotificationChannels(String soundRaw, NotificationManager manager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        for (TheAlarm.Sound value : TheAlarm.Sound.values()) {
            if (!soundRaw.equals(value.name())) {
                manager.deleteNotificationChannel(getString(R.string.channel_id) + "_" + value.name());
            }
        }
    }

    private String getSound(String soundRaw) {
        if (TextUtils.isEmpty(soundRaw)) {
            return null;
        }
        String path = null;
        try {
            TheAlarm.Sound sound = TheAlarm.Sound.valueOf(soundRaw);
            switch (sound) {
                case dcagd:
                    path = "" + R.raw.dcagd;
                    break;
                case dccnc:
                    path = "" + R.raw.dccnc;
                    break;
                case dcgen:
                    path = "" + R.raw.dcgen;
                    break;
                case dcprd:
                    path = "" + R.raw.dcprd;
                    break;
                case digen:
                    path = "" + R.raw.digen;
                    break;
                case dmacs:
                    path = "" + R.raw.dmacs;
                    break;
                case dmcpu:
                    path = "" + R.raw.dmcpu;
                    break;
                case dmgen:
                    path = "" + R.raw.dmgen;
                    break;
                case dmhdd:
                    path = "" + R.raw.dmhdd;
                    break;
                case dmmem:
                    path = "" + R.raw.dmmem;
                    break;
                case dmswp:
                    path = "" + R.raw.dmswp;
                    break;
                case ecgen:
                    path = "" + R.raw.ecgen;
                    break;
                case eigen:
                    path = "" + R.raw.eigen;
                    break;
                case emagr:
                    path = "" + R.raw.emagr;
                    break;
                case emgen:
                    path = "" + R.raw.emgen;
                    break;
                case ucagd:
                    path = "" + R.raw.ucagd;
                    break;
                case uccnc:
                    path = "" + R.raw.uccnc;
                    break;
                case ucgen:
                    path = "" + R.raw.ucgen;
                    break;
                case ucprd:
                    path = "" + R.raw.ucprd;
                    break;
                case uigen:
                    path = "" + R.raw.uigen;
                    break;
                case umacs:
                    path = "" + R.raw.umacs;
                    break;
                case umcpu:
                    path = "" + R.raw.umcpu;
                    break;
                case umgen:
                    path = "" + R.raw.umgen;
                    break;
                case umhdd:
                    path = "" + R.raw.umhdd;
                    break;
                case ummem:
                    path = "" + R.raw.ummem;
                    break;
                case umswp:
                    path = "" + R.raw.umswp;
                    break;
            }
            if (TextUtils.isEmpty(path)) {
                return null;
            }
        } catch (IllegalArgumentException e) {
            UserLog.e(TAG, "Unknown sound");
            return null;
        }
        return "android.resource://" + getPackageName() + "/" + path;
    }

    @Override
    public void onNewToken(@NonNull String token) {
        UserLog.i(TAG, "onNewToken called, new Firebase token: " + token);
        MyPreferenceManager.saveFirebaseToken(getBaseContext(), token);
        MyPreferenceManager.setFcmRegisteredOnServer(getBaseContext(), false);
    }
}
