package org.mulinlab.varnote.filters;

import org.mulinlab.varnote.filters.query.VariantFilter;
import org.mulinlab.varnote.utils.node.LocFeature;

import java.util.List;

public class RegionFilter implements VariantFilter<LocFeature>  {
    private List<LocFeature> regions;
    private boolean isExclude = true;
    private boolean isGene;
    private int count;

    public RegionFilter(List<LocFeature> regions, boolean isExclude, boolean isGene) {
        count = 0;
        this.regions = regions;
        this.isExclude = isExclude;
        this.isGene = isGene;
    }

    @Override
    public boolean isFilterLine(LocFeature loc) {
        if(isExclude) {
            for (LocFeature region:regions) {
                if(isOverlap(region, loc)) {
                    count++;
                    return true;
                }
            }

            return false;
        } else {
            for (LocFeature region:regions) {
                if(isOverlap(region, loc)) {
                    return false;
                }
            }
            count++;
            return true;
        }
    }

    public boolean isOverlap(LocFeature region, LocFeature loc) {
        if(!region.chr.equals(loc.chr)) {
            return false;
        } else {
            if((loc.beg + 1) > region.end || loc.end < (region.beg + 1)) {
                return false;
            } else {
                return true;
            }
        }
    }

    @Override
    public String[] getLogs() {
        return new String[]{
                String.format("Filter out %d variants %s %s", count, isExclude ? "in" : "out of", isGene ? "genes" : "regions")
        };
    }

    public int getCount() {
        return count;
    }

    @Override
    public Object clone() {
        return new RegionFilter(regions, isExclude, isGene);
    }

    @Override
    public String getName() {
        return isGene ? "genes" : "regions";
    }
}
