package org.mulinlab.varnote.filters.query.gt;


import htsjdk.variant.variantcontext.Genotype;
import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.filters.query.gt.GenotypeFilter;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.enumset.GenotypeQC;
import org.mulinlab.varnote.utils.node.LocFeature;

public abstract class ABGenotypeFilter implements GenotypeFilter {

    final static Logger logger = LoggingUtils.logger;

    protected final GenotypeQC name;
    protected int filterCount;

    public ABGenotypeFilter(final GenotypeQC name) {
        this.name = name;
        filterCount = 0;
    }

    @Override
    public void printLog() {
        logger.info(String.format("Filter out %d data of genotype with %s ", filterCount, name));
    }
}
