package org.mulinlab.varnote.filters.iterator;

import htsjdk.variant.vcf.VCFCodec;
import org.mulinlab.varnote.filters.query.LocFeatureFilter;
import org.mulinlab.varnote.filters.query.line.LineFilter;
import org.mulinlab.varnote.utils.node.VCFFeature;
import java.util.List;

public final class VCFFilterIterator extends LineFilterIterator {

    private final List<LocFeatureFilter> locFilters;
    private final VCFCodec decode;

    public VCFFilterIterator(final NoFilterIterator iterator, List<LineFilter> filters, final List<LocFeatureFilter> locFilters,
                             final VCFCodec decode) {
        super(iterator, filters);
        this.locFilters = locFilters;
        this.decode = decode;
    }

    @Override
    public VCFFeature processLine(final String line) {
        super.processLine(line);
        if(!isFiltered) {
            final VCFFeature ctx = new VCFFeature(decode.decode(line));
            for (LocFeatureFilter locFeatureFilter: locFilters) {
                if(locFeatureFilter.isFilterLine(ctx)) {
                    return null;
                }
            }
            ctx.origStr = line;
            return ctx;
        } else {
            return null;
        }
    }
}
