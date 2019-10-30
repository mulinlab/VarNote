package org.mulinlab.varnote.filters.mendelian;

import de.charite.compbio.jannovar.pedigree.Disease;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import de.charite.compbio.jannovar.pedigree.Person;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import org.mulinlab.varnote.utils.enumset.ChromosomeType;
import org.mulinlab.varnote.utils.enumset.ModeOfInheritance;

public class MendelianInheritanceADFilter extends MendelianInheritanceFilter {

    public MendelianInheritanceADFilter(final Pedigree pedigree) {
        super(ModeOfInheritance.AUTOSOMAL_DOMINANT, pedigree);
    }

    @Override
    protected boolean isChromTypeMatch(final ChromosomeType chr) {
        return chr == ChromosomeType.AUTOSOMAL;
    }

    protected boolean isCompatibleSingleton(final GenotypesContext genotypes) {
        if (genotypes.size() == 0)
            return false; // no calls!
        return genotypes.get(0).isHet();
    }

    @Override
    protected boolean isCompatibleFamily(final GenotypesContext genotypes) {
        int numAffectedWithHet = 0;


        for (Person p : pedigree.getMembers()) {
            final Genotype gt = genotypes.get(p.getName());
            final Disease d = p.getDisease();

            if (d == Disease.AFFECTED) {
                if (gt.isHomRef() || gt.isHomVar())
                    return false;
                else if (gt.isHet())
                    numAffectedWithHet++;
            } else if (d == Disease.UNAFFECTED) {
                if (gt.isHet() || gt.isHomVar())
                    return false;
            }
        }

        return (numAffectedWithHet > 0);
    }
}
