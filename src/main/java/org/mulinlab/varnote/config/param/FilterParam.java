package org.mulinlab.varnote.config.param;

import org.mulinlab.varnote.filters.RegionFilter;
import org.mulinlab.varnote.filters.mendelian.MendelianInheritanceFilter;
import org.mulinlab.varnote.filters.query.VariantFilter;
import org.mulinlab.varnote.filters.query.gt.GenotypeFilter;
import java.util.ArrayList;
import java.util.List;

public final class FilterParam {
    private List<VariantFilter> variantFilters;
    private List<GenotypeFilter> genotypeFilters;
    private MendelianInheritanceFilter miFilter;
    private RegionFilter regionFilter;

    public FilterParam() {
    }

    @Override
    public FilterParam clone() {
        List<VariantFilter> variantFiltersClone = null;
        if(variantFilters != null) {
            variantFiltersClone = new ArrayList<>();
            for (VariantFilter filter:variantFilters) {
                variantFiltersClone.add((VariantFilter)filter.clone());
            }
        }

        List<GenotypeFilter> genotypeFiltersClone = null;
        if(genotypeFilters != null) {
            genotypeFiltersClone = new ArrayList<>();
            for (GenotypeFilter filter:genotypeFilters) {
                genotypeFiltersClone.add((GenotypeFilter)filter.clone());
            }
        }

        MendelianInheritanceFilter miFilterClone = null;
        if(miFilter != null) {
            miFilterClone = (MendelianInheritanceFilter)miFilter.clone();
        }

        RegionFilter regionFilterClone = null;
        if(regionFilter != null) {
            regionFilterClone = (RegionFilter)regionFilter.clone();
        }
        return new FilterParam(variantFiltersClone, genotypeFiltersClone, miFilterClone, regionFilterClone);
    }

    public FilterParam(List<VariantFilter> variantFilters, List<GenotypeFilter> genotypeFilters, MendelianInheritanceFilter miFilter, RegionFilter regionFilter) {
        this.variantFilters = variantFilters;
        this.genotypeFilters = genotypeFilters;
        this.miFilter = miFilter;
        this.regionFilter = regionFilter;
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

    public RegionFilter getRegionFilter() {
        return regionFilter;
    }

    public void setRegionFilter(RegionFilter regionFilter) {
        this.regionFilter = regionFilter;
    }
}
