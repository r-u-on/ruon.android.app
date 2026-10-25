package com.ruon2.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented smoke test running on a real (SDK 36) device/emulator.
 */
@RunWith(AndroidJUnit4.class)
public class AppSmokeTest {

    @Test
    public void usesExpectedApplicationId() {
        Context appContext = ApplicationProvider.getApplicationContext();
        assertEquals("com.ruon2.app", appContext.getPackageName());
    }

    @Test
    public void resourcesAreAccessible() {
        Context appContext = ApplicationProvider.getApplicationContext();
        assertEquals("R-U-ON", appContext.getString(R.string.app_name));
        assertNotNull(appContext.getString(R.string.channel_id));
    }
}
