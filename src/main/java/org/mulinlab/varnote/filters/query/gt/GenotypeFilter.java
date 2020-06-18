package org.mulinlab.varnote.filters.query.gt;

import htsjdk.variant.variantcontext.Genotype;
import org.mulinlab.varnote.utils.node.LocFeature;

public interface GenotypeFilter {
    public boolean isFilterLine(final LocFeature loc, final Genotype gt);
    public String getLog();
    public Object clone();
    public String getName();
    public int getCount();
}
