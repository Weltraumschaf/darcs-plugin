/*
 * LICENSE
 *
 * "THE BEER-WARE LICENSE" (Revision 42):
 * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
 * As long as you retain this notice you can do whatever you want with
 * this stuff. If we meet some day, and you think this stuff is worth it,
 * you can buy me a beer in return.
 */
package org.jenkinsci.plugins.darcs.browsers;

/**
 * Helper class to build URL queries.
 *
 * Queries are constructed by partial strings combined by separator characters.
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
final class DarcsQueryBuilder {

    /**
     * Types for queries.
     */
    enum SeparatorType {
        /**
         * Separates everything with slash, REST like.
         */
        SLASHES,
        /**
         * Starts wit {@literal ?} and then separates with {@literal ;}.
         */
        SEMICOLONS,
        /**
         * Starts wit {@literal ?} and then separates with {@literal &}.
         */
        AMPERSANDS;
    }

    /**
     * Buffers the builded query string.
     */
    private final StringBuilder buf = new StringBuilder();
    /**
     * The separator type for the query.
     */
    private final SeparatorType type;

    /**
     * Does not add a first string.
     *
     * @param t separator type
     */
    DarcsQueryBuilder(final SeparatorType t) {
        this(t, null);
    }

    /**
     * Dedicated constructor.
     *
     * @param t separator type
     * @param s first string of query
     */
    DarcsQueryBuilder(final SeparatorType t, final String s) {
        super();
        type = t;
        add(s);
    }

    /**
     * Get the separator type.
     *
     * @return type of separation
     */
    public SeparatorType getType() {
        return this.type;
    }

    /**
     * Add a string part.
     *
     * @param s partial string
     * @return return itself
     */
    public DarcsQueryBuilder add(final String s) {
        if (null == s) {
            // nothing to add
            return this;
        }

        switch (this.type) {
            case SLASHES:
                buf.append('/');

                break;
            case SEMICOLONS:
                if (buf.length() == 0) {
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
            default:
                throw new IllegalStateException(String.format("Unsupported separator type %s", type));
        }

        buf.append(s);

        return this;
    }

    @Override
    public String toString() {
        return buf.toString();
    }

}
