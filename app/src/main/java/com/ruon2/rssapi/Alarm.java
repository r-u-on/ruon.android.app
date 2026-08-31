package com.ruon2.rssapi;

import static com.ruon2.rssapi.common.Util.eq;

import org.w3c.dom.Element;

public class Alarm {

    public static enum Severity {
        Critical, Major, Minor
    }

    private String guid;
    private String agent;
    private Severity severity;
    private String resource;
    private String description;
    private String date;
    private String group;


    public Alarm(Element item) throws Exception {


        guid = item.getElementsByTagName("guid").item(0).getTextContent();
        String ds = item.getElementsByTagName("description").item(0).getTextContent().trim();

        if (eq("http://ww.r-u-on.com/#error", guid)) {
            throw new Exception(ds);
        }

        String[] title = item.getElementsByTagName("title").item(0).getTextContent().split(": ");

        agent = title[0];
        severity = Severity.valueOf(title[1]);


        String[] dl = ds.split("<br>");

        resource = dl[0].substring("Resource: ".length()).trim();
        description = dl[1].trim();
        date = dl[2].trim();

        if (dl.length > 3) {
            if (dl[3].startsWith("Group:")) {
                group = dl[3].substring("Group: ".length()).trim();
            }
        }
    }


    public String getGuid() {
        return guid;
    }

    public String getAgent() {
        return agent;
    }

    public Severity getSeverity() {
        return severity;
    }


    public String getResource() {
        return resource;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public String toString() {
        Object[] oo = {guid, agent, severity, resource, description, date, group};
        String s = "";
        for (Object o : oo) {
            s += o + "\n";
        }
        return s;
    }
}
