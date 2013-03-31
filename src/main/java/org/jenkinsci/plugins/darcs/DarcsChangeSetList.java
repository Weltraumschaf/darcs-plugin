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

import hudson.Util;
import hudson.scm.ChangeLogSet;
import hudson.model.AbstractBuild;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import org.apache.commons.lang.Validate;

/**
 * List of change set that went into a particular build.
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsChangeSetList extends ChangeLogSet<DarcsChangeSet> {

    /**
     * Kind description string.
     */
    private static final String KIND = "darcs";
    /**
     * Set of the changes.
     */
    private final List<DarcsChangeSet> changeSets;
    /**
     * Lazy computed digest over all change set hashes.
     */
    private String digest;

    /**
     * Convenience constructor with empty change set list.
     */
    public DarcsChangeSetList() {
        this(new ArrayList<DarcsChangeSet>());
    }

    /**
     * Constructs without build.
     *
     * @param changes list of patches
     */
    public DarcsChangeSetList(final List<DarcsChangeSet> changes) {
        this(null, changes);
    }

    /**
     * Constructs with build and changes.
     *
     * @param build current build associated with change set
     * @param changes list of patches
     */
    @SuppressWarnings("LeakingThisInConstructor") // because its' at the end od constructor
    public DarcsChangeSetList(final AbstractBuild build, final List<DarcsChangeSet> changes) {
        super(build);
        Validate.notNull(changes);
        // we want the changesets allways in same order for digesting
        Collections.sort(changes, new Comparator<DarcsChangeSet>() {
            public int compare(DarcsChangeSet a, DarcsChangeSet b) {
                return a.getHash().compareTo(b.getHash());
            }
        });
        changeSets = Collections.unmodifiableList(changes);

        for (final DarcsChangeSet log : changes) {
            log.setParent(this); // XXX Consider if necessary.
        }
    }

    @Override
    public boolean isEmptySet() {
        return getChangeSets().isEmpty();
    }

    /**
     * Returns an iterator for the list.
     *
     * @return change set iterator
     */
    public Iterator<DarcsChangeSet> iterator() {
        return getChangeSets().iterator();
    }

    /**
     * Returns the count of change sets.
     *
     * @return size of changes
     */
    public int size() {
        return getChangeSets().size();
    }

    /**
     * Returns the change set list.
     *
     * @return change set list
     */
    public List<DarcsChangeSet> getChangeSets() {
        return changeSets;
    }

    /**
     * Returns the kind as string.
     *
     * @return {@value #KIND}
     */
    @Override
    public String getKind() {
        return KIND;
    }

    /**
     * Calculates MD5 digest over all change sets Darcs hashes.
     *
     * @return MD5 string
     */
    private String calcDigest() {
        final StringBuilder buffer = new StringBuilder();

        for (final DarcsChangeSet cs : changeSets) {
            buffer.append(cs.getHash());
        }

        return Util.getDigestOf(buffer.toString());
    }

    /**
     * Returns the digest for the whole change set list.
     *
     * Lazy computes the digest one time.
     *
     * @return MD5 hashed digest
     */
    public String digest() {
        if (null == digest) {
            digest = calcDigest();
        }

        return digest;
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof DarcsChangeSetList)) {
            return false;
        }

        final DarcsChangeSetList other = (DarcsChangeSetList) object;
        return DarcsObjects.equal(digest(), other.digest());
    }

    @Override
    public int hashCode() {
        return digest().hashCode();
    }

    @Override
    public String toString() {
        return DarcsObjects.toString("DarcsChangeSetList")
            .add("changeSets", changeSets)
            .add("digest", digest())
            .toString();
    }

}
