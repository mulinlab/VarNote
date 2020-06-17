package org.mulinlab.varnote.utils.enumset;

import de.charite.compbio.jannovar.pedigree.Pedigree;
import org.mulinlab.varnote.filters.mendelian.*;
import org.mulinlab.varnote.utils.pedigree.PedigreeConverter;

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

    public static MendelianInheritanceFilter getMendelianInheritanceFilter(ModeOfInheritance modeOfInheritance, Pedigree pedigree) {
        switch (modeOfInheritance) {
            case AUTOSOMAL_DOMINANT:
                return new MendelianInheritanceADFilter(pedigree);
            case AUTOSOMAL_RECESSIVE:
                return new MendelianInheritanceARFilter(pedigree);
            case X_RECESSIVE:
                return new MendelianInheritanceXDFilter(pedigree);
            case X_DOMINANT:
                return new MendelianInheritanceXRFilter(pedigree);
            default:
                return null;
        }
    }
}
