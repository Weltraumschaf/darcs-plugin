/*
 * LICENSE
 *
 * "THE BEER-WARE LICENSE" (Revision 42):
 * "Ralph Lange" <Ralph.Lange@gmx.de> wrote this file.
 * As long as you retain this notice you can do whatever you want with
 * this stuff. If we meet some day, and you think this stuff is worth it,
 * you can buy me a beer in return.
 */
package org.jenkinsci.plugins.darcs;

import java.io.UnsupportedEncodingException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ralph Lange <ralph.lange@gmx.de>
 */
public class DarcsXmlSanitizerTest {

    private final DarcsXmlSanitizer sut = new DarcsXmlSanitizer();

    @Test
    public void cleanse_usAscii() throws UnsupportedEncodingException {
        final byte[] input = "foo <name>abcdefg</name> bar <comment>abcdefg</comment> foobar".getBytes("US-ASCII");
        final String output = "foo <name>abcdefg</name> bar <comment>abcdefg</comment> foobar";
        final String result = sut.cleanse(input);
        assertEquals(output, result);
    }

    @Test
    public void cleanse_utf8() throws UnsupportedEncodingException {
        //CHECKSTYLE:OFF
        final byte[] input = "foo <name>äöüÄÖÜß</name> bar <comment>äöüÄÖÜß</comment> foobar".getBytes("UTF-8");
        //CHECKSTYLE:ON
        final String output = "foo <name>äöüÄÖÜß</name> bar <comment>äöüÄÖÜß</comment> foobar";
        final String result = sut.cleanse(input);
        assertEquals(output, result);
    }

    @Test
    public void cleanse_iso8859_1() throws UnsupportedEncodingException {
        //CHECKSTYLE:OFF
        final byte[] input = "foo <name>äöüÄÖÜß</name> bar <comment>äöüÄÖÜß</comment> foobar".getBytes("ISO-8859-1");
        //CHECKSTYLE:ON
        final String output = "foo <name>äöüÄÖÜß</name> bar <comment>äöüÄÖÜß</comment> foobar";
        final String result = sut.cleanse(input);
        assertEquals(output, result);
    }

    @Test
    public void cleanse_utf8_iso8859_mixed() throws UnsupportedEncodingException {
        final byte[] iso = "foo <name>äöüÄÖÜß</name> bar ".getBytes("ISO-8859-1");
        final byte[] utf8 = "<comment>äöüÄÖÜß</comment> foobar".getBytes("UTF-8");
        final byte[] input = new byte[iso.length + utf8.length];
        System.arraycopy(iso, 0, input, 0, iso.length);
        System.arraycopy(utf8, 0, input, iso.length, utf8.length);
        final String output = "foo <name>äöüÄÖÜß</name> bar <comment>äöüÄÖÜß</comment> foobar";
        final String result = sut.cleanse(input);
        assertEquals(output, result);
    }

}
