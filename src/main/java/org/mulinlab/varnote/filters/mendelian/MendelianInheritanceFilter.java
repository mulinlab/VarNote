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

public abstract class MendelianInheritanceFilter implements VariantFilter<LocFeature> {

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

    @Override
    public boolean isFilterLine(final LocFeature loc) {
        if(isChromTypeMatch(VannoUtils.toChromosomeType(loc.chr))) {
            if (this.nMembers == 1 && isCompatibleSingleton(loc.variantContext.getGenotypes())) {
                return false;
            } else if(isCompatibleFamily(loc.variantContext.getGenotypes())) {
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

    @Override
    public void printLog() {
        logger.info(String.format("Filter out variants don't fit chromosome: %d", filterChrCount));
        logger.info(String.format("Filter out variants don't fit mendelian inheritance mode: %d", filterCompatibleCount));
    }

}
