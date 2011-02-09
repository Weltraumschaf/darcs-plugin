
package org.jvnet.hudson.plugins.darcs.browsers;

/**
 *
 * @author 
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
