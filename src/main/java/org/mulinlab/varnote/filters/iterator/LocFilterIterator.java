package org.mulinlab.varnote.filters.iterator;


import org.mulinlab.varnote.filters.query.VariantFilter;
import org.mulinlab.varnote.filters.query.line.LineFilter;
import org.mulinlab.varnote.operations.decode.LocCodec;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.util.List;


public final class LocFilterIterator extends LineFilterIterator {
    private final LocCodec decode;
    private List<VariantFilter> locFilters;

    public LocFilterIterator(NoFilterIterator iterator, List<LineFilter> filters,  final LocCodec decode) {
        super(iterator, filters);
        this.decode = decode;
    }

    @Override
    public LocFeature processLine(final String line) {
        super.processLine(line);
        if(!isFiltered) {
            final LocFeature ctx = decode.decode(line);
            if(ctx != null) {
                if(locFilters != null && locFilters.size() > 0) {
                    for (VariantFilter variantFilter:locFilters) {
                        if(variantFilter.isFilterLine(ctx)) {
                            return null;
                        }
                    }
                }
                return ctx;
            } else {
                return null;
            }

        } else
            return null;
    }

    public List<VariantFilter> getLocFilters() {
        return locFilters;
    }

    public void setLocFilters(List<VariantFilter> locFilters) {
        this.locFilters = locFilters;
    }
}
