/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jenkinsci.plugins.darcs;

/**
 *
 * @author sxs
 */
public class DarcsCmdException extends Exception {
    public DarcsCmdException(String string) {
        super(string);
    }

    public DarcsCmdException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
}
