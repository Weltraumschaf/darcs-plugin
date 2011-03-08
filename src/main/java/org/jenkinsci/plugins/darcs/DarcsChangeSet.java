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

import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kohsuke.stapler.export.Exported;

/**
 * Represents a change set (aka. a patch in darcs).
 *
 * <p>
 * The object should be treated like an immutable object.
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsChangeSet extends ChangeLogSet.Entry {
    /**
     * The patch author.
     */
    private String author;
    /**
     * The patch date in UTC.
     */
    private String date;
    /**
     * Localized patch date.
     */
    private String localDate;
    /**
     * Whether it is an inversion of an other patch.
     */
    private boolean inverted;
    /**
     * The patches unique has.
     */
    private String hash;
    /**
     * The patch name.
     */
    private String name;
    /**
     * The patch long comment.
     */
    private String comment;

    /**
     * Filles added by this patch.
     */
    private List<String> added = new ArrayList<String>();
    /**
     * Filles deleted by this patch.
     */
    private List<String> deleted = new ArrayList<String>();
    /**
     * Filles modified by this patch.
     */
    private List<String> modified = new ArrayList<String>();

    /**
     * Filepaths affected by the patch.
     * Lazily computed.
     */
    private volatile List<String> affectedPaths;

    /**
     * Returns the author as User object.
     *
     * If you want the parsed author string from darcs call getPlainAuthor().
     *
     * @return
     */
    @Exported
    public User getAuthor() {
        return User.get(getPlainAuthor());
    }

    /**
     * Returns the plain author string used in the darcs repo.
     *
     * @return
     */
    public String getPlainAuthor() {
        return author;
    }

    /**
     * Returns the patch comment.
     *
     * @return
     */
    @Exported
    public String getComment() {
        return comment;
    }

    /**
     * Returns the patch date as string.
     *
     * @todo a date object would be better.
     * @return
     */
    @Exported
    public String getDate() {
        return date;
    }

    /**
     * Returns the unique hash string of the patch.
     *
     * @return
     */
    @Exported
    public String getHash() {
        return hash;
    }

    /**
     * Returns whether the patch is inverted or not.
     *
     * @return
     */
    @Exported
    public boolean isInverted() {
        return inverted;
    }

    /**
     * Returns the localized date string.
     *
     * @return
     */
    @Exported
    public String getLocalDate() {
        return localDate;
    }

    /**
     * Returns the patch name.
     *
     * @return
     */
    @Exported
    public String getName() {
        return name;
    }

    /**
     * Method for fullfill the interface. Delegates to getComment().
     *
     * @return
     */
    @Override
    public String getMsg() {
        return getComment();
    }

    /**
     * Sets the author string from darcs.
     *
     * Thus this object should be treated as imutable, this setter should only
     * be called from the DarcsChangeLogParser.
     *
     * @param anAuthor
     */
    public void setAuthor(String anAuthor) {
        author = anAuthor;
    }

    /**
     * Sets the comment string.
     *
     * Thus this object should be treated as imutable, this setter should only
     * be called from the DarcsChangeLogParser.
     *
     * @param aComment
     */
    public void setComment(String aComment) {
        comment = aComment;
    }

    /**
     * Sets the date string.
     *
     * Thus this object should be treated as imutable, this setter should only
     * be called from the DarcsChangeLogParser.
     *
     * @param aDate
     */
    public void setDate(String aDate) {
        date = aDate;
    }

    /**
     * Sets the hash string.
     *
     * Thus this object should be treated as imutable, this setter should only
     * be called from the DarcsChangeLogParser.
     *
     * @param aHash
     */
    public void setHash(String aHash) {
        hash = aHash;
    }

    /**
     * Sets the inverted flag.
     *
     * Thus this object should be treated as imutable, this setter should only
     * be called from the DarcsChangeLogParser.
     *
     * @param isInverted
     */
    public void setInverted(boolean isInverted) {
        inverted = isInverted;
    }

    /**
     * Sets the localized date string.
     *
     * Thus this object should be treated as imutable, this setter should only
     * be called from the DarcsChangeLogParser.
     *
     * @param aLocalDate
     */
    public void setLocalDate(String aLocalDate) {
        localDate = aLocalDate;
    }

    /**
     * Sets the patch name.
     *
     * Thus this object should be treated as imutable, this setter should only
     * be called from the DarcsChangeLogParser.
     *
     * @param aName
     */
    public void setName(String aName) {
        name = aName;
    }

    /**
     * Returns a list of all files affected by this patch.
     *
     * @return
     */
    @Override
    public List<String> getAffectedPaths() {
        if (null == affectedPaths) {
            int size = added.size() + modified.size() + deleted.size();
            List<String> r = new ArrayList<String>(size);
            r.addAll(added);
            r.addAll(deleted);
            r.addAll(modified);
            affectedPaths = r;
        }

        return affectedPaths;
    }

    /**
     * Gets all the files that were added.
     *
     * @return
     */
    @Exported
    public List<String> getAddedPaths() {
        return added;
    }

    /**
     * Gets all the files that were deleted.
     *
     * @return
     */
    @Exported
    public List<String> getDeletedPaths() {
        return deleted;
    }

    /**
     * Gets all the files that were modified.
     *
     * @return
     */
    @Exported
    public List<String> getModifiedPaths() {
        return modified;
    }

    /**
     * Convenience methodfor getting affected paths by type.
     *
     * @param kind
     * @return
     */
    public List<String> getPaths(EditType kind) {
        if (kind == EditType.ADD) {
            return getAddedPaths();
        }

        if (kind == EditType.EDIT) {
            return getModifiedPaths();
        }

        if (kind == EditType.DELETE) {
            return getDeletedPaths();
        }

        return null;
    }

    /**
     * Returns all three variations of {@link EditType}.
     * Placed here to simplify access from views.
     *
     * @return
     */
    public List<EditType> getEditTypes() {
        return Arrays.asList(EditType.ADD, EditType.EDIT, EditType.DELETE);
    }

    /**
     * See ChangeLogSet.Entry.setParent().
     *
     * @param parent
     */
    @Override
    protected void setParent(ChangeLogSet parent) {
        super.setParent(parent);
    }
}
