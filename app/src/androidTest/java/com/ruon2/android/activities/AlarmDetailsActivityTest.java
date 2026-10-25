package com.ruon2.android.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.gson.Gson;
import com.ruon2.android.util.TheAlarm;
import com.ruon2.app.R;
import com.ruon2.rssapi.Alarm;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Espresso UI test for {@link AlarmDetailsActivity}: an alarm passed in as a Gson
 * extra is rendered into the detail views. Runs on a real SDK 36 device/emulator.
 */
@RunWith(AndroidJUnit4.class)
public class AlarmDetailsActivityTest {

    private static Intent intentFor(TheAlarm alarm) {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(ctx, AlarmDetailsActivity.class);
        intent.putExtra(TheAlarm.TAG, new Gson().toJson(alarm));
        return intent;
    }

    private static TheAlarm sampleAlarm() {
        TheAlarm alarm = new TheAlarm();
        alarm.setGuid("guid-abc-123");
        alarm.setAgent("web01");
        alarm.setSeverity(Alarm.Severity.Critical);
        alarm.setResource("web-server");
        alarm.setDescription("CPU usage is high");
        alarm.setDate("2024-06-01 10:00:00");
        return alarm;
    }

    @Test
    public void rendersAlarmFields() {
        try (ActivityScenario<AlarmDetailsActivity> scenario =
                     ActivityScenario.launch(intentFor(sampleAlarm()))) {
            onView(withId(R.id.alarm_agent)).check(matches(withText("web-server")));
            onView(withId(R.id.alarm_severity)).check(matches(withText("Critical")));
            onView(withId(R.id.alarm_timestamp)).check(matches(withText("2024-06-01 10:00:00")));
            onView(withId(R.id.alarm_detail))
                    .check(matches(withText(containsString("CPU usage is high"))));
            onView(withId(R.id.alarm_detail))
                    .check(matches(withText(containsString("guid-abc-123"))));
        }
    }

    @Test
    public void rendersMinorSeverity() {
        TheAlarm alarm = sampleAlarm();
        alarm.setSeverity(Alarm.Severity.Minor);
        try (ActivityScenario<AlarmDetailsActivity> scenario =
                     ActivityScenario.launch(intentFor(alarm))) {
            onView(withId(R.id.alarm_severity)).check(matches(withText("Minor")));
        }
    }
}
