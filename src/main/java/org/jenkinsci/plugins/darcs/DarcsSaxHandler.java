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

    /**
     * Logging facility.
     */
    private static final Logger LOGGER = Logger.getLogger(DarcsSaxHandler.class.getName());
    /**
     * True attribute value for boolean tag attributes.
     */
    private static final String ATTR_TRUE = "True";
    /**
     * False attribute value for boolean tag attributes.
     */
    private static final String ATTR_FALSE = "False";

    /**
     * The tags used in the change log XML.
     */
    private enum DarcsChangelogTag {

        /**
         * Tag {@literal <changelog>}.
         */
        CHANGELOG("changelog"),
        /**
         * Tag {@literal <patch>}.
         */
        PATCH("patch"),
        /**
         * Tag {@literal <name>}.
         */
        NAME("name"),
        /**
         * Tag {@literal <comment>}.
         */
        COMMENT("comment"),
        /**
         * Tag {@literal <summary>}.
         */
        SUMMARY("summary"),
        /**
         * Tag {@literal <modify_file>}.
         */
        MODIFY_FILE("modify_file"),
        /**
         * Tag {@literal <add_file>}.
         */
        ADD_FILE("add_file"),
        /**
         * Tag {@literal <remove_file>}.
         */
        REMOVE_FILE("remove_file"),
        /**
         * Tag {@literal <move>}.
         */
        MOVE_FILE("move"),
        /**
         * Tag {@literal <added_lines>}.
         */
        ADDED_LINES("added_lines"),
        /**
         * Tag {@literal <removed_lines>}.
         */
        REMOVED_LINES("removed_lines"),
        /**
         * Tag {@literal <add_directory>}.
         */
        ADD_DIRECTORY("add_directory"),
        /**
         * Tag {@literal <remove_directory>}.
         */
        REMOVE_DIRECTORY("remove_directory");
        /**
         * Lookup of string literal to tag.
         */
        private static final Map<String, DarcsChangelogTag> LOOKUP = new HashMap<String, DarcsChangelogTag>();

        static {
            for (final DarcsChangelogTag tag : DarcsChangelogTag.values()) {
                LOOKUP.put(tag.getTagName().toLowerCase(), tag);
            }
        }
        /**
         * Literal tag name.
         */
        private final String tagName;

        /**
         * Dedicated constructor.
         *
         * @param tagName the string between the angle brackets.
         */
        private DarcsChangelogTag(final String tagName) {
            this.tagName = tagName;
        }

        /**
         * Get the tag name.
         *
         * @return the tag name
         */
        public String getTagName() {
            return tagName;
        }

        /**
         * Returns the tag enum to a literal tag name.
         *
         * @param tagName literal tag name, part between the angle brackets
         * @return may return null, if tag name is unknown
         */
        static DarcsChangelogTag forTagName(final String tagName) {
            if (LOOKUP.containsKey(tagName.toLowerCase())) {
                return LOOKUP.get(tagName);
            }

            return null;
        }
    }

    /**
     * Attributes the {@literal <patch>} has.
     */
    private enum DarcsPatchTagAttribute {

        /**
         * Author attribute.
         */
        AUTHOR("author"),
        /**
         * date attribute.
         */
        DATE("date"),
        /**
         * Local date attribute.
         */
        LOCAL_DATE("local_date"),
        /**
         * Hash attribute.
         */
        HASH("hash"),
        /**
         * Inverted attribute.
         */
        INVERTED("inverted");
        /**
         * Name of the attribute.
         */
        private final String name;

        /**
         * Dedicated constructor.
         *
         * @param name of the attribute
         */
        private DarcsPatchTagAttribute(String name) {
            this.name = name;
        }

        /**
         * Get the attribute name.
         *
         * @return lower cased attribute name
         */
        public String getName() {
            return name;
        }
    }

    /**
     * Attributes the {@literal <move>} has.
     */
    private enum DarcsMoveTagAttribute {
        /** From attribute. */
        FROM("from"),
        /** To attribute. */
        TO("to");
        /**
         * Name of the attribute.
         */
        private final String name;

        /**
         * Dedicated constructor.
         *
         * @param name of the attribute
         */
        private DarcsMoveTagAttribute(String name) {
            this.name = name;
        }

        /**
         * Get the attribute name.
         *
         * @return lower cased attribute name
         */
        public String getName() {
            return name;
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

    /**
     * Recognizes the current scanned tag.
     *
     * Logs a warning if unrecognizable tag occurred and set {@link #currentTag} to {@code null}.
     *
     * @param tagName scanned tag name
     */
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
            currentChangeSet.setAuthor(atts.getValue(DarcsPatchTagAttribute.AUTHOR.getName()));
            currentChangeSet.setDate(atts.getValue(DarcsPatchTagAttribute.DATE.getName()));
            currentChangeSet.setLocalDate(atts.getValue(DarcsPatchTagAttribute.LOCAL_DATE.getName()));
            currentChangeSet.setHash(atts.getValue(DarcsPatchTagAttribute.HASH.getName()));

            if (ATTR_TRUE.equalsIgnoreCase(atts.getValue(DarcsPatchTagAttribute.INVERTED.getName()))) {
                currentChangeSet.setInverted(true);
            } else if (ATTR_FALSE.equalsIgnoreCase(atts.getValue(DarcsPatchTagAttribute.INVERTED.getName()))) {
                currentChangeSet.setInverted(false);
            }
        } else if (DarcsChangelogTag.MOVE_FILE == currentTag) {
            currentChangeSet.getDeletedPaths().add(atts.getValue(DarcsMoveTagAttribute.FROM.getName()));
            currentChangeSet.getAddedPaths().add(atts.getValue(DarcsMoveTagAttribute.TO.getName()));
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
                final String comment = stripIgnoreThisFromComment(literalBuffer.toString());
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
            default:
                LOGGER.info(String.format("Ignored tag <%s>!", currentTag));
        }

        currentTag = null;
    }

    /**
     * Strips out strings like "Ignore-this: 606c40ef0d257da9b7a916e7f1c594aa".
     *
     * It is assumed that after the hash a single line break occurred.
     *
     * @param comment comment message
     * @return cleaned comment message
     */
    static String stripIgnoreThisFromComment(final String comment) {
        if (comment.startsWith("Ignore-this:")) {
            final int end = comment.indexOf('\n');

            if (-1 == end) {
                return "";
            }

            return comment.substring(end + 1);
        }

        return comment;
    }

    /**
     * Determine whether a character is a whitespace character or not.
     *
     * @param c character to check
     * @return {@code true} if passed in char is one of \n, \r, \t, ' '; else {@code false}
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

    /**
     * Return whether to skip white spaces.
     *
     * White spaces are not skipped if parsing the text of name and comment tags.
     *
     * @return {@code false} if current tag is {@link DarcsChangelogTag#NAME} or {@link DarcsChangelogTag#COMMENT};
     * else {@code false}
     */
    private boolean skipWhiteSpace() {
        return DarcsChangelogTag.NAME != currentTag && DarcsChangelogTag.COMMENT != currentTag;
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) {
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
