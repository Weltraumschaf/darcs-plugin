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
 * Represents a change set (aka. a patch in Darcs).
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
     * Returns the author as User object.
     *
     * If you want the parsed author string from Darcs call getPlainAuthor().
     *
     * @return a Jenkins user object
     */
    @Exported
    public User getAuthor() {
        return User.get(getPlainAuthor());
    }

    /**
     * Returns the plain author string used in the Darcs repository.
     *
     * @return author name
     */
    public String getPlainAuthor() {
        return author;
    }

    /**
     * Returns the patch comment.
     *
     * @return comment message
     */
    @Exported
    public String getComment() {
        return comment;
    }

    /**
     * Returns the patch date as string.
     *
     * @return date string in UTC
     */
    @Exported
    public String getDate() {
        return date;
    }

    /**
     * Returns the unique hash string of the patch.
     *
     * @return hash string
     */
    @Exported
    public String getHash() {
        return hash;
    }

    /**
     * Returns whether the patch is inverted or not.
     *
     * @return {@code true} if it is an inverse patch, else {@code false}
     */
    @Exported
    public boolean isInverted() {
        return inverted;
    }

    /**
     * Returns the localized date string.
     *
     * @return local date string in UTC
     */
    @Exported
    public String getLocalDate() {
        return localDate;
    }

    /**
     * Returns the patch name.
     *
     * @return the patch name
     */
    @Exported
    public String getName() {
        return name;
    }

    /**
     * Method for fulfill the interface.
     *
     * Delegates to {@link #getComment()}.
     *
     * @return same as {@link #getComment()}
     */
    @Override
    public String getMsg() {
        return getComment();
    }

    /**
     * Sets the author string from Darcs.
     *
     * Thus this object should be treated as immutable, this setter should only be called from the DarcsChangeLogParser.
     *
     * @param anAuthor author name string
     */
    public void setAuthor(final String anAuthor) {
        author = anAuthor;
    }

    /**
     * Sets the comment string.
     *
     * Thus this object should be treated as immutable, this setter should only be called from the DarcsChangeLogParser.
     *
     * @param aComment patch comment message
     */
    public void setComment(final String aComment) {
        comment = aComment;
    }

    /**
     * Sets the date string.
     *
     * Thus this object should be treated as immutable, this setter should only be called from the DarcsChangeLogParser.
     *
     * @param aDate date in UTC
     */
    public void setDate(final String aDate) {
        date = aDate;
    }

    /**
     * Sets the hash string.
     *
     * Thus this object should be treated as immutable, this setter should only be called from the DarcsChangeLogParser.
     *
     * @param aHash hash string
     */
    public void setHash(final String aHash) {
        hash = aHash;
    }

    /**
     * Sets the inverted flag.
     *
     * Thus this object should be treated as immutable, this setter should only be called from the DarcsChangeLogParser.
     *
     * @param isInverted {@code true} if it is an inverse patch, else {@code false}
     */
    public void setInverted(final boolean isInverted) {
        inverted = isInverted;
    }

    /**
     * Sets the localized date string.
     *
     * Thus this object should be treated as immutable, this setter should only be called from the DarcsChangeLogParser.
     *
     * @param aLocalDate date in UTC
     */
    public void setLocalDate(final String aLocalDate) {
        localDate = aLocalDate;
    }

    /**
     * Sets the patch name.
     *
     * Thus this object should be treated as immutable, this setter should only be called from the DarcsChangeLogParser.
     *
     * @param aName patch name
     */
    public void setName(final String aName) {
        name = aName;
    }

    /**
     * Returns a lazy computed list of all files affected by this patch.
     *
     * @return the list is recalculated on each call
     */
    @Override
    public List<String> getAffectedPaths() {
        return new ArrayList<String>() {
            {
                addAll(added);
                addAll(deleted);
                addAll(modified);
            }
        };
    }

    /**
     * Gets all the files that were added.
     *
     * @return modifiable list
     */
    @Exported
    public List<String> getAddedPaths() {
        return added;
    }

    /**
     * Gets all the files that were deleted.
     *
     * @return modifiable list
     */
    @Exported
    public List<String> getDeletedPaths() {
        return deleted;
    }

    /**
     * Gets all the files that were modified.
     *
     * @return modifiable list
     */
    @Exported
    public List<String> getModifiedPaths() {
        return modified;
    }

    /**
     * Convenience method for getting affected paths by type.
     *
     * @param kind one of {@link EditType#ADD}, {@link EditType#EDIT}, {@link EditType#DELETE}
     * @return list associated to the edit type
     */
    public List<String> getPaths(final EditType kind) {
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

    @Override
    protected void setParent(final ChangeLogSet parent) { // NOPMD Needed w/ public access in DarcsChangeSetList.
        super.setParent(parent);
    }

    /**
     * Returns all three variations of {@link EditType}. Placed here to simplify access from views.
     *
     * @return available edit types
     */
    public List<EditType> getEditTypes() {
        return Arrays.asList(EditType.ADD, EditType.EDIT, EditType.DELETE);
    }

    @Override
    public String toString() {
        return DarcsObjects.toString("DarcsChangeSet")
            .add("hash", hash)
            .add("name", name)
            .add("author", author)
            .add("date", date)
            .add("localDate", localDate)
            .add("inverted", inverted)
            .add("added", added)
            .add("modified", modified)
            .add("deleted", deleted)
            .toString();
    }

    @Override
    public int hashCode() {
        return DarcsObjects.hashCode(
            added,
            author,
            comment,
            date,
            deleted,
            hash,
            inverted,
            localDate,
            modified,
            name
        );
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof DarcsChangeSet)) {
            return false;
        }

        final DarcsChangeSet other = (DarcsChangeSet) obj;

        if (DarcsObjects.notEqual(added, other.added)) {
            return false;
        }

        if (DarcsObjects.notEqual(author, other.author)) {
            return false;
        }

        if (DarcsObjects.notEqual(comment, other.comment)) {
            return false;
        }

        if (DarcsObjects.notEqual(date, other.date)) {
            return false;
        }

        if (DarcsObjects.notEqual(deleted, other.deleted)) {
            return false;
        }

        if (DarcsObjects.notEqual(hash, other.hash)) {
            return false;
        }

        if (DarcsObjects.notEqual(localDate, other.localDate)) {
            return false;
        }

        if (DarcsObjects.notEqual(modified, other.modified)) {
            return false;
        }

        if (DarcsObjects.notEqual(inverted, other.inverted)) {
            return false;
        }

        if (DarcsObjects.notEqual(name, other.name)) {
            return false;
        }

        return true;
    }

}
