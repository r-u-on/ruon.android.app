package com.ruon2.rssapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Exercises the {@link API} response listeners end-to-end against realistic feed
 * payloads - the parsing pipeline used for authentication and alarm fetching.
 */
public class ApiParsingTest {

    @Test
    public void fetchAlarms_parsesChannelItems() {
        String rss =
                "<rss><channel>" +
                        "<item><guid>g1</guid><title>web01: Critical</title>" +
                        "<description><![CDATA[Resource: web<br>down<br>2024-06-01 10:00:00<br>Group: prod]]></description></item>" +
                        "<item><guid>g2</guid><title>db01: Minor</title>" +
                        "<description><![CDATA[Resource: db<br>slow<br>2024-06-01 11:00:00]]></description></item>" +
                        "</channel></rss>";

        final AtomicReference<List<Alarm>> captured = new AtomicReference<>();
        final AtomicReference<Exception> error = new AtomicReference<>();

        API.FetchAlarmsListener listener = new API.FetchAlarmsListener() {
            @Override
            protected void alarms(List<Alarm> alarms) {
                captured.set(alarms);
            }

            @Override
            public void error(Exception e) {
                error.set(e);
            }
        };
        listener.result(rss);

        assertNull("no error expected", error.get());
        List<Alarm> alarms = captured.get();
        assertNotNull(alarms);
        assertEquals(2, alarms.size());
        assertEquals("web01", alarms.get(0).getAgent());
        assertEquals(Alarm.Severity.Critical, alarms.get(0).getSeverity());
        assertEquals("prod", alarms.get(0).getGroup());
        assertEquals("db01", alarms.get(1).getAgent());
        assertEquals(Alarm.Severity.Minor, alarms.get(1).getSeverity());
    }

    @Test
    public void fetchAlarms_emptyChannel_yieldsEmptyList() {
        final AtomicReference<List<Alarm>> captured = new AtomicReference<>();
        API.FetchAlarmsListener listener = new API.FetchAlarmsListener() {
            @Override
            protected void alarms(List<Alarm> alarms) {
                captured.set(alarms);
            }

            @Override
            public void error(Exception e) {
                throw new AssertionError("unexpected error", e);
            }
        };
        listener.result("<rss><channel></channel></rss>");

        assertNotNull(captured.get());
        assertTrue(captured.get().isEmpty());
    }

    @Test
    public void authenticate_guidResponse_authenticates() {
        final AtomicReference<String> guid = new AtomicReference<>();
        final AtomicReference<Exception> error = new AtomicReference<>();

        API.AuthenticateListener listener = new API.AuthenticateListener() {
            @Override
            protected void authenticated(String g) {
                guid.set(g);
            }

            @Override
            public void error(Exception e) {
                error.set(e);
            }
        };
        listener.result("<guid>ABC-123-TOKEN</guid>");

        assertNull(error.get());
        assertEquals("ABC-123-TOKEN", guid.get());
    }

    @Test
    public void authenticate_errorResponse_reportsError() {
        final AtomicReference<Exception> error = new AtomicReference<>();
        API.AuthenticateListener listener = new API.AuthenticateListener() {
            @Override
            protected void authenticated(String g) {
                throw new AssertionError("should not authenticate on error");
            }

            @Override
            public void error(Exception e) {
                error.set(e);
            }
        };
        listener.result("<error>Invalid email or password</error>");

        assertNotNull(error.get());
        assertEquals("Invalid email or password", error.get().getMessage());
    }

    @Test
    public void authenticate_unexpectedResponse_reportsError() {
        final AtomicReference<Exception> error = new AtomicReference<>();
        API.AuthenticateListener listener = new API.AuthenticateListener() {
            @Override
            protected void authenticated(String g) {
                throw new AssertionError("should not authenticate");
            }

            @Override
            public void error(Exception e) {
                error.set(e);
            }
        };
        listener.result("<unexpected>data</unexpected>");

        assertNotNull(error.get());
    }
}
