package org.mulinlab.varnote.config.param;

import org.mulinlab.varnote.filters.mendelian.MendelianInheritanceFilter;
import org.mulinlab.varnote.filters.query.VariantFilter;
import org.mulinlab.varnote.filters.query.gt.GenotypeFilter;
import java.util.ArrayList;
import java.util.List;

public final class FilterParam {
    private List<VariantFilter> variantFilters;
    private List<GenotypeFilter> genotypeFilters;
    private MendelianInheritanceFilter miFilter;

    public FilterParam() {
    }

    public FilterParam(List<VariantFilter> variantFilters, List<GenotypeFilter> genotypeFilters, MendelianInheritanceFilter miFilter) {
        this.variantFilters = variantFilters;
        this.genotypeFilters = genotypeFilters;
        this.miFilter = miFilter;
    }

    public void addVariantFilters(final VariantFilter variantFilter) {
        if(variantFilters == null) {
            variantFilters = new ArrayList<>();
        }
        variantFilters.add(variantFilter);
    }

    public void addGenotypeFilters(final GenotypeFilter genotypeFilter) {
        if(genotypeFilters == null) {
            genotypeFilters = new ArrayList<>();
        }
        genotypeFilters.add(genotypeFilter);
    }

    public void printLog() {
        if(variantFilters != null)
            for (VariantFilter filter:variantFilters) {
                filter.printLog();
            }

        if(genotypeFilters != null)
            for (GenotypeFilter filter:genotypeFilters) {
                filter.printLog();
            }

        if(miFilter != null) miFilter.printLog();
    }

    public List<VariantFilter> getVariantFilters() {
        return variantFilters;
    }

    public List<GenotypeFilter> getGenotypeFilters() {
        return genotypeFilters;
    }

    public MendelianInheritanceFilter getMiFilter() {
        return miFilter;
    }

    public void setVariantFilters(List<VariantFilter> variantFilters) {
        this.variantFilters = variantFilters;
    }

    public void setGenotypeFilters(List<GenotypeFilter> genotypeFilters) {
        this.genotypeFilters = genotypeFilters;
    }

    public void setMiFilter(MendelianInheritanceFilter miFilter) {
        this.miFilter = miFilter;
    }

    public boolean isNotNull() {
        return variantFilters != null || genotypeFilters != null ||  miFilter != null;
    }
}
