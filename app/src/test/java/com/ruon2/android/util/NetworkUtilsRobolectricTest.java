package com.ruon2.android.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowNetworkInfo;

/**
 * Robolectric tests for {@link NetworkUtils#isNetworkAvailable(Context)} across
 * connected, disconnected and no-active-network states.
 */
@RunWith(RobolectricTestRunner.class)
public class NetworkUtilsRobolectricTest {

    private Context context;
    private ShadowConnectivityManager shadowCm;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        shadowCm = shadowOf(cm);
    }

    @Test
    public void returnsTrue_whenConnectedAndAvailable() {
        NetworkInfo connected = ShadowNetworkInfo.newInstance(
                NetworkInfo.DetailedState.CONNECTED,
                ConnectivityManager.TYPE_WIFI,
                0,
                /* isAvailable= */ true,
                NetworkInfo.State.CONNECTED);
        shadowCm.setActiveNetworkInfo(connected);

        assertTrue(NetworkUtils.isNetworkAvailable(context));
    }

    @Test
    public void returnsFalse_whenDisconnected() {
        NetworkInfo disconnected = ShadowNetworkInfo.newInstance(
                NetworkInfo.DetailedState.DISCONNECTED,
                ConnectivityManager.TYPE_WIFI,
                0,
                /* isAvailable= */ false,
                NetworkInfo.State.DISCONNECTED);
        shadowCm.setActiveNetworkInfo(disconnected);

        assertFalse(NetworkUtils.isNetworkAvailable(context));
    }

    @Test
    public void returnsFalse_whenNoActiveNetwork() {
        shadowCm.setActiveNetworkInfo(null);
        assertFalse(NetworkUtils.isNetworkAvailable(context));
    }
}
