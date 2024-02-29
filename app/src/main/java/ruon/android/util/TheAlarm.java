package ruon.android.util;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import ruon.rssapi.Alarm;

public class TheAlarm {
    public static final String TAG = TheAlarm.class.getSimpleName();

    public static List<TheAlarm> convert(List<Alarm> alarms) {
        List<TheAlarm> theAlarms = new ArrayList<>();
        for (Alarm alarm:alarms) {
            theAlarms.add(new TheAlarm(alarm));
        }
        return theAlarms;
    }

    public static enum Sound {
        dcagd, dccnc, dcgen, dcprd, digen, dmacs, dmcpu, dmgen, dmhdd, dmmem,
        dmswp, ecgen, eigen, emagr, emgen, ucagd, uccnc, ucgen, ucprd, uigen, umacs,
        umcpu, umgen, umhdd, ummem, umswp;    }


    @SerializedName("Guid")
    private String guid;
    @SerializedName("Agent")
    private String agent;
    @SerializedName("Severity")
    private Alarm.Severity severity;
    @SerializedName("Resource")
    private String resource;
    @SerializedName("Description")
    private String description;
    @SerializedName("Date")
    private String date;
    private String group;
    private String sound;




    public TheAlarm(Alarm alarm) {
        guid = alarm.getGuid();
        agent = alarm.getAgent();
        severity = alarm.getSeverity();
        resource = alarm.getResource();
        description = alarm.getDescription();
        date = alarm.getDate();
        group = alarm.getGroup();
    }


    public TheAlarm(){ /*Required empty bean constructor*/ }


    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
    public void setAgent(String agent) {
        this.agent = agent;
    }
    public String getAgent() {
        return agent;
    }

    public Alarm.Severity getSeverity() {
        return severity;
    }
    public void setSeverity(Alarm.Severity severity) {
        this.severity = severity;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public String getGroup() {
        return group;
    }

    public void setSound(String sound){
        this.sound = sound;
    }

    public String getSound(){
        return sound;
    }

    @Override
    public String toString() {
        Object [] oo = {guid, agent, severity, resource, description, date, group};
        String s = "";
        for (Object o:oo) {
            s+=o+"\n";
        }
        return s;
    }
}
