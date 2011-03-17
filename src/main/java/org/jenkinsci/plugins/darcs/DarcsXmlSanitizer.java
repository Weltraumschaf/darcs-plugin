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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
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
    private enum State {OUTSIDE, IN_NAME, IN_COMMENT};
    
    public DarcsXmlSanitizer() {
        decoders.add(Charset.forName("UTF-8").newDecoder());
        for (String cs : addlCharsets) {
            decoders.add(Charset.forName(cs).newDecoder());
        }
        // last resort: UTF-8 with replacement
        decoders.add(Charset.forName("UTF-8").newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE));
    }

    /**
     * Knuth-Morris-Pratt pattern matching algorithm
     */
    private static int positionBeforeNext(byte[] data, int start, byte[] pattern) {
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

    private static int positionAfterNext(byte[] data, int start, byte[] pattern) {
        int pos = positionBeforeNext(data, start, pattern);
        if (-1 != pos) {
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

    public String cleanse(byte[] input) {
        ByteBuffer in;
        CharBuffer cb = CharBuffer.allocate(input.length);
        CoderResult result;
        State state = State.OUTSIDE;
        int curr_pos = 0;
        int next_name, next_comm;
        int next = 0;

        while (curr_pos < input.length) {
            switch (state) {
                case OUTSIDE:
                    next_name = positionAfterNext(input, curr_pos, "<name>".getBytes());
                    next_comm = positionAfterNext(input, curr_pos, "<comment>".getBytes());
                    if (-1 != next_name && next_name < next_comm) {
                        next = next_name;
                        state = State.IN_NAME;
                    } else {
                        next = next_comm;
                        state = State.IN_COMMENT;
                    }
                    if (-1 == next) {
                        next = input.length;
                        state = State.OUTSIDE;
                    }
                    break;
                case IN_NAME:
                    next = positionBeforeNext(input, next, "</name>".getBytes());
                    if (-1 != next) {
                        state = State.OUTSIDE;
                    }
                    break;
                case IN_COMMENT:
                    next = positionBeforeNext(input, next, "</comment>".getBytes());
                    if (-1 != next) {
                        state = State.OUTSIDE;
                    }
                    break;
            }

            in = ByteBuffer.wrap(input, curr_pos, next - curr_pos);
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
            curr_pos += next - curr_pos;
        }
        cb.flip();
        return cb.toString();
    }

    private byte[] readFile(File file) throws IOException {
        // Taken from www.exampledepot.com
        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            throw new IOException("File is too large " + file.getName());
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        InputStream is = new FileInputStream(file);
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    public String cleanse(File file) throws IOException {
        return cleanse(readFile(file));
    }
}
