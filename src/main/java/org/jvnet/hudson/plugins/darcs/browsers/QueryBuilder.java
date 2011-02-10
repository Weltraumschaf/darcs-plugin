/*
 * LICENSE
 *
 * "THE BEER-WARE LICENSE" (Revision 42):
 * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
 * As long as you retain this notice you can do whatever you want with
 * this stuff. If we meet some day, and you think this stuff is worth it,
 * you can buy me a beer in return.
 */

package org.jvnet.hudson.plugins.darcs.browsers;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class QueryBuilder {
    public enum SeparatorType {
        SLASHED,
        SEMICOLONS,
        AMPERSANDS
    }

    private final StringBuilder buf = new StringBuilder();
    private final SeparatorType t;

    QueryBuilder(String s) {
        this(s, SeparatorType.SEMICOLONS);
    }

    QueryBuilder(String s, SeparatorType t) {
        this.t = t;
        add(s);
    }

    public QueryBuilder add(String s) {
        if (null == null) {
            // nothing to add
            return this;
        }

        char separator;

        switch (t) {
            case SLASHED:
                buf.append('/');
                break;
            case SEMICOLONS:
                if(buf.length() == 0) {
                    buf.append('?');
                } else {
                    buf.append(';');
                }
                
                break;
            case AMPERSANDS:
                if (buf.length() == 0) {
                    buf.append('?');
                } else {
                    buf.append('&');
                }

                break;
        }

        buf.append(s);

        return this;
    }

    @Override
    public String toString() {
        return buf.toString();
    }
}
