package org.mulinlab.varnote.utils.enumset;

import org.apache.commons.lang3.StringUtils;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;

import java.util.ArrayList;
import java.util.List;

public enum GenomicRegionType {
    GENE_INCLUDE(0),
    GENE_EXCLUDE(1),
    REGION_INCLUDE(2),
    REGION_EXCLUDE(3),
    ALL_GENE_INCLUDE(4),
    ALL_GENE_EXCLUDE(5);

    private final int num;
    GenomicRegionType(final int num) {
        this.num = num;
    }
    public int getNum() {
        return num;
    }

    public static GenomicRegionType toGenomicRegionType(final int value) {
        for (GenomicRegionType t : GenomicRegionType.values()) {
            if(value == t.getNum()) {
                return t;
            }
        }
        throw new InvalidArgumentException(String.format("'%s' is not a valid/supported genomic region type. Valid types are: %s", value, GenomicRegionType.validTypes()));
    }

    public static String validTypes() {
        List<String> s = new ArrayList<>();
        for (GenomicRegionType d: GenomicRegionType.values()) {
            s.add(d.getNum() + ": " + d.toString());
        }

        return "{" + StringUtils.join(s, ", ") + "}";
    }

    public boolean isExclude() {
        switch (this) {
            case ALL_GENE_EXCLUDE:
            case GENE_EXCLUDE:
            case REGION_EXCLUDE:
                return true;
            default:
                return false;
        }
    }

    public boolean isGene() {
        switch (this) {
            case ALL_GENE_INCLUDE:
            case ALL_GENE_EXCLUDE:
            case GENE_INCLUDE:
            case GENE_EXCLUDE:
                return true;
            default:
                return false;
        }
    }
}
