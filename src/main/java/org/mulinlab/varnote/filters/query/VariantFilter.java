
package org.mulinlab.varnote.filters.query;
import org.mulinlab.varnote.utils.node.LocFeature;

public interface VariantFilter<T> {
    public boolean isFilterLine(final T loc);
    public void printLog();
}
