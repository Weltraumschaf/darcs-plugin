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
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsRevisionState extends SCMRevisionState {
    @Override
    public String toString() {
        return "RevisionState xxx";
    }
    
    @Override
    public boolean equals(Object other) {
        boolean result = false;
        
        if (other instanceof DarcsRevisionState) {
            
        }

        return result;
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
