package com.ruon2.rssapi.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;

/**
 * Pure-JVM unit tests for {@link Util} - equality helper, XML parsing and stream draining.
 */
public class UtilTest {

    @Test
    public void eq_sameReference_isTrue() {
        Object o = new Object();
        assertTrue(Util.eq(o, o));
    }

    @Test
    public void eq_equalValues_isTrue() {
        assertTrue(Util.eq("critical", "critical"));
        assertTrue(Util.eq(Integer.valueOf(5), Integer.valueOf(5)));
    }

    @Test
    public void eq_differentValues_isFalse() {
        assertFalse(Util.eq("critical", "major"));
    }

    @Test
    public void eq_bothNull_isTrue() {
        assertTrue(Util.eq(null, null));
    }

    @Test
    public void eq_oneNull_isFalse() {
        assertFalse(Util.eq(null, "x"));
        assertFalse(Util.eq("x", null));
    }

    @Test
    public void parse_returnsDocumentElement() throws Exception {
        Element root = Util.parse("<root><child>hello</child></root>");
        assertNotNull(root);
        assertEquals("root", root.getTagName());
        assertEquals("hello",
                root.getElementsByTagName("child").item(0).getTextContent());
    }

    @Test(expected = Exception.class)
    public void parse_malformedXml_throws() throws Exception {
        Util.parse("<root><unclosed></root>");
    }

    @Test
    public void drain_readsWholeStream_asUtf8() throws Exception {
        String content = "line1\nline2\nשלום";
        String drained = Util.drain(
                new ByteArrayInputStream(content.getBytes("utf-8")));
        assertEquals(content, drained);
    }
}
