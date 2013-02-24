/*
 * LICENSE
 *
 * "THE BEER-WARE LICENSE" (Revision 42):
 * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
 * As long as you retain this notice you can do whatever you want with
 * this stuff. If we meet some day, and you think this stuff is worth it,
 * you can buy me a beer in return.
 */
package org.jenkinsci.plugins.darcs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAS based change log parser.
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
class DarcsSaxHandler extends DefaultHandler {

    private static final Logger LOGGER = Logger.getLogger(DarcsSaxHandler.class.getName());

    /**
     * The tags used in the change log XML.
     */
    private enum DarcsChangelogTag {

        CHANGELOG("changelog"),
        PATCH("patch"),
        NAME("name"),
        COMMENT("comment"),
        SUMMARY("summary"),
        MODIFY_FILE("modify_file"),
        ADD_FILE("add_file"),
        REMOVE_FILE("remove_file"),
        MOVE_FILE("move"),
        ADDED_LINES("added_lines"),
        REMOVED_LINES("removed_lines"),
        ADD_DIRECTORY("add_directory"),
        REMOVE_DIRECTORY("remove_directory");
        /**
         * Lookup of string literal to tag.
         */
        private static final Map<String, DarcsChangelogTag> LOOKUP = new HashMap<String, DarcsChangelogTag>();

        static {
            for (final DarcsChangelogTag tag : DarcsChangelogTag.values()) {
                LOOKUP.put(tag.tagName, tag);
            }
        }
        /**
         * Literal tag name
         */
        private final String tagName;

        private DarcsChangelogTag(final String tagName) {
            this.tagName = tagName;
        }

        /**
         * Returns the tag enum to a literal tag name.
         *
         * @param tagName literal tag name, part between the angle brackets
         * @return may return null, if tag name is unknown
         */
        static DarcsChangelogTag forTagName(final String tagName) {
            if (LOOKUP.containsKey(tagName)) {
                return LOOKUP.get(tagName);
            }

            return null;
        }
    }
    /**
     * The current parsed tag.
     */
    private DarcsChangelogTag currentTag;
    /**
     * Current processed change set.
     */
    private DarcsChangeSet currentChangeSet;
    /**
     * Signals that parsing has ended.
     */
    private boolean ready;
    /**
     * Change sets collected during the parse process.
     */
    private final List<DarcsChangeSet> changeSets = new ArrayList<DarcsChangeSet>();
    /**
     * Buffers scanned literals.
     */
    private StringBuilder literalBuffer = new StringBuilder();

    /**
     * Dedicated constructor.
     */
    public DarcsSaxHandler() {
        super();
    }

    /**
     * Returns if the parsing of the XML has ended.
     *
     * @return {@code true} if end of document reached, else {@code false}
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Get the list of parsed change sets.
     *
     * @return may be an empty list, but never {@code null}
     */
    public List<DarcsChangeSet> getChangeSets() {
        return changeSets;
    }

    @Override
    public void endDocument() {
        ready = true;
    }

    private void recognizeTag(final String tagName) {
        final DarcsChangelogTag tag = DarcsChangelogTag.forTagName(tagName);

        if (null == tag) {
            LOGGER.warning(String.format("Unrecognized tag <%s>!", tagName));
        } else {
            currentTag = tag;
        }
    }

    @Override
    public void startElement(final String uri, final String name, final String qName, final Attributes atts) {
        if (DarcsChangelogTag.MODIFY_FILE == currentTag) {
            currentChangeSet.getModifiedPaths().add(literalBuffer.toString());
        }

        recognizeTag(qName);

        if (DarcsChangelogTag.PATCH == currentTag) {
            currentChangeSet = new DarcsChangeSet();
            currentChangeSet.setAuthor(atts.getValue("author"));
            currentChangeSet.setDate(atts.getValue("date"));
            currentChangeSet.setLocalDate(atts.getValue("local_date"));
            currentChangeSet.setHash(atts.getValue("hash"));

            if (atts.getValue("inverted").equals("True")) {
                currentChangeSet.setInverted(true);
            } else if (atts.getValue("inverted").equals("False")) {
                currentChangeSet.setInverted(false);
            }
        } else if (DarcsChangelogTag.MOVE_FILE == currentTag) {
            currentChangeSet.getDeletedPaths().add(atts.getValue("from"));
            currentChangeSet.getAddedPaths().add(atts.getValue("to"));
        }

        literalBuffer = new StringBuilder();
    }

    @Override
    public void endElement(final String uri, final String name, final String qName) {
        recognizeTag(qName);

        switch (currentTag) {
            case PATCH:
                changeSets.add(currentChangeSet);
                break;
            case NAME:
                currentChangeSet.setName(literalBuffer.toString());
                break;
            case COMMENT:
                String comment = stripIgnoreThisFromComment(literalBuffer.toString());
                currentChangeSet.setComment(comment);
                break;
            case ADD_FILE:
            case ADD_DIRECTORY:
                currentChangeSet.getAddedPaths().add(literalBuffer.toString());
                break;
            case REMOVE_FILE:
            case REMOVE_DIRECTORY:
                currentChangeSet.getDeletedPaths().add(literalBuffer.toString());
                break;
        }

        currentTag = null;
    }

    /**
     * Strips out strings like "Ignore-this: 606c40ef0d257da9b7a916e7f1c594aa".
     *
     * It is assumed that after the hash a single line break occurred.
     *
     * @param String comment
     * @return boolean
     */
    static String stripIgnoreThisFromComment(final String comment) {
        if (comment.startsWith("Ignore-this:")) {
            int end = comment.indexOf("\n");

            if (-1 == end) {
                return "";
            }

            return comment.substring(end + 1);
        }

        return comment;
    }

    /**
     *
     * @param char c
     * @return boolean
     */
    private boolean isWhiteSpace(final char c) {
        switch (c) {
            case '\n':
            case '\r':
            case '\t':
            case ' ':
                return true;
            default:
                return false;
        }
    }

    private boolean skipWhiteSpace() {
        return DarcsChangelogTag.NAME != currentTag && DarcsChangelogTag.COMMENT != currentTag;
    }

    @Override
    public void characters(final char ch[], final int start, final int length) {
        for (int i = start; i < start + length; i++) {
            if (isWhiteSpace(ch[i]) && skipWhiteSpace()) {
                continue;
            }

            literalBuffer.append(ch[i]);
        }
    }

    @Override
    public void error(final SAXParseException saxpe) {
        LOGGER.warning(saxpe.toString());
    }

    @Override
    public void fatalError(final SAXParseException saxpe) {
        LOGGER.warning(saxpe.toString());
    }

    @Override
    public void warning(final SAXParseException saxpe) {
        LOGGER.warning(saxpe.toString());
    }
}
