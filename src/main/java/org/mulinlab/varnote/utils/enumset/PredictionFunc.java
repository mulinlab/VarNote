package org.mulinlab.varnote.utils.enumset;

public enum PredictionFunc {
    CADD("CADD", 5),	Eigen("Eigen", 13),	FATHMM_MKL("FATHMM-MKL", 9),	FATHMM_XF("FATHMM-XF", 27),
    GenoCanyon("GenoCanyon", 17),	LINSIGHT("LINSIGHT", 23),	ReMM("ReMM", 21);

    private final String label;
    private final int index;

    PredictionFunc(String label, int index) {
        this.label = label;
        this.index = index;
    }

    public String getLabel() {
        return label;
    }

    public int getIndex() {
        return index;
    }
}
