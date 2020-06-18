package org.mulinlab.varnote.filters.query.gt;


import htsjdk.variant.variantcontext.Genotype;
import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.filters.query.gt.GenotypeFilter;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.enumset.GenotypeQC;
import org.mulinlab.varnote.utils.node.LocFeature;

public abstract class ABGenotypeFilter implements GenotypeFilter {

    protected final GenotypeQC name;
    protected int filterCount;

    public ABGenotypeFilter(final GenotypeQC name) {
        this.name = name;
        filterCount = 0;
    }

    @Override
    public String getLog() {
        return String.format("Filter out %d data of genotype with %s ", filterCount, name);
    }

    @Override
    public Object clone() {
        return null;
    }

    public int getFilterCount() {
        return filterCount;
    }

    public String getName() {
        return name.getName();
    }
    public int getCount() {return filterCount; }
}
