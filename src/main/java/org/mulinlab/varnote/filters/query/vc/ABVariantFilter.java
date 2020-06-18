package org.mulinlab.varnote.filters.query.vc;

import htsjdk.variant.variantcontext.VariantContext;
import org.mulinlab.varnote.filters.query.VariantFilter;
import org.mulinlab.varnote.utils.enumset.VairantQC;
import org.mulinlab.varnote.utils.node.LocFeature;

public abstract class ABVariantFilter implements VariantFilter<LocFeature> {

    protected final VairantQC name;
    protected int filterCount;
    protected int minValue;

    public ABVariantFilter(final VairantQC name) {
        this.name = name;
        filterCount = 0;
    }

    @Override
    public String[] getLogs() {
        return new String[]{ String.format("Filter out %d data of genotype with %s ", filterCount, name) };
    }

    @Override
    public Object clone() {
        return null;
    }

    public String getName() {
        return name.getName();
    }
    public int getCount() {return filterCount; }

    public String[] getCtxValArr(VariantContext ctx) {
        String val = ctx.getAttributeAsString(name.toString(), "ERROR");

        if(!val.equals("ERROR")) {
            if(val.indexOf(",") != -1) {
                val = val.replace("[", "").replace("]", "");
                return val.split(",");
            } else {
                return new String[]{val};
            }
        } else {
            return null;
        }
    }

}
