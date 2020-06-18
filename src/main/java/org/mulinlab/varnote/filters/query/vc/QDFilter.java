package org.mulinlab.varnote.filters.query.vc;

import org.mulinlab.varnote.utils.enumset.VairantQC;
import org.mulinlab.varnote.utils.node.LocFeature;

public final class QDFilter extends ABVariantFilter {
    public QDFilter(final int minValue) {
        super(VairantQC.QD);
        this.minValue = minValue;
    }

    @Override
    public boolean isFilterLine(LocFeature loc) {
        final String[] vals = getCtxValArr(loc.variantContext);
        if(vals == null) {
            return false;
        } else {
            for (String val:vals) {
                if(Double.parseDouble(val.trim()) >= minValue) {
                    return false;
                }
            }

            filterCount++;
            return true;
        }
    }

    @Override
    public Object clone() {
        return new QDFilter(minValue);
    }
}
