package org.mulinlab.varnote.config.parser;

import htsjdk.variant.variantcontext.VariantContext;
import org.mulinlab.varnote.config.run.AdvanceToolRunConfig;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.util.Map;

public class CANParser extends AbstractParser {

    private int count;
    private final String[] token = new String[2];

    public CANParser() {
        this.count = 0;
    }

    @Override
    public String processNode(LocFeature query, Map<String, LocFeature[]> dbNodeMap) {
        CANLoc canLoc = new CANLoc(query);

        if(alleleFrequencyFilter(dbNodeMap.get(AdvanceToolRunConfig.GENOMAD_LABEL), 0.05)) {
            LocFeature regBase = getRegBase(query, dbNodeMap.get(AdvanceToolRunConfig.REGBASE_MAP_LABEL));
            LocFeature cosmicFeature = getFeatureMatchRefAndAlt(query, dbNodeMap.get(AdvanceToolRunConfig.COSMIC_LABEL));
            LocFeature icgcFeature = getFeatureMatchRefAndAlt(query, dbNodeMap.get(AdvanceToolRunConfig.ICGC_LABEL));

            canLoc.setRegBase(regBase);
            canLoc.setCosmic(cosmicFeature);
            canLoc.setICGC(icgcFeature);

            System.out.println(canLoc);
        }
        return "";
	}

    @Override
    public void printLog() {

    }

    public int getCount() {
        return count;
    }

    class CANLoc {
        private LocFeature query;
        private double reg1 = -1;
        private double reg2 = -1;

        private String GENE = NO_VALUE;
        private String STRAND = NO_VALUE;
        private String SNP = NO_VALUE;
        private String affected_donors = NO_VALUE;
        private String tested_donors = NO_VALUE;
        private String mutation = NO_VALUE;

        public CANLoc(LocFeature query) {
            this.query = query;
        }

        public void setRegBase(final LocFeature regBase) {
            if(regBase != null) {
                reg1 = Double.parseDouble(regBase.parts[9]);
                reg2 = Double.parseDouble(regBase.parts[10]);
            }
        }

        public void setCosmic(final LocFeature cosmic) {
            if(cosmic != null) {
                VariantContext ctx = cosmic.variantContext;
                GENE = ctx.getAttributeAsString("GENE", NO_VALUE);
                STRAND = ctx.getAttributeAsString("STRAND", NO_VALUE);
                SNP = ctx.getAttributeAsString("SNP", NO_VALUE);
            }
        }

        public void setICGC(final LocFeature icgc) {
            if(icgc != null) {
                VariantContext ctx = icgc.variantContext;
                affected_donors = ctx.getAttributeAsString("affected_donors", NO_VALUE);
                tested_donors = ctx.getAttributeAsString("tested_donors", NO_VALUE);
                mutation = ctx.getAttributeAsString("mutation", NO_VALUE);
            }
        }

        @Override
        public String toString() {
            return "CANLoc{" +
                    "query=" + query +
                    ", reg1=" + reg1 +
                    ", reg2=" + reg2 +
                    ", GENE='" + GENE + '\'' +
                    ", STRAND='" + STRAND + '\'' +
                    ", SNP='" + SNP + '\'' +
                    ", affected_donors='" + affected_donors + '\'' +
                    ", tested_donors='" + tested_donors + '\'' +
                    ", mutation='" + mutation + '\'' +
                    '}';
        }
    }
}
