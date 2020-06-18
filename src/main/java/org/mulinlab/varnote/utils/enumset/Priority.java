package org.mulinlab.varnote.utils.enumset;

import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import java.util.Arrays;

public enum Priority {
    High(1), Median(2), Low(3);

    private final int num;
    Priority(final int num) {
        this.num = num;
    }
    public int getNum() {
        return num;
    }

    public static Priority toPriority(final int value) {
        for (Priority m : Priority.values()) {
            if(value == m.getNum()) {
                return m;
            }
        }
        throw new InvalidArgumentException(String.format("'%s' is not a valid/supported priority. Valid modes are: %s", value, Arrays.asList(OutMode.values())));
    }
}
