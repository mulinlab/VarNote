package org.mulinlab.varnote.utils.enumset;

public enum GenotypeQC {
    QULITY_SCORE("Genotype Quality"),
    READ_DEPTH("Read Depth"),
    ALLELIC_DEPTH("Allelic Depths");

    private final String name;
    GenotypeQC(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
