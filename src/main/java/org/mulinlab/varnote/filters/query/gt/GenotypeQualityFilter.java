package org.mulinlab.varnote.filters.query.gt;

import htsjdk.variant.variantcontext.Genotype;
import org.mulinlab.varnote.utils.enumset.GenotypeQC;
import org.mulinlab.varnote.utils.node.LocFeature;

public final class GenotypeQualityFilter extends ABGenotypeFilter {
    private final int minGq;

    public GenotypeQualityFilter(final int minGq) {
        super(GenotypeQC.QULITY_SCORE);
        this.minGq = minGq;
    }

    @Override
    public boolean isFilterLine(final LocFeature loc, final Genotype gt) {
        if (gt.getGQ() < minGq) {
            filterCount++;
            return true;
        }
        else return false;
    }
}
