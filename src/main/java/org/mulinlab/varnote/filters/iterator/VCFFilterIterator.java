package org.mulinlab.varnote.filters.iterator;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.GenotypesContext;
import org.mulinlab.varnote.config.param.FilterParam;
import org.mulinlab.varnote.filters.query.gt.GenotypeFilter;
import org.mulinlab.varnote.filters.query.VariantFilter;
import org.mulinlab.varnote.filters.query.line.LineFilter;
import org.mulinlab.varnote.operations.decode.VCFLocCodec;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.util.List;

public final class VCFFilterIterator extends LineFilterIterator {

    private final FilterParam filterParam;
    private final VCFLocCodec decode;

    public VCFFilterIterator(final NoFilterIterator iterator, List<LineFilter> filters, final FilterParam filterParam, final VCFLocCodec decode) {
        super(iterator, filters);
        this.filterParam = filterParam;
        this.decode = decode;
    }

    public void close() {
        iterator.close();
    }

    @Override
    public LocFeature processLine(final String line) {
        super.processLine(line);
        if(!isFiltered) {
            final LocFeature ctx = decode.decode(line);

            if(filterParam != null) {
                if(filterParam.getRegionFilter() != null && filterParam.getRegionFilter().isFilterLine(ctx)) return null;

//                System.out.println(ctx);
                if(filterParam.getVariantFilters() != null) {
                    for (VariantFilter vaFilter: filterParam.getVariantFilters()) {
                        if(vaFilter.isFilterLine(ctx)) {
                            return null;
                        }
                    }
                }

                GenotypesContext gtx;
                if(filterParam.getGenotypeFilters() != null) {
                    gtx = GenotypesContext.create(ctx.variantContext.getGenotypes().size());
                    for (final Genotype gt : ctx.variantContext.getGenotypes()) {
                        boolean flag = true;
                        for (final GenotypeFilter filter : filterParam.getGenotypeFilters()) {
                            if(filter.isFilterLine(ctx, gt)) {
                                flag = false;
                            }
                        }

                        if(flag) {
                            gtx.add(gt);
                        } else {
                            gtx.add(GenotypeBuilder.createMissing(gt.getSampleName(), gt.getPloidy()));
                        }
                    }
                } else {
                    gtx = ctx.variantContext.getGenotypes();
                }

                if(filterParam.getMiFilter() != null && filterParam.getMiFilter().isFilterLine(ctx.chr, gtx)) return null;
            }

            ctx.origStr = line;
            return ctx;
        } else {
            return null;
        }
    }
}
