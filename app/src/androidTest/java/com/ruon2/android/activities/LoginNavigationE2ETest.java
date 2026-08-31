package com.ruon2.android.activities;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.preference.PreferenceManager;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ruon2.android.model.MyPreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * End-to-end navigation test: when a session token is already stored, launching
 * {@link LoginActivity} must immediately route the user to {@link MainActivity}.
 * The outgoing MainActivity launch is stubbed so it does not start for real.
 */
@RunWith(AndroidJUnit4.class)
public class LoginNavigationE2ETest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        Intents.init();
        // Pretend the user is already authenticated.
        MyPreferenceManager.saveToken(context, "already-logged-in-token");
        // Prevent the real MainActivity (which hits the network) from starting.
        intending(hasComponent(MainActivity.class.getName()))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    }

    @After
    public void tearDown() {
        Intents.release();
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit();
    }

    @Test
    public void alreadyLoggedIn_redirectsToMainActivity() {
        ActivityScenario<LoginActivity> scenario = null;
        try {
            // LoginActivity finishes itself during the redirect; scenario lifecycle
            // bookkeeping may raise - the navigation intent is still recorded.
            scenario = ActivityScenario.launch(LoginActivity.class);
        } catch (RuntimeException ignored) {
            // no-op
        } finally {
            if (scenario != null) {
                scenario.close();
            }
        }

        intended(hasComponent(MainActivity.class.getName()));
    }
}
