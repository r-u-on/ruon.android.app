package com.ruon2.android.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowNetworkCapabilities;

/**
 * Robolectric tests for {@link NetworkUtils#isNetworkAvailable(Context)} across
 * internet-capable, no-internet and no-active-network states.
 */
@RunWith(RobolectricTestRunner.class)
public class NetworkUtilsRobolectricTest {

    private Context context;
    private ConnectivityManager cm;
    private ShadowConnectivityManager shadowCm;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        shadowCm = shadowOf(cm);
    }

    @Test
    public void returnsTrue_whenActiveNetworkHasInternet() {
        setActiveNetworkCapabilities(true);
        assertTrue(NetworkUtils.isNetworkAvailable(context));
    }

    @Test
    public void returnsFalse_whenActiveNetworkHasNoInternet() {
        setActiveNetworkCapabilities(false);
        assertFalse(NetworkUtils.isNetworkAvailable(context));
    }

    @Test
    public void returnsFalse_whenNoActiveNetwork() {
        shadowCm.clearAllNetworks();
        assertFalse(NetworkUtils.isNetworkAvailable(context));
    }

    private void setActiveNetworkCapabilities(boolean hasInternet) {
        Network network = cm.getActiveNetwork();
        NetworkCapabilities capabilities = ShadowNetworkCapabilities.newInstance();
        shadowOf(capabilities).addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        if (hasInternet) {
            shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        shadowCm.setNetworkCapabilities(network, capabilities);
    }
}
