package org.mulinlab.varnote.utils.enumset;

public enum VairantQC {
    QD("Variant Confidence/Quality by Depth"),
    AC("Allele Count"),
    AF("Allele Frequency"),
    AN("Allele Number");

    private final String name;
    VairantQC(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
