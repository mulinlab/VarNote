package org.mulinlab.varnote.filters.query.line;


public final class SkipLineFilter implements LineFilter {

    private long count;
    private long max;
    private boolean isMax = false;

    public SkipLineFilter(final int skip) {
        count = 0;
        this.max = skip;
    }

    @Override
    public boolean isFilterLine(String line) {
        if(!isMax) {
            if(count++ < max) {
//            System.out.println("skip line " + line);
                return true;
            } else {
                isMax = true;
                return false;
            }
        } else {
            return false;
        }
    }
}
