package org.mulinlab.varnote.filters.mendelian;


import com.google.common.collect.ImmutableSet;
import de.charite.compbio.jannovar.pedigree.Disease;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import de.charite.compbio.jannovar.pedigree.Person;
import de.charite.compbio.jannovar.pedigree.Sex;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import org.mulinlab.varnote.utils.enumset.ChromosomeType;
import org.mulinlab.varnote.utils.enumset.ModeOfInheritance;

public class MendelianInheritanceXRFilter extends MendelianInheritanceFilter {

    public MendelianInheritanceXRFilter(final Pedigree pedigree) {
        super(ModeOfInheritance.X_RECESSIVE, pedigree);
    }

    @Override
    protected boolean isChromTypeMatch(final ChromosomeType chr) {
        return chr == ChromosomeType.X_CHROMOSOMAL;
    }

    protected boolean isCompatibleSingleton(final GenotypesContext genotypes) {
        if (genotypes.size() == 0)
            return false;
        if (genotypes.get(0).isHomVar())
            return true;
        else if (pedigree.getMembers().get(0).getSex() != Sex.FEMALE && genotypes.get(0).isHet())
            return true;
        else
            return false;
    }

    protected boolean isCompatibleFamily(final GenotypesContext genotypes) {
        return (affectedsAreCompatible(genotypes) && parentsAreCompatible(genotypes) && unaffectedsAreCompatible(genotypes));
    }

    protected boolean affectedsAreCompatible(final GenotypesContext genotypes) {
        int numVar = 0;

        for (Person p : pedigree.getMembers()) {
            final String name = p.getName();
            final Genotype gt = genotypes.get(name);
            if (p.getDisease() == Disease.AFFECTED) {
                if (gt.isHomRef()) {
                    return false;
                } else if (p.getSex() == Sex.FEMALE && gt.isHet()) {
                    return false;
                } else if (gt.isHomVar() || (p.getSex() != Sex.FEMALE && gt.isHet())) {
                    numVar += 1;
                }
            }
        }

        return (numVar > 0);
    }

    private boolean parentsAreCompatible(final GenotypesContext genotypes) {
        final ImmutableSet<String> femaleParentNames = getAffectedFemaleParentNames();

        for (Person p : pedigree.getMembers()) {
            final Genotype gt = genotypes.get(p.getName());
            if (femaleParentNames.contains(p.getName())) {
                if (p.getSex() == Sex.MALE && p.getDisease() == Disease.UNAFFECTED) {
                    return false;
                }
                if (p.getSex() == Sex.FEMALE && (gt.isHomVar() || gt.isHomRef())) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean unaffectedsAreCompatible(final GenotypesContext genotypes) {
        final ImmutableSet<String> unaffectedNames = getUnaffectedNames();

        for (Person p : pedigree.getMembers()) {
            if (unaffectedNames.contains(p.getName())) {
                final Genotype gt = genotypes.get(p.getName());

                if (p.isMale() && (gt.isHet() || gt.isHomVar()))
                    return false;
                else if (gt.isHomVar())
                    return false;
            }
        }

        return true;
    }

    public ImmutableSet<String> getUnaffectedNames() {
        ImmutableSet.Builder<String> resultNames = new ImmutableSet.Builder<String>();
        for (Person member : pedigree.getMembers())
            if (member.getDisease() == Disease.UNAFFECTED)
                resultNames.add(member.getName());
        return resultNames.build();
    }

    public ImmutableSet<String> getAffectedFemaleParentNames() {
        ImmutableSet.Builder<String> parentNames = new ImmutableSet.Builder<String>();
        for (Person member : pedigree.getMembers()) {
            if (member.isAffected() && member.isFemale()) {
                if (member.getFather() != null)
                    parentNames.add(member.getFather().getName());
                if (member.getMother() != null)
                    parentNames.add(member.getMother().getName());
            }
        }
        return parentNames.build();
    }
}
