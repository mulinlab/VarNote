package org.mulinlab.varnote.utils.enumset;

import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;

public enum FormatType {
    VCF("vcf"), VCFLIKE("vcfLike"), BED("bed"), BEDALLELE("bedAllele"),
    COORDONLY("coordOnly"), COORDALLELE("coordAllele"), TAB("tab"), RSID("rsid");
    private final String name;

    FormatType(final String name) {
        this.name = name;
    }

    public static String allValues() {
        List<String> vals = new ArrayList<>();
        for (FormatType f:FormatType.values()) {
            vals.add(f.name);
        }

        return StringUtils.join(vals, ", ");
    }

    public static String varValues() {
        List<String> vals = new ArrayList<>();
        for (FormatType f:FormatType.values()) {
            if(!f.name.startsWith("bed") && !f.name.startsWith("rsid")) {
                vals.add(f.name);
            }
        }

        return StringUtils.join(vals, ", ");
    }
}
