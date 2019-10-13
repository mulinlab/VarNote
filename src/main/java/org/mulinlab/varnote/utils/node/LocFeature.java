package org.mulinlab.varnote.utils.node;

import htsjdk.tribble.Feature;

public class LocFeature implements Feature {
    public int beg;
    public int end;

    public String chr;
    public String ref;
    public String alt;

    public String origStr;
    public String bgzStr;

    public LocFeature() {
        bgzStr = null;
        beg = 0;
        end = 0;
        chr = null;
    }

    public LocFeature(int beg, int end, String chr) {
        this.beg = beg;
        this.end = end;
        this.chr = chr;
    }

    @Override
    public String toString() {
        return chr + "\t" + beg + "\t" + end;
    }

    public void clear() {
        beg = -1;
        end = -1;
        chr = null;
        ref = null;
        alt = null;
        origStr = null;
        bgzStr = null;
    }

    public String getOrigStr() {
        return this.origStr;
    }

    public LocFeature clone()   {
        LocFeature cloned = new LocFeature(this.beg, this.end, null);
        cloned.bgzStr = this.bgzStr;
        cloned.origStr = this.origStr;
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
