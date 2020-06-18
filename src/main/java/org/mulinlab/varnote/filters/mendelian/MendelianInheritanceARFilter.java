package org.mulinlab.varnote.filters.mendelian;

import com.google.common.collect.ImmutableSet;
import de.charite.compbio.jannovar.mendel.GenotypeCalls;
import de.charite.compbio.jannovar.pedigree.Disease;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import de.charite.compbio.jannovar.pedigree.Person;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import org.mulinlab.varnote.utils.enumset.ChromosomeType;
import org.mulinlab.varnote.utils.enumset.ModeOfInheritance;

public class MendelianInheritanceARFilter extends MendelianInheritanceFilter {

    public MendelianInheritanceARFilter(final Pedigree pedigree) {
        super(ModeOfInheritance.AUTOSOMAL_RECESSIVE, pedigree);
    }

    @Override
    protected boolean isChromTypeMatch(final ChromosomeType chr) {
        return chr == ChromosomeType.AUTOSOMAL;
    }

    protected boolean isCompatibleSingleton(final GenotypesContext genotypes) {
        if (genotypes.size() == 0)
            return false;
        return genotypes.get(0).isHomVar();
    }

    protected boolean isCompatibleFamily(final GenotypesContext genotypes) {
        return (affectedsAreCompatible(genotypes) && unaffectedParentsOfAffectedAreNotHomozygous(genotypes)
                && unaffectedsAreNotHomozygousAlt(genotypes));
    }

    private boolean affectedsAreCompatible(final GenotypesContext genotypes) {
        int numHomozygousAlt = 0;

        for (Pedigree.IndexedPerson entry : pedigree.getNameToMember().values()) {
            if (entry.getPerson().getDisease() == Disease.AFFECTED) {
                final Genotype gt = genotypes.get(entry.getPerson().getName());
                if (gt.isHomRef() || gt.isHet())
                    return false;
                else if (gt.isHomVar())
                    numHomozygousAlt += 1;
            }
        }

        return (numHomozygousAlt > 0);
    }

    private boolean unaffectedParentsOfAffectedAreNotHomozygous(final GenotypesContext genotypes) {
        for (String name : getUnaffectedParentNamesOfAffecteds()) {
            final Genotype gt = genotypes.get(name);
            if (gt.isHomVar() || gt.isHomRef())
                return false;
        }
        return true;
    }

    private boolean unaffectedsAreNotHomozygousAlt(final GenotypesContext genotypes) {
        for (Pedigree.IndexedPerson entry : pedigree.getNameToMember().values())
            if (entry.getPerson().getDisease() == Disease.UNAFFECTED
                    && genotypes.get(entry.getPerson().getName()).isHomVar())
                return false;
        return true;
    }

    private ImmutableSet<String> getUnaffectedParentNamesOfAffecteds() {
        ImmutableSet.Builder<String> builder = new ImmutableSet.Builder<String>();

        for (Person person : pedigree.getMembers())
            if (person.getDisease() == Disease.AFFECTED) {
                if (person.getFather() != null && person.getFather().getDisease() == Disease.UNAFFECTED)
                    builder.add(person.getFather().getName());
                if (person.getMother() != null && person.getMother().getDisease() == Disease.UNAFFECTED)
                    builder.add(person.getMother().getName());
            }

        return builder.build();
    }

    @Override
    public Object clone() {
        return new MendelianInheritanceARFilter(pedigree);
    }

}
