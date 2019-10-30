package org.mulinlab.varnote.utils.enumset;

public enum ModeOfInheritance {
    AUTOSOMAL_DOMINANT,
    AUTOSOMAL_RECESSIVE,

    X_RECESSIVE,
    X_DOMINANT,
    ANY;

    public boolean isRecessive() {
        switch (this) {
            case AUTOSOMAL_RECESSIVE:
            case X_RECESSIVE:
                return true;
            default:
                return false;
        }
    }

    public boolean isDominant() {
        switch (this) {
            case AUTOSOMAL_DOMINANT:
            case X_DOMINANT:
                return true;
            default:
                return false;
        }
    }
}
