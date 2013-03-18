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
 * Darcs XML Sanitizer.
 *
 * The output of "darcs changes --xml-output" might be invalid XML. Darcs treats the patch comments as binary blobs, and
 * the changes command returns them as-is inside the XML structure, without ensuring that the encoding is consistent. If
 * some of the patches in your repository were recorded on UTF-8 machines and others on e.g. ISO-8859 machines, the XML
 * output will contain characters in both encodings.
 *
 * Some parsers (e.g. xerxes) choke on invalid characters in the XML input, so this sanitizer is designed to ensure that
 * the encoding is consistent.
 *
 * @author Ralph Lange <Ralph.Lange@gmx.de>
 */
class DarcsXmlSanitizer {

    /**
     * Used characters.
     */
    private static final List<String> ADDL_CHARSETS = Arrays.asList("ISO-8859-1", "UTF-16");
    /**
     * List of used decoders.
     */
    private final List<CharsetDecoder> decoders = new ArrayList<CharsetDecoder>();

    /**
     * States which indicates where in the comment string we are.
     */
    private enum State {

        /**
         * Outside a name or comment tag.
         */
        OUTSIDE,
        /**
         * Inside a name tag.
         */
        IN_NAME,
        /**
         * Inside a comment tag.
         */
        IN_COMMENT;
    };

    /**
     * Dedicated constructor.
     */
    public DarcsXmlSanitizer() {
        super();
        decoders.add(Charset.forName("UTF-8").newDecoder());

        for (final String cs : ADDL_CHARSETS) {
            decoders.add(Charset.forName(cs).newDecoder());
        }

        // last resort: UTF-8 with replacement
        decoders.add(Charset.forName("UTF-8").newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE));
    }

    /**
     * Knuth-Morris-Pratt pattern matching algorithm.
     *
     * @param data data to inspect
     * @param start start position
     * @param pattern pattern to match
     * @return found position
     */
    private static int positionBeforeNext(final byte[] data, final int start, final byte[] pattern) {
        final int[] failure = computeFailure(pattern);
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

    /**
     * @param data data to inspect
     * @param start start position
     * @param pattern pattern to match
     * @return found position
     */
    private static int positionAfterNext(final byte[] data, final int start, final byte[] pattern) {
        int pos = positionBeforeNext(data, start, pattern);

        if (-1 != pos) {
            pos += pattern.length;
        }

        return pos;
    }

    /**
     * Computes the failure function using a bootstrapping process, where the pattern is matched against itself.
     *
     * @param pattern used pattern
     * @return computed failure
     */
    private static int[] computeFailure(final byte[] pattern) {
        final int[] failure = new int[pattern.length];
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

    /**
     * Cleanse the mixed encoding in the input byte array.
     *
     * @param input dirty input
     * @return cleaned output
     */
    public String cleanse(final byte[] input) {
        final CharBuffer cb = CharBuffer.allocate(input.length);
        CoderResult result;
        State state = State.OUTSIDE;
        int currentPosition = 0;
        int nextPosition = 0;
        int nextName;
        int nextComment;

        while (currentPosition < input.length) {
            switch (state) {
                case OUTSIDE:
                    nextName = positionAfterNext(input, currentPosition, "<name>".getBytes());
                    nextComment = positionAfterNext(input, currentPosition, "<comment>".getBytes());

                    if (-1 != nextName && nextName < nextComment) {
                        nextPosition = nextName;
                        state = State.IN_NAME;
                    } else {
                        nextPosition = nextComment;
                        state = State.IN_COMMENT;
                    }

                    if (-1 == nextPosition) {
                        nextPosition = input.length;
                        state = State.OUTSIDE;
                    }
                    break;
                case IN_NAME:
                    nextPosition = positionBeforeNext(input, nextPosition, "</name>".getBytes());

                    if (-1 != nextPosition) {
                        state = State.OUTSIDE;
                    }

                    break;
                case IN_COMMENT:
                    nextPosition = positionBeforeNext(input, nextPosition, "</comment>".getBytes());

                    if (-1 != nextPosition) {
                        state = State.OUTSIDE;
                    }

                    break;
                default:
                    throw new IllegalStateException(String.format("Illegal state %s!", state));
            }

            final ByteBuffer in = ByteBuffer.wrap(input, currentPosition, nextPosition - currentPosition);
            in.mark();
            cb.mark();

            for (final CharsetDecoder dec : decoders) {
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
            currentPosition += nextPosition - currentPosition;
        }

        cb.flip();
        return cb.toString();
    }

    /**
     * @see #cleanse(byte[])
     * @param file dirty input
     * @return cleaned output
     * @throws IOException if file IO errors happened
     */
    public String cleanse(final File file) throws IOException {
        return cleanse(readFile(file));
    }

    /**
     * Read a file into a byte array.
     *
     * @param file file to read
     * @return read bytes
     * @throws IOException if file IO errors happened
     */
    private byte[] readFile(final File file) throws IOException {
        // Taken from www.exampledepot.com
        // Get the size of the file
        final long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            throw new IOException("File is too large " + file.getName());
        }

        // Create the byte array to hold the data
        final byte[] bytes = new byte[(int) length];
        int offset = 0;
        InputStream is = null;

        try {
            // Read in the bytes
            is = new FileInputStream(file);

            int numRead = 0;
            // FIXME move assignment out of loop condition. May be use Apache Commons IO Utils to read file.
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            if (null != is) {
                is.close();
            }
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        return bytes;
    }

}
