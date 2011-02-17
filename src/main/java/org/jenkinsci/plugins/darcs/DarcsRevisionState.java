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
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsRevisionState extends SCMRevisionState {
	private List<String> hashes;

	public DarcsRevisionState(ArrayList<String> hashes) {
		super();
		this.hashes = hashes;
	}
	
    @Override
    public String toString() {
        return "RevisionState " + hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        boolean result = false;
        
        if (other instanceof DarcsRevisionState) {
            DarcsRevisionState that = (DarcsRevisionState) other;
            result = hashCode() == that.hashCode();
        }

        return result;
    }

    @Override
    public int hashCode() {
        return hashes.hashCode();
    }
}
