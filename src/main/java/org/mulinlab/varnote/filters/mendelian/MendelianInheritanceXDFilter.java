package org.mulinlab.varnote.filters.mendelian;


import de.charite.compbio.jannovar.pedigree.Disease;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import de.charite.compbio.jannovar.pedigree.Person;
import de.charite.compbio.jannovar.pedigree.Sex;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import org.mulinlab.varnote.utils.enumset.ChromosomeType;
import org.mulinlab.varnote.utils.enumset.ModeOfInheritance;

public class MendelianInheritanceXDFilter extends MendelianInheritanceFilter {

    public MendelianInheritanceXDFilter(final Pedigree pedigree) {
        super(ModeOfInheritance.X_DOMINANT, pedigree);
    }

    @Override
    protected boolean isChromTypeMatch(final ChromosomeType chr) {
        return chr == ChromosomeType.X_CHROMOSOMAL;
    }

    protected boolean isCompatibleSingleton(final GenotypesContext genotypes) {
        if (genotypes.size() == 0)
            return false;
        final Genotype gt = genotypes.get(0);
        if (pedigree.getMembers().get(0).getSex() == Sex.FEMALE) {
            return genotypes.get(0).isHet();
        } else {
            return (gt.isHet() || gt.isHomVar());
        }
    }

    protected boolean isCompatibleFamily(final GenotypesContext genotypes) {
        int numAffectedWithVar = 0;

        for (Person p : pedigree.getMembers()) {
            final Sex sex = p.getSex();
            final Genotype gt = genotypes.get(p.getName());
            final Disease d = p.getDisease();

            if (d == Disease.AFFECTED) {
                if (gt.isHomRef() || (sex == Sex.FEMALE && gt.isHomVar())) {
                    return false;
                } else if (sex == Sex.FEMALE && gt.isHet()) {
                    numAffectedWithVar++;
                } else if (sex != Sex.FEMALE && (gt.isHet() || gt.isHomVar())) {
                    numAffectedWithVar++;
                }
            } else if (d == Disease.UNAFFECTED) {
                if (gt.isHet() || gt.isHomVar())
                    return false;
            }
        }
        return (numAffectedWithVar > 0);
    }

    @Override
    public Object clone() {
        return new MendelianInheritanceXDFilter(pedigree);
    }
}
