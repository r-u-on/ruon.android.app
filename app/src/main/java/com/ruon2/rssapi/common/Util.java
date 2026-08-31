package com.ruon2.rssapi.common;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Util {

    public static boolean eq(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    public static Element parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xml.getBytes("utf-8")));
        return document.getDocumentElement();
    }

    public static String drain(InputStream in) throws IOException {
        try {
            in = new BufferedInputStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int b;
            while ((b = in.read()) >= 0) {
                out.write(b);
            }
            return new String(out.toByteArray(), "utf-8");
        } finally {
            in.close();
        }
    }
}
