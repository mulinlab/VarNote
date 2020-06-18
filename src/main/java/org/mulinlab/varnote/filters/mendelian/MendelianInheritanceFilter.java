package org.mulinlab.varnote.filters.mendelian;

import de.charite.compbio.jannovar.pedigree.Pedigree;
import htsjdk.variant.variantcontext.GenotypesContext;
import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.filters.query.VariantFilter;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.ChromosomeType;
import org.mulinlab.varnote.utils.enumset.ModeOfInheritance;
import org.mulinlab.varnote.utils.node.LocFeature;

public abstract class MendelianInheritanceFilter {

    final static Logger logger = LoggingUtils.logger;

    protected int filterChrCount = 0;
    protected int filterCompatibleCount = 0;

    protected ModeOfInheritance mode;

    protected Pedigree pedigree;
    protected int nMembers;


    public MendelianInheritanceFilter(final ModeOfInheritance mode, final Pedigree pedigree) {
        this.mode = mode;
        this.pedigree = pedigree;
        this.nMembers = pedigree.getNMembers();
    }

    public boolean isFilterLine(final String chr, final GenotypesContext gtx) {
        if(isChromTypeMatch(VannoUtils.toChromosomeType(chr))) {
            if (this.nMembers == 1 && isCompatibleSingleton(gtx)) {
                return false;
            } else if(isCompatibleFamily(gtx)) {
                return false;
            }
//            System.out.println("not com " + loc.origStr);
            return addCompatibleCount();
        } else {
            return addChrCount();
        }
    }

    private boolean addChrCount() {
        filterChrCount++;
        return true;
    }

    private boolean addCompatibleCount() {
        filterCompatibleCount++;
        return true;
    }

    protected abstract boolean isChromTypeMatch(final ChromosomeType chr);
    protected abstract boolean isCompatibleSingleton(final GenotypesContext genotypes);
    protected abstract boolean isCompatibleFamily(final GenotypesContext genotypes);

    public String[] getLogs() {
        return new String[]{
                String.format("Filter out variants don't fit chromosome: %d", filterChrCount),
                String.format("Filter out variants don't fit mendelian inheritance mode: %d", filterCompatibleCount)
        };
    }

    @Override
    public Object clone() {
        return null;
    }

    public int getFilterChrCount() {
        return filterChrCount;
    }

    public int getFilterCompatibleCount() {
        return filterCompatibleCount;
    }

    public ModeOfInheritance getMode() {
        return mode;
    }

    public Pedigree getPedigree() {
        return pedigree;
    }
}
