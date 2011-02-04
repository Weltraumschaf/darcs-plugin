/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jvnet.hudson.plugins.darcs;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author sxs
 */
public class DarcsSaxHandler extends DefaultHandler {
    
    private static final Logger logger = Logger.getLogger(DarcsSaxHandler.class.getName());

    public enum DarcsChangelogTag {
        CHANGELOG,
        PATCH,
        NAME,
        COMMENT,
        SUMMARY,
        MODIFY_FILE,
        ADD_FILE,
        REMOVE_FILE,
        ADDED_LINES,
        REMOVED_LINES;
    }

    private DarcsChangelogTag currentTag;
    private DarcsChangeSet currentChangeset;
    private boolean ready;
    private List<DarcsChangeSet> changeSets;

    public DarcsSaxHandler() {
        super();
        ready      = false;
        changeSets = new ArrayList<DarcsChangeSet>();
    }

    public boolean isReady() { return ready; }

    public List<DarcsChangeSet> getChangeSets() {
        return changeSets;
    }

    @Override
    public void endDocument () {
	ready = true;
    }

    private void recognizeTag(String tagName) {
        if ("changelog".equals(tagName)) {
            currentTag = DarcsChangelogTag.CHANGELOG;
        } else if ("patch".equals(tagName)) {
            currentTag = DarcsChangelogTag.PATCH;
        } else if ("name".equals(tagName)) {
            currentTag = DarcsChangelogTag.NAME;
        } else if ("comment".equals(tagName)) {
            currentTag = DarcsChangelogTag.COMMENT;
        } else if ("summary".equals(tagName)) {
            currentTag = DarcsChangelogTag.SUMMARY;
        } else if ("modify_file".equals(tagName)) {
            currentTag = DarcsChangelogTag.MODIFY_FILE;
        } else if ("add_file".equals(tagName)) {
            currentTag = DarcsChangelogTag.ADD_FILE;
        } else if ("remove_file".equals(tagName)) {
            currentTag = DarcsChangelogTag.REMOVE_FILE;
        } else if ("added_lines".equals(tagName)) {
            currentTag = DarcsChangelogTag.ADDED_LINES;
        } else if ("removed_lines".equals(tagName)) {
            currentTag = DarcsChangelogTag.REMOVED_LINES;
        }
    }

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
	recognizeTag(qName);
//	logger.log(Level.INFO, "found tag: {0}", currentTag);

        if (DarcsChangelogTag.PATCH == currentTag) {
//            logger.log(Level.INFO, "Create new changeset.");
            currentChangeset = new DarcsChangeSet();
            currentChangeset.setAuthor(atts.getValue("author"));
            currentChangeset.setDate(atts.getValue("date"));
            currentChangeset.setLocalDate(atts.getValue("local_date"));
            currentChangeset.setHash(atts.getValue("hash"));

            if (atts.getValue("inverted").equals("True")) {
                currentChangeset.setInverted(true);
            } else if (atts.getValue("inverted").equals("False")) {
                currentChangeset.setInverted(false);
            }
        }
    }

    @Override
    public void endElement(String uri, String name, String qName) {
        recognizeTag(qName);

        if (DarcsChangelogTag.PATCH == currentTag) {
//            logger.log(Level.INFO, "Add changeset to changeset list.");
            changeSets.add(currentChangeset);
        }
    }

    private boolean isWhiteSpace(char c) {
        if ('\n' == c) {
            return true;
        }

        if ('\r' == c) {
            return true;
        }

        if ('\t' == c) {
            return true;
        }

        if (' ' == c) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public void characters(char ch[], int start, int length) {
        String literal = "";

        for (int i = start; i < start + length; i++) {
            if (isWhiteSpace(ch[i])) {
                continue;
            }

            literal += ch[i];
	}

        if (literal.equals("")) {
            return;
        }

        if (DarcsChangelogTag.NAME == currentTag) {
            currentChangeset.setName(literal);
        } else if (DarcsChangelogTag.COMMENT == currentTag) {
            currentChangeset.setComment(literal);
        } else if (DarcsChangelogTag.MODIFY_FILE == currentTag) {
            currentChangeset.getModifiedPaths().add(literal);
        } else if (DarcsChangelogTag.REMOVED_LINES == currentTag) {
            currentChangeset.getDeletedPaths().add(literal);
        } else if (DarcsChangelogTag.ADDED_LINES == currentTag) {
            currentChangeset.getAddedPaths().add(literal);
        }
    }
}
