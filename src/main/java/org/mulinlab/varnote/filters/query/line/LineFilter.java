
package org.mulinlab.varnote.filters.query.line;

import htsjdk.variant.variantcontext.VariantContext;


public interface LineFilter {
    public boolean isFilterLine(final String line);
}
