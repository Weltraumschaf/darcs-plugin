/*
 * LICENSE
 *
 * "THE BEER-WARE LICENSE" (Revision 42):
 * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
 * As long as you retain this notice you can do whatever you want with
 * this stuff. If we meet some day, and you think this stuff is worth it,
 * you can buy me a beer in return.
 */

package org.jvnet.hudson.plugins.darcs;

import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.EditType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

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
     * The patch author
     */
    private String author;
    /**
     * The patch date in UTC
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
    private List<String> added    = new ArrayList<String>();
    /**
     * Filles deleted by this patch.
     */
    private List<String> deleted  = new ArrayList<String>();
    /**
     * Filles modified by this patch.
     */
    private List<String> modified = new ArrayList<String>();
    
    /**
     * Filepaths affected by the patch.
     * Lazily computed.
     */
    private volatile List<String> affectedPaths;

    @Exported
    public User getAuthor() {
        return User.get(author);
    }

    @Exported
    public String getComment() {
        return comment;
    }

    @Exported
    public String getDate() {
        return date;
    }

    @Exported
    public String getHash() {
        return hash;
    }

    @Exported
    public boolean isInverted() {
        return inverted;
    }

    @Exported
    public String getLocalDate() {
        return localDate;
    }

    @Exported
    public String getName() {
        return name;
    }

    @Override
    public String getMsg() {
        return getComment();
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public void setLocalDate(String localDate) {
        this.localDate = localDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Collection<String> getAffectedPaths() {
        if (affectedPaths == null) {
            List<String> r = new ArrayList<String>(added.size() + modified.size() + deleted.size());
            r.addAll(added);
            r.addAll(modified);
            r.addAll(deleted);
            affectedPaths = r;
        }
        
        return affectedPaths;
    }

    /**
     * Gets all the files that were added.
     */
    @Exported
    public List<String> getAddedPaths() {
        return added;
    }

    /**
     * Gets all the files that were deleted.
     */
    @Exported
    public List<String> getDeletedPaths() {
        return deleted;
    }

    /**
     * Gets all the files that were modified.
     */
    @Exported
    public List<String> getModifiedPaths() {
        return modified;
    }

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
     */
    public List<EditType> getEditTypes() {
        // return EditType.ALL;
        return Arrays.asList(EditType.ADD, EditType.EDIT, EditType.DELETE);
    }

    @Override
    protected void setParent(ChangeLogSet parent) {
        super.setParent(parent);
    }

    @ExportedBean(defaultVisibility=999)
    public static class Path implements AffectedFile {

        private String src;
        private String dst;
        private char action;
        private String path;
        private DarcsChangeSet changeSet;

        private Path(String source, String destination, char action, String filePath, DarcsChangeSet changeSet) {
            this.src       = source;
            this.dst       = destination;
            this.action    = action;
            this.path      = filePath;
            this.changeSet = changeSet;
        }

        public String getSrc() {
            return src;
        }

        public String getDst() {
            return dst;
        }

        @Exported(name="file")
        public String getPath() {
            return path;
        }

        public DarcsChangeSet getChangeSet() {
            return changeSet;
        }

        @Exported
        public EditType getEditType() {
            switch (action) {
                case 'A':
                    return EditType.ADD;
                case 'R':
                    return EditType.DELETE;
                default:
                    return EditType.EDIT;
            }
        }
    }
}
