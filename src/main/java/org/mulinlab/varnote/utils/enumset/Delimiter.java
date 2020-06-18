package org.mulinlab.varnote.utils.enumset;

import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import java.util.Arrays;

public enum Delimiter {
    TAB('\t'), COMMA(',');

    private final char c;
    Delimiter(final char c) {
        this.c = c;
    }

    public char getC() {
        return c;
    }

    public String getCStr() {
        return String.valueOf(c);
    }

    public static Delimiter toDelimiter(String delimiter) {
        for (Delimiter d : Delimiter.values()) {
            if(delimiter.toUpperCase().equals(d.toString())) {
                return d;
            }
        }
        throw new InvalidArgumentException(String.format("'%s' is not a valid/supported delimiter. Valid delimiters are: %s", delimiter, Arrays.asList(Delimiter.values())));
    }
}
