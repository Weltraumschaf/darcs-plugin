
package org.jvnet.hudson.plugins.darcs;

public interface IDarcsApi {

    /**
     * Display help about darcs and darcs commands.
     */
    public void help();

    // Changing and querying the working copy:

    /**
     * Add one or more new files or directories.
     */
    public void add();
    /**
     * Remove files from version control.
     */
    public void remove();
    /**
     * Move or rename files.
     */
    public void move();
    /**
     * Substitute one word for another.
     */
    public void replace();
    /**
     * Discard unrecorded changes.
     */
    public void revert();
    /**
     * Undo the last revert (may fail if changes after the revert).
     */
    public void unrevert();
    /**
     * List unrecorded changes in the working tree.
     */
    public void whatsnew();

    // Copying changes between the working copy and the repository:

    /**
     * Create a patch from unrecorded changes.
     */
    public void record();
    /**
     * Remove recorded patches without changing the working copy.
     */
    public void unrecord();
    /**
     * Improve a patch before it leaves your repository.
     */
    public void amendRecord();
    /**
     * Mark unresolved conflicts in working tree, for manual resolution.
     */
    public void markConflicts();

    //Direct modification of the repository:

    /**
     * Name the current repository state for future reference.
     */
    public void tag();
    /**
     * Set a preference (test, predist, boringfile or binariesfile).
     */
    public void setpref();

    // Querying the repository:

    /**
     * Create a diff between two versions of the repository.
     */
    public void diff();
    /**
     * List patches in the repository.
     */
    public void changes();
    /**
     * Display which patch last modified something.
     */
    public void annotate();
    /**
     * Create a distribution tarball.
     */
    public void dist();
    /**
     * Locate the most recent version lacking an error.
     */
    public void trackdown();
    /**
     * Show information which is stored by darcs.
     */
    public void show();

    // Copying patches between repositories with working copy update:
    /**
     * Copy and apply patches from another repository to this one.
     */
    public void pull();
    /**
     * Fetch patches from another repository, but don't apply them.
     */
    public void fetch();
    /**
     * Delete selected patches from the repository. (UNSAFE!)
     */
    public void obliterate();
    /**
     * Record a new patch reversing some recorded changes.
     */
    public void rollback();
    /**
     * Copy and apply patches from this repository to another one.
     */
    public void push();
    /**
     * Send by email a bundle of one or more patches.
     */
    public void send();
    /**
     * Apply a patch bundle created by `darcs send'.
     */
    public void apply();
    /**
     * Create a local copy of a repository.
     */
    public void get();
    /**
     * Makes a copy of the repository.
     */
    public void put();

    // Administrating repositories:
    /**
     * Make the current directory a repository.
     */
    public void initialize();
    /**
     * Optimize the repository.
     */
    public void optimize();
    /**
     * Check the repository for consistency.
     */
    public void check();
    /**
     * Repair a corrupted repository.
     */
    public void repair();
    /**
     * Convert a repository from a legacy format.
     */
    public void convert();
}