package org.mulinlab.varnote.utils.node;

import htsjdk.variant.variantcontext.VariantContext;

public class VCFFeature extends LocFeature {

    public VariantContext variantContext;

    public VCFFeature(final VariantContext variantContext) {
        bgzStr = null;

        beg = variantContext.getStart();
        end = variantContext.getEnd();
        chr = variantContext.getContig();
        this.variantContext = variantContext;
    }
}
