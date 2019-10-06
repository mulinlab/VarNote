package org.mulinlab.varnote.utils.enumset;

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
}
