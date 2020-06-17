package org.mulinlab.varnote.utils.node;

import htsjdk.tribble.Feature;
import htsjdk.variant.variantcontext.VariantContext;
import org.mulinlab.varnote.utils.enumset.VariantType;

public class LocFeature implements Feature {
    public int beg;
    public int end;

    public String chr;
    public String ref;
    public String alt;
    private String[] alts;

    public String origStr;
    public String bgzStr;

    public String[] parts;
    public VariantContext variantContext;

    public VariantType vt;

    public LocFeature() {
        clear();
        beg = 0;
        end = 0;
    }

    public LocFeature(int beg, int end, String chr) {
        clear();
        this.beg = beg;
        this.end = end;
        this.chr = chr;
    }

    public LocFeature(int beg, int end, String chr, String ref, String alt) {
        clear();
        this.beg = beg;
        this.end = end;
        this.chr = chr;
        this.ref = ref;
        this.alt = alt;
    }

    @Override
    public String toString() {
        return chr + "\t" + beg + "\t" + end;
    }

    public String posTabStr() {
        return chr.toLowerCase().replace("chr", "") + "_" + (beg+1) + "_" + ref + "_" + alt;
    }
    public String posStr() {
        return chr + ":" + (beg+1) + " " + ref + "|" + alt;
    }

    public void clear() {
        beg = -1;
        end = -1;
        chr = null;
        ref = null;
        alt = null;
        origStr = null;
        bgzStr = null;
        parts = null;
        alts = null;
        variantContext = null;
        vt = VariantType.NM;
    }

    public String[] getAlts() {
        if(alts == null) {
            alts = alt.split(",");
        }
        return alts;
    }

    public String getOrigStr() {
        return this.origStr;
    }

    public LocFeature clone()   {
        LocFeature cloned = new LocFeature(this.beg, this.end, this.chr);
        cloned.ref = this.ref;
        cloned.alt = this.alt;
        cloned.bgzStr = this.bgzStr;
        cloned.origStr = this.origStr;
        cloned.vt = this.vt;

        if(this.parts != null && this.parts.length > 0) {
            cloned.parts = new String[this.parts.length];
            for (int i = 0; i < this.parts.length; i++) {
                cloned.parts[i] = this.parts[i];
            }
        }
        if(this.variantContext != null) cloned.variantContext = this.variantContext;
        return cloned;
    }


    @Override
    public String getContig() {
        return chr;
    }

    @Override
    public int getStart() {
        return beg;
    }

    @Override
    public int getEnd() {
        return end;
    }
}
