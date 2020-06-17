package org.mulinlab.varnote.utils.enumset;

public enum RegFunc {
    REG("regbase", 1,  -1, -1), FitCons2("fitCons2", 0, 4, 3),
    GenoSkylinePlus("genoSkylinePlus", 0, 4, 3),
    FUNLDA("funlda", 0, 4, 3), GenoNet("genoNet", 0, -1, 4);

    private final String label;
    private final int type;
    private final int cellTypeIdx;
    private final int scoreIdx;

    RegFunc(final String label, final int type, final int cellTypeIdx, final int scoreIdx) {
        this.label = label;
        this.type = type;
        this.cellTypeIdx = cellTypeIdx;
        this.scoreIdx = scoreIdx;
    }

    public String getLabel() {
        return label;
    }

    public int getType() {
        return type;
    }

    public int getCellTypeIdx() {
        return cellTypeIdx;
    }

    public int getScoreIdx() {
        return scoreIdx;
    }
}
