package org.mulinlab.varnote.filters.query.line;


public final class SkipLineFilter implements LineFilter {

    private int count;
    private int max;
    public SkipLineFilter(final int skip) {
        count = 0;
        this.max = skip;
    }

    @Override
    public boolean isFilterLine(String line) {
        if(count++ < max) {
            System.out.println("skip line " + count);
            return true;
        } else {
            return false;
        }
    }
}
