package org.mulinlab.varnote.utils.enumset;


public enum CellMark {
    DNase, H3K27ac, H3K27me3, H3K36me3, H3K4me1, H3K4me2, H3K4me3, H3K79me2, H3K9me3;

    public static CellMark toCellMark(String cellmark) {
        for (CellMark c : CellMark.values()) {
            if(cellmark.equals(c.toString())) {
                return c;
            }
        }
        return null;
    }
}
