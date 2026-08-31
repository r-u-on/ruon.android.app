package com.ruon2.android.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.content.Context;
import android.preference.PreferenceManager;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ruon2.app.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Espresso UI tests for {@link LoginActivity} on a real SDK 36 device/emulator:
 * initial layout, field presence and client-side validation.
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Before
    public void clearSession() {
        // Ensure no saved token, otherwise onResume immediately redirects to MainActivity.
        Context ctx = ApplicationProvider.getApplicationContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().clear().commit();
    }

    @Test
    public void showsLoginControls() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.email)).check(matches(isDisplayed()));
            onView(withId(R.id.password)).check(matches(isDisplayed()));
            onView(withId(R.id.login_btn)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void showsAccountHintAndVersion() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.account_hint))
                    .check(matches(withText(containsString("r-u-on.com"))));
            onView(withId(R.id.version_name))
                    .check(matches(withText(containsString("R-U-ON"))));
        }
    }

    @Test
    public void emptyCredentials_showValidationDialog() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.email)).perform(clearText(), closeSoftKeyboard());
            onView(withId(R.id.password)).perform(clearText(), closeSoftKeyboard());
            onView(withId(R.id.login_btn)).perform(click());

            // Validation surfaces via InfoDialog, whose neutral button is labelled "Ok".
            onView(withText("Ok")).inRoot(isDialog()).check(matches(isDisplayed()));
        }
    }

    @Test
    public void emailWithoutPassword_showsPasswordValidation() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.email)).perform(typeText("user@example.com"), closeSoftKeyboard());
            onView(withId(R.id.password)).perform(clearText(), closeSoftKeyboard());
            onView(withId(R.id.login_btn)).perform(click());

            // A dialog must appear (password-required when online, or network error when offline).
            onView(withText("Ok")).inRoot(isDialog()).check(matches(isDisplayed()));
        }
    }
}
