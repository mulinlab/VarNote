package org.mulinlab.varnote.filters.query.gt;

import htsjdk.variant.variantcontext.Genotype;
import org.mulinlab.varnote.utils.enumset.GenotypeQC;
import org.mulinlab.varnote.utils.node.LocFeature;


public final class DepthFilter extends ABGenotypeFilter {
    private final int minDepth;

    public DepthFilter(final int minDepth) {
        super(GenotypeQC.READ_DEPTH);
        this.minDepth = minDepth;
    }

    @Override
    public boolean isFilterLine(final LocFeature loc, final Genotype gt) {
        if (gt.getDP() != -1 && gt.getDP() < minDepth) {
            filterCount++;
            return true;
        }
        else return false;
    }

    @Override
    public Object clone() {
        return new DepthFilter(minDepth);
    }
}
