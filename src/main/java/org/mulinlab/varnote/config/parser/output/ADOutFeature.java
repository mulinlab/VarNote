package org.mulinlab.varnote.config.parser.output;


import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.mulinlab.varnote.utils.JannovarUtils;
import org.mulinlab.varnote.utils.jannovar.VariantAnnotation;
import org.mulinlab.varnote.utils.node.LocFeature;

public abstract class ADOutFeature {
    protected boolean isREMM = false;

    protected LocFeature queryLoc;
    protected boolean hasVariantEffect = false;

    protected VariantEffect variantEffect;
    protected String geneSymbol;
    protected String geneId;

    protected String rsid = "-";

    public ADOutFeature(LocFeature queryLoc) {
        this.queryLoc = queryLoc;
    }

    public void setVariantEffect(JannovarUtils jannovarUtils) {
        if(jannovarUtils != null) {
            try {
                VariantAnnotation annotation = jannovarUtils.annotate(queryLoc);

                if(annotation != null) {
                    hasVariantEffect = true;
                    variantEffect = annotation.getVariantEffect();
                    geneId = annotation.getGeneId();
                    geneSymbol = annotation.getGeneSymbol();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getRsid() {
        return rsid;
    }

    public void setRsid(String rsid) {
        this.rsid = rsid;
    }
}
