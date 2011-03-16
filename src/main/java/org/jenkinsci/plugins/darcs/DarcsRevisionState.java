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

/**
 * Represents the revision state of a repo.
 * 
 * The state consits of all changes in a repo. The comparison is made over
 * a digest from the DarcsChangeSetList.
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsRevisionState extends SCMRevisionState {

    /**
     * Holds all patches as DarcsChangeSet objects.
     */
    private final DarcsChangeSetList changes;

    public DarcsRevisionState() {
        this(new DarcsChangeSetList());
    }
    
    public DarcsRevisionState(DarcsChangeSetList changes) {
        super();
        this.changes = changes;
    }

    /**
     * Returns the current revison state change set list.
     * 
     * @return 
     */
    public DarcsChangeSetList getChanges() {
        return changes;
    }

    /**
     * Returns the change set lists digest as string.
     * 
     * @return 
     */
    @Override
    public String toString() {
        return getChanges().digest();
    }

    /**
     * Compares the change set lists.
     * 
     * @param other
     * @return 
     */
    @Override
    public boolean equals(Object other) {
        boolean result = false;

        if (other instanceof DarcsRevisionState) {
            DarcsRevisionState that = (DarcsRevisionState) other;
            return getChanges().equals(that.getChanges());
        }

        return result;
    }

    /**
     * Returns the change set lists hash code.
     * 
     * @return 
     */
    @Override
    public int hashCode() {
        return changes.hashCode();
    }
}
