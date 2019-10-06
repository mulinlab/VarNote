package org.mulinlab.varnote.utils.enumset;

public enum Mode {
    TABIX(0),
    MIX(1),
    SWEEP(2);

    private final int num;
    Mode(final int num) {
        this.num = num;
    }
    public int getNum() {
        return num;
    }
}
