package org.mulinlab.varnote.filters.iterator;


import org.mulinlab.varnote.filters.query.line.LineFilter;
import org.mulinlab.varnote.operations.decode.LocCodec;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.util.List;


public final class LocFilterIterator extends LineFilterIterator {
    private final LocCodec decode;

    public LocFilterIterator(NoFilterIterator iterator, List<LineFilter> filters, final LocCodec decode) {
        super(iterator, filters);
        this.decode = decode;
    }

    @Override
    public LocFeature processLine(final String line) {
        super.processLine(line);
        if(!isFiltered)
            return decode.decode(line);
        else
            return null;
    }
}
