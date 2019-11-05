package org.mulinlab.varnote.filters.iterator;


import org.mulinlab.varnote.filters.query.line.LineFilter;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.util.Iterator;
import java.util.List;


public abstract class LineFilterIterator implements Iterator<LocFeature> {

    protected final NoFilterIterator iterator;
    protected final List<LineFilter> filters;
    protected boolean isFiltered;


    protected LocFeature feature;
    private boolean iterating = false;

    public LineFilterIterator(final NoFilterIterator iterator, final List<LineFilter> filters) {
        this.iterator = iterator;
        this.filters = filters;
    }

    public void close() {
        iterator.close();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    };

    @Override
    public LocFeature next() {
        return processLine(iterator.next());
    }


    public LocFeature processLine(final String line) {
        isFiltered = false;
        for (LineFilter lineFilter: filters) {
            if(lineFilter.isFilterLine(line)) {
                isFiltered = true;
                break;
            }
        }
        return null;
    }

    public long getPosition() {
        return iterator.getPosition();
    }
}
