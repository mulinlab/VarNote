package org.mulinlab.varnote.filters.query;


import htsjdk.variant.variantcontext.VariantContext;
import org.mulinlab.varnote.utils.node.VCFFeature;

public class VCFContextFilter implements LocFeatureFilter<VCFFeature> {

    @Override
    public boolean isFilterLine(VCFFeature loc) {
        VariantContext ctx = loc.variantContext;

//        if(ctx.getAttributeAsDouble("EAS_AF", 0) > 0.01) {
////            System.out.println(ctx.getAttributeAsDouble("EAS_AF", 0) + " true");
//            return true;
//        }
//
//        if(ctx.getAttributeAsInt("AC", 0) < 5) {
////            System.out.println(ctx.getAttributeAsInt("AC", 0) + " true");
//            return true;
//        }

        return false;
    }
}
