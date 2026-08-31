package com.ruon2.android.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ruon2.rssapi.Alarm;
import com.ruon2.rssapi.common.Util;

import org.junit.Test;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.List;

/**
 * Pure-JVM tests for {@link TheAlarm}: Gson (de)serialization as used when passing
 * an alarm between activities, the Alarm-to-TheAlarm conversion, and the enums.
 */
public class TheAlarmTest {

    private final Gson gson = new Gson();

    @Test
    public void gson_roundTrip_preservesFields() {
        TheAlarm alarm = new TheAlarm();
        alarm.setGuid("guid-1");
        alarm.setAgent("web01");
        alarm.setSeverity(Alarm.Severity.Critical);
        alarm.setResource("web-server");
        alarm.setDescription("CPU high");
        alarm.setDate("2024-06-01 10:00:00");
        alarm.setSound("dcgen");

        String json = gson.toJson(alarm);
        TheAlarm restored = gson.fromJson(json, TheAlarm.class);

        assertEquals("guid-1", restored.getGuid());
        assertEquals("web01", restored.getAgent());
        assertEquals(Alarm.Severity.Critical, restored.getSeverity());
        assertEquals("web-server", restored.getResource());
        assertEquals("CPU high", restored.getDescription());
        assertEquals("2024-06-01 10:00:00", restored.getDate());
        assertEquals("dcgen", restored.getSound());
    }

    @Test
    public void gson_usesSerializedNames() {
        TheAlarm alarm = new TheAlarm();
        alarm.setGuid("g");
        alarm.setAgent("a");
        String json = gson.toJson(alarm);
        // Fields are annotated with capitalized @SerializedName values.
        assertTrue(json.contains("\"Guid\":\"g\""));
        assertTrue(json.contains("\"Agent\":\"a\""));
    }

    @Test
    public void gson_deserializesServerCasing() {
        String json = "{\"Guid\":\"x\",\"Agent\":\"srv\",\"Severity\":\"Major\"," +
                "\"Resource\":\"res\",\"Description\":\"d\",\"Date\":\"2024-01-01\"}";
        TheAlarm alarm = gson.fromJson(json, TheAlarm.class);
        assertEquals("srv", alarm.getAgent());
        assertEquals(Alarm.Severity.Major, alarm.getSeverity());
        assertEquals("res", alarm.getResource());
    }

    @Test
    public void constructor_fromAlarm_copiesFieldsExceptSound() throws Exception {
        Element item = Util.parse(
                "<item><guid>g5</guid><title>host: Minor</title>" +
                        "<description><![CDATA[Resource: r<br>desc<br>2024-06-01<br>Group: grp]]></description></item>");
        Alarm source = new Alarm(item);

        TheAlarm alarm = new TheAlarm(source);

        assertEquals("g5", alarm.getGuid());
        assertEquals("host", alarm.getAgent());
        assertEquals(Alarm.Severity.Minor, alarm.getSeverity());
        assertEquals("r", alarm.getResource());
        assertEquals("grp", alarm.getGroup());
        assertNull("sound is not copied from Alarm", alarm.getSound());
    }

    @Test
    public void convert_mapsEveryAlarm() throws Exception {
        Element item = Util.parse(
                "<item><guid>g6</guid><title>node: Critical</title>" +
                        "<description><![CDATA[Resource: r<br>d<br>2024-06-01]]></description></item>");
        Alarm source = new Alarm(item);

        List<TheAlarm> converted = TheAlarm.convert(Collections.singletonList(source));

        assertEquals(1, converted.size());
        assertEquals("node", converted.get(0).getAgent());
    }

    @Test
    public void soundEnum_hasExpectedValues() {
        assertEquals(26, TheAlarm.Sound.values().length);
        assertNotNull(TheAlarm.Sound.valueOf("dcgen"));
        assertNotNull(TheAlarm.Sound.valueOf("umswp"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void soundEnum_unknownValue_throws() {
        TheAlarm.Sound.valueOf("not_a_sound");
    }

    @Test
    public void severityEnum_hasThreeLevels() {
        assertEquals(3, Alarm.Severity.values().length);
        assertNotNull(Alarm.Severity.valueOf("Critical"));
        assertNotNull(Alarm.Severity.valueOf("Major"));
        assertNotNull(Alarm.Severity.valueOf("Minor"));
    }

    @Test
    public void tag_isSimpleClassName() {
        assertEquals("TheAlarm", TheAlarm.TAG);
    }
}
