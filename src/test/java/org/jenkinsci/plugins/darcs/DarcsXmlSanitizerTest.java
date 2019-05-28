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
        final byte[] input = "foo <name>äöüÄÖÜß</name> bar <comment>äöüÄÖÜß</comment> foobar".getBytes("UTF-8");
        final String output = "foo <name>äöüÄÖÜß</name> bar <comment>äöüÄÖÜß</comment> foobar";
        final String result = sut.cleanse(input);
        assertEquals(output, result);
    }

    @Test
    public void cleanse_iso8859_1() throws UnsupportedEncodingException {
        final byte[] input = "foo <name>äöüÄÖÜß</name> bar <comment>äöüÄÖÜß</comment> foobar".getBytes("ISO-8859-1");
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

    @Test
    public void replaceInvalidChars() {

    }

    @Test
    public void replaceInvalidChar_validCharacters() {
        assertEquals(' ', sut.replaceInvalidChar(' '));
        assertEquals('a', sut.replaceInvalidChar('a'));
        assertEquals('b', sut.replaceInvalidChar('b'));
        assertEquals('c', sut.replaceInvalidChar('c'));
        assertEquals('z', sut.replaceInvalidChar('z'));
        assertEquals('A', sut.replaceInvalidChar('A'));
        assertEquals('ñ', sut.replaceInvalidChar('ñ'));
        assertEquals('Ï', sut.replaceInvalidChar('Ï'));
    }

    @Test
    public void replaceInvalidChar_validControlCharacters() {
        assertEquals('\t', sut.replaceInvalidChar('\t'));
        assertEquals('\n', sut.replaceInvalidChar('\n'));
        assertEquals('\r', sut.replaceInvalidChar('\r'));
    }

    @Test
    public void replaceInvalidChar_invalidControlCharacters() {
        assertEquals('�', sut.replaceInvalidChar((char) 0x00));
        assertEquals('�', sut.replaceInvalidChar((char) 0x01));
        assertEquals('�', sut.replaceInvalidChar((char) 0x02));
        assertEquals('�', sut.replaceInvalidChar((char) 0x03));
        assertEquals('�', sut.replaceInvalidChar((char) 0x04));
        assertEquals('�', sut.replaceInvalidChar((char) 0x05));
        assertEquals('�', sut.replaceInvalidChar((char) 0x06));
        assertEquals('�', sut.replaceInvalidChar((char) 0x07));
        assertEquals('�', sut.replaceInvalidChar((char) 0x08));
        assertEquals('�', sut.replaceInvalidChar((char) 0x0B));
        assertEquals('�', sut.replaceInvalidChar((char) 0x0C));
        assertEquals('�', sut.replaceInvalidChar((char) 0x0E));
        assertEquals('�', sut.replaceInvalidChar((char) 0x0F));
        assertEquals('�', sut.replaceInvalidChar((char) 0x10));
        assertEquals('�', sut.replaceInvalidChar((char) 0x11));
        assertEquals('�', sut.replaceInvalidChar((char) 0x12));
        assertEquals('�', sut.replaceInvalidChar((char) 0x13));
        assertEquals('�', sut.replaceInvalidChar((char) 0x14));
        assertEquals('�', sut.replaceInvalidChar((char) 0x15));
        assertEquals('�', sut.replaceInvalidChar((char) 0x16));
        assertEquals('�', sut.replaceInvalidChar((char) 0x17));
        assertEquals('�', sut.replaceInvalidChar((char) 0x18));
        assertEquals('�', sut.replaceInvalidChar((char) 0x19));
        assertEquals('�', sut.replaceInvalidChar((char) 0x1A));
        assertEquals('�', sut.replaceInvalidChar((char) 0x1B));
        assertEquals('�', sut.replaceInvalidChar((char) 0x1C));
        assertEquals('�', sut.replaceInvalidChar((char) 0x1D));
        assertEquals('�', sut.replaceInvalidChar((char) 0x1E));
        assertEquals('�', sut.replaceInvalidChar((char) 0x1F));
    }
}
