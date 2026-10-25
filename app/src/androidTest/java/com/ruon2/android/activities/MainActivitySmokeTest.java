package com.ruon2.android.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.Manifest;
import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.ruon2.android.model.MyPreferenceManager;
import com.ruon2.app.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Launch/stability (crash & ANR) smoke test for {@link MainActivity} on SDK 36.
 * The activity registers a NOT_EXPORTED receiver and kicks off a refresh on resume;
 * this verifies it reaches RESUMED and renders without crashing.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivitySmokeTest {

    private Context context;

    // Pre-grant POST_NOTIFICATIONS so the first-launch permission dialog does not
    // pop over the activity and hold it in PAUSED during the test.
    @Rule
    public GrantPermissionRule notificationPermission =
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        // A token so the resume-time refresh runs its normal (authenticated) path.
        MyPreferenceManager.saveToken(context, "smoke-test-token");
    }

    @After
    public void tearDown() {
        MyPreferenceManager.getPrefs(context).edit().clear().commit();
    }

    @Test
    public void launchesToResumedWithoutCrashing() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.moveToState(Lifecycle.State.RESUMED);
            onView(withId(R.id.refresher)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void survivesPauseResumeCycle() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            // Exercises receiver unregister/register (onPause/onResume) under SDK 36.
            scenario.moveToState(Lifecycle.State.STARTED);
            scenario.moveToState(Lifecycle.State.RESUMED);
            onView(withId(R.id.refresher)).check(matches(isDisplayed()));
        }
    }
}
