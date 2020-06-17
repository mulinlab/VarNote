package org.mulinlab.varnote.utils.enumset;

import org.mulinlab.varnote.exceptions.InvalidArgumentException;

public enum Snp2PosMode {
    SNP2POS(0),
    POS2SNP(1);

    private final int num;
    Snp2PosMode(final int num) {
        this.num = num;
    }
    public int getNum() {
        return num;
    }

    public static Snp2PosMode toRs2PosMode(final int value) {
        for (Snp2PosMode m : Snp2PosMode.values()) {
            if(value == m.getNum()) {
                return m;
            }
        }
        throw new InvalidArgumentException(String.format("'%s' is not a valid/supported rsid to genomic position mode. Valid modes are: 0, 1.", value));
    }
}
