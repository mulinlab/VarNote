package org.mulinlab.varnote.config.parser;

import org.mulinlab.varnote.config.run.AdvanceToolRunConfig;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PATParser extends AbstractParser {

    private int count;
    private final String[] token = new String[2];
    private final List<PATLoc> results;

    public PATParser() {
        this.count = 0;
        this.results = new ArrayList<>();
    }

    @Override
    public String processNode(LocFeature query, Map<String, LocFeature[]> dbNodeMap) {
		LocFeature[] gnomadFeatures = dbNodeMap.get(AdvanceToolRunConfig.GENOMAD_LABEL);
        LocFeature[] dbnsfpFeatures = dbNodeMap.get(AdvanceToolRunConfig.DBNSFP_LABEL);

        if(alleleFrequencyFilter(gnomadFeatures, 0.005)) {
            double reg1 = -1, reg2 = -1;
            LocFeature regBase = getRegBase(query, dbNodeMap.get(AdvanceToolRunConfig.REGBASE_MAP_LABEL));
            if(regBase != null) {
                reg1 = Double.parseDouble(regBase.parts[7]);
                reg2 = Double.parseDouble(regBase.parts[8]);
            }

            //genename cds_strand refcodon codonpos  REVEL_score  REVEL_rankscore
            if(dbnsfpFeatures != null) {
                for (LocFeature feature : dbnsfpFeatures) {
                    if(feature.ref.equals(query.ref) && feature.alt.equals(query.alt)) {
                        if(!feature.parts[69].equals(".") && !feature.parts[70].equals(".")) {
                            System.out.println(query.getOrigStr());
                            this.results.add(new PATLoc(query, feature.parts[11], feature.parts[12], feature.parts[13], feature.parts[14],
                            Double.parseDouble(feature.parts[69]), Double.parseDouble(feature.parts[70]), reg1, reg2));
                        }
                    }
                }
            }
        }
        return null;
	}

    public int getCount() {
        return count;
    }

    @Override
    public void printLog() { }

    public List<PATLoc> getResults() {
        return results;
    }

    class PATLoc {
        private final LocFeature query;
        private final String genename;
        private final String cds_strand;
        private final String refcodon;
        private final String codonpos;
        private final double REVEL_score;
        private final double REVEL_rankscore;
        private final double reg1;
        private final double reg2;

        public PATLoc(LocFeature query, String genename, String cds_strand, String refcodon, String codonpos, double REVEL_score, double REVEL_rankscore,
                double reg1, double reg2) {
            this.query = query;
            this.genename = genename;
            this.cds_strand = cds_strand;
            this.refcodon = refcodon;
            this.codonpos = codonpos;
            this.REVEL_score = REVEL_score;
            this.REVEL_rankscore = REVEL_rankscore;
            this.reg1 = reg1;
            this.reg2 = reg2;
        }

        public LocFeature getQuery() {
            return query;
        }

        public String getGenename() {
            return genename;
        }

        public String getCds_strand() {
            return cds_strand;
        }

        public String getRefcodon() {
            return refcodon;
        }

        public String getCodonpos() {
            return codonpos;
        }

        public double getREVEL_score() {
            return REVEL_score;
        }

        public double getREVEL_rankscore() {
            return REVEL_rankscore;
        }
    }
}
