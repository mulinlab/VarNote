
package org.mulinlab.varnote.filters.query;


public interface VariantFilter<T> {
    public boolean isFilterLine(final T loc);
    public String[] getLogs();
    public Object clone();
    public String getName();
    public int getCount();
}
