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

import hudson.scm.SCMRevisionState;
import org.apache.commons.lang.Validate;

/**
 * Represents the revision state of a repository.
 *
 * The state consists of all changes in a repository. The comparison is made over a digest from the DarcsChangeSetList.
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsRevisionState extends SCMRevisionState {

    /**
     * Holds all patches as DarcsChangeSet objects.
     */
    private final DarcsChangeSetList changes;

    /**
     * Initializes object with empty change set list.
     */
    public DarcsRevisionState() {
        this(new DarcsChangeSetList());
    }

    /**
     * Dedicated constructor.
     *
     * @param changes list of change sets
     */
    public DarcsRevisionState(final DarcsChangeSetList changes) {
        super();
        Validate.notNull(changes);
        this.changes = changes;
    }

    /**
     * Returns the current revision state change set list.
     *
     * @return never {@code null}
     */
    public DarcsChangeSetList getChanges() {
        return changes;
    }

    @Override
    public String toString() {
        return getChanges().digest();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof DarcsRevisionState)) {
            return false;
        }

        final DarcsRevisionState other = (DarcsRevisionState) obj;
        return getChanges().equals(other.getChanges());
    }

    @Override
    public int hashCode() {
        return changes.hashCode();
    }

}
