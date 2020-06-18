package org.mulinlab.varnote.filter;

import htsjdk.variant.vcf.VCFCodec;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mulinlab.varnote.config.anno.databse.VCFParser;
import org.mulinlab.varnote.config.param.FilterParam;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.filters.iterator.VCFFilterIterator;
import org.mulinlab.varnote.filters.mendelian.MendelianInheritanceADFilter;
import org.mulinlab.varnote.filters.query.VariantFilter;
import org.mulinlab.varnote.filters.query.gt.DepthFilter;
import org.mulinlab.varnote.filters.query.gt.GenotypeFilter;
import org.mulinlab.varnote.filters.query.gt.GenotypeQualityFilter;
import org.mulinlab.varnote.filters.query.line.LineFilter;
import org.mulinlab.varnote.filters.query.line.VCFHeaderLineFilter;
import org.mulinlab.varnote.operations.decode.VCFLocCodec;
import org.mulinlab.varnote.operations.readers.itf.LongLineReader;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.enumset.ModeOfInheritance;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;
import org.mulinlab.varnote.utils.pedigree.PedFiles;
import org.mulinlab.varnote.utils.pedigree.Pedigree;
import org.mulinlab.varnote.utils.pedigree.PedigreeConverter;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MendelianFilterIteratorTest {
    final Logger logger = LoggingUtils.logger;

    @Test
    public void test() throws Exception {
        long t1 = System.currentTimeMillis();

        String path = "FSGS.jointcall.new.vcf";
        Pedigree pedigree = PedFiles.readPedigree(new File("FSGS_0.ped").toPath());

        List<LineFilter> lineFilters = new ArrayList<>();
        lineFilters.add(new VCFHeaderLineFilter());


        FilterParam filterParam = new FilterParam();
        MendelianInheritanceADFilter mendelianInheritanceADFilter = new MendelianInheritanceADFilter(PedigreeConverter.convertToJannovarPedigree(pedigree));
        filterParam.setMiFilter(mendelianInheritanceADFilter);
        filterParam.addGenotypeFilters(new DepthFilter(4));
        filterParam.addGenotypeFilters(new GenotypeQualityFilter(10));


        VCFParser vcfParser = new VCFParser(path);

        VCFFilterIterator iterator = new VCFFilterIterator(new NoFilterIterator(new LongLineReader(path)), lineFilters, filterParam, new VCFLocCodec(Format.newVCF(), true, vcfParser));

        int i = 0;
        while(iterator.hasNext()) {
            LocFeature feature = iterator.next();
            if(feature != null) {
                i++;
            }
        }

        iterator.close();

        long t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %d total: %d\n", (t2 - t1), i));
    }
}
