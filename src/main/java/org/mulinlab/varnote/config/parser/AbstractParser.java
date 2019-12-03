package org.mulinlab.varnote.config.parser;

import htsjdk.variant.variantcontext.VariantContext;
import org.mulinlab.varnote.utils.node.LocFeature;


public abstract class AbstractParser implements ResultParser {

    public final static String NO_VALUE = ".";

    public boolean alleleFrequencyFilter(final LocFeature[] gnomadFeatures, final double cutoff) {
        if(gnomadFeatures != null) {

            VariantContext ctx;
            String AF;
            for (LocFeature feature : gnomadFeatures) {
                ctx = feature.variantContext;
                AF = ctx.getAttributeAsString("AF", "ERROR");
                AF = AF.replace("[", "").replace("]", "");
                if (!AF.equals("ERROR") && AF.indexOf(",") != -1) {
                    for (String part : AF.split(",")) {
                        if (Double.parseDouble(part) < cutoff) {
                            return true;
                        }
                    }
                } else if (Double.parseDouble(AF) < cutoff) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public LocFeature getRegBase(final LocFeature query, final LocFeature[] regbaseFeatures) {
        if(regbaseFeatures != null) {
            for (LocFeature locFeature: regbaseFeatures) {
                if(locFeature.parts[3].equals(query.ref) && locFeature.parts[4].equals(query.alt) && !locFeature.parts[5].equals(".")) {
                    return locFeature;
                }
            }
        }

        return null;
    }

    public LocFeature getFeatureMatchRefAndAlt(final LocFeature query, final LocFeature[] features) {
        if(features != null) {
            for (LocFeature locFeature: features) {
                if(locFeature.ref.equals(query.ref) && locFeature.alt.equals(query.alt)) {
                    return locFeature;
                }
            }
        }
        return null;
    }
}
