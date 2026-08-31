package com.ruon2.rssapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.ruon2.rssapi.common.Util;

import org.junit.Test;
import org.w3c.dom.Element;

/**
 * Pure-JVM unit tests for {@link Alarm}, which parses a single RSS {@code <item>}.
 * The live feed delivers the description as CDATA containing {@code <br>} separators,
 * so the fixtures mirror that exactly.
 */
public class AlarmTest {

    private static Element item(String guid, String title, String cdataDescription) throws Exception {
        return Util.parse(
                "<item>" +
                        "<guid>" + guid + "</guid>" +
                        "<title>" + title + "</title>" +
                        "<description><![CDATA[" + cdataDescription + "]]></description>" +
                        "</item>");
    }

    @Test
    public void parsesAllFields_withGroup() throws Exception {
        Alarm alarm = new Alarm(item(
                "guid-42",
                "web01: Critical",
                "Resource: web-server<br>CPU usage is high<br>2024-06-01 10:00:00<br>Group: production"));

        assertEquals("guid-42", alarm.getGuid());
        assertEquals("web01", alarm.getAgent());
        assertEquals(Alarm.Severity.Critical, alarm.getSeverity());
        assertEquals("web-server", alarm.getResource());
        assertEquals("CPU usage is high", alarm.getDescription());
        assertEquals("2024-06-01 10:00:00", alarm.getDate());
        assertEquals("production", alarm.getGroup());
    }

    @Test
    public void parsesWithoutGroup_leavesGroupNull() throws Exception {
        Alarm alarm = new Alarm(item(
                "guid-7",
                "db01: Major",
                "Resource: database<br>Disk is full<br>2024-06-02 09:30:00"));

        assertEquals(Alarm.Severity.Major, alarm.getSeverity());
        assertEquals("database", alarm.getResource());
        assertEquals("Disk is full", alarm.getDescription());
        assertNull(alarm.getGroup());
    }

    @Test
    public void parsesMinorSeverity() throws Exception {
        Alarm alarm = new Alarm(item(
                "guid-9",
                "cache01: Minor",
                "Resource: cache<br>Latency elevated<br>2024-06-03 08:00:00"));

        assertEquals(Alarm.Severity.Minor, alarm.getSeverity());
        assertEquals("cache01", alarm.getAgent());
    }

    @Test
    public void errorGuid_throwsWithDescriptionAsMessage() throws Exception {
        try {
            new Alarm(item(
                    "http://ww.r-u-on.com/#error",
                    "ignored: Major",
                    "Authentication failed"));
            fail("Expected an exception for the error sentinel guid");
        } catch (Exception e) {
            assertEquals("Authentication failed", e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownSeverity_throws() throws Exception {
        // Severity.valueOf on an unknown token throws IllegalArgumentException.
        new Alarm(item(
                "guid-x",
                "agent: Fatal",
                "Resource: r<br>d<br>2024-01-01"));
    }

    @Test
    public void toString_containsKeyFields() throws Exception {
        Alarm alarm = new Alarm(item(
                "guid-42",
                "web01: Critical",
                "Resource: web-server<br>CPU high<br>2024-06-01 10:00:00<br>Group: production"));

        String s = alarm.toString();
        assertEquals(true, s.contains("web01"));
        assertEquals(true, s.contains("Critical"));
        assertEquals(true, s.contains("production"));
    }
}
