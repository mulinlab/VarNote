package org.mulinlab.varnote.filters.iterator;


import org.mulinlab.varnote.filters.query.line.LineFilter;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.util.Iterator;
import java.util.List;


public abstract class LineFilterIterator implements Iterator<LocFeature> {

    protected final NoFilterIterator iterator;
    protected final List<LineFilter> filters;
    protected boolean isFiltered;
    protected long count;
    protected long fcount;
    protected String line;

    protected LocFeature feature;

    public LineFilterIterator(final NoFilterIterator iterator, final List<LineFilter> filters) {
        this.iterator = iterator;
        this.filters = filters;
        this.count = 0;
        this.fcount = 0;
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

    public String nextString() {
        return iterator.next();
    }

    public LocFeature processLine(final String line) {
        this.line = line;
        if(line.trim().equals("")) {
            isFiltered = true;
            fcount++;
            return null;
        } else {
            isFiltered = false;
            for (LineFilter lineFilter: filters) {
                if(lineFilter.isFilterLine(line)) {
                    isFiltered = true;
                    break;
                }
            }
            if(!isFiltered) count++;
            else {
                fcount++;
            }
            return null;
        }
    }

    public String getLine() {
        return line;
    }

    public long getPosition() {
        return iterator.getPosition();
    }

    public long getCount() {
        return count;
    }

    public long getFcount() {
        return fcount;
    }
}
