package org.mulinlab.varnote.utils.enumset;

import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import java.util.Arrays;

public enum OutMode {
    QUERY(0),
    DB(1),
    BOTH(2);

    private final int num;
    OutMode(final int num) {
        this.num = num;
    }
    public int getNum() {
        return num;
    }

    public static OutMode toOutMode(final int value) {
        for (OutMode m : OutMode.values()) {
            if(value == m.getNum()) {
                return m;
            }
        }
        throw new InvalidArgumentException(String.format("'%s' is not a valid/supported output mode. Valid modes are: %s", value, Arrays.asList(OutMode.values())));
    }
}
