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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Darcs XML Sanitizer
 *
 * The output of "darcs changes --xml-output" might be invalid XML.
 * Darcs treats the patch comments as binary blobs, and the changes command
 * returns them as-is inside the XML structure, without ensuring that the encoding
 * is consistent. If some of the patches in your repository were recorded on UTF-8
 * machines and others on e.g. ISO-8859 machines, the XML output will contain
 * characters in both encodings.
 *
 * Some parsers (e.g. xerxes) choke on invalid characters in the XML input, so
 * this sanitizer is designed to ensure that the encoding is consistent.
 *
 * @author Ralph Lange <Ralph.Lange@gmx.de>
 */
public class DarcsXmlSanitizer {

    static final List<String> addlCharsets =
            Arrays.asList("ISO-8859-1", "UTF-16");
    List<CharsetDecoder> decoders = new ArrayList<CharsetDecoder>();

    public DarcsXmlSanitizer() {
        decoders.add(Charset.forName("UTF-8").newDecoder());
        for (String cs : addlCharsets) {
            decoders.add(Charset.forName(cs).newDecoder());
        }
    }

    /**
     * Knuth-Morris-Pratt pattern matching algorithm
     */
    private static int positionOfNext(byte[] data, int start, byte[] pattern) {
        int[] failure = computeFailure(pattern);
        int j = 0;
        if (0 == data.length || start >= data.length) {
            return -1;
        }
        for (int i = start; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    private static int positionBeforeNext(byte[] data, int start, byte[] pattern) {
        int pos = positionOfNext(data, start, pattern);
        if (-1 == pos) {
            pos = data.length;
        }
        return pos;
    }

    private static int positionAfterNext(byte[] data, int start, byte[] pattern) {
        int pos = positionOfNext(data, start, pattern);
        if (-1 == pos) {
            pos = data.length;
        } else {
            pos += pattern.length;
        }
        return pos;
    }

    /**
     * Computes the failure function using a bootstrapping process,
     * where the pattern is matched against itself.
     */
    private static int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];
        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }
        return failure;
    }

    public String cleanse(byte[] input, String encoding) {
        CharsetDecoder dec_utf8 = decoders.get(0);
        ByteBuffer in;
        CharBuffer cb = CharBuffer.allocate(input.length);
        CoderResult result;
        int curr_pos = 0;
        int next_name, next_comm, next, end;

        while (curr_pos < input.length) {
            next_name = positionAfterNext(input, curr_pos, "<name>".getBytes());
            next_comm = positionAfterNext(input, curr_pos, "<comment>".getBytes());

            if (next_name < next_comm) {
                next = next_name;
                end = positionBeforeNext(input, next, "</name>".getBytes());
            } else {
                next = next_comm;
                end = positionBeforeNext(input, next, "</comment>".getBytes());
            }

            in = ByteBuffer.wrap(input, curr_pos, next - curr_pos);
            dec_utf8.reset();
            dec_utf8.decode(in, cb, true);
            dec_utf8.flush(cb);
            curr_pos += next - curr_pos;

            if (curr_pos >= input.length)
                break;

            in = ByteBuffer.wrap(input, curr_pos, end - curr_pos);
            in.mark();
            cb.mark();
            for (CharsetDecoder dec : decoders) {
                dec.reset();
                result = dec.decode(in, cb, true);
                if (result.isError()) {
                    in.reset();
                    cb.reset();
                    continue;
                } else {
                    dec.flush(cb);
                    break;
                }
            }
            curr_pos += end - curr_pos;
        }
        return cb.flip().toString();
    }
}
