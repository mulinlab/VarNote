package org.mulinlab.varnote.filter;

import htsjdk.variant.vcf.VCFCodec;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mulinlab.varnote.config.param.FilterParam;
import org.mulinlab.varnote.filters.iterator.VCFFilterIterator;
import org.mulinlab.varnote.filters.query.InfoFilter;
import org.mulinlab.varnote.filters.query.VariantFilter;
import org.mulinlab.varnote.filters.query.line.LineFilter;
import org.mulinlab.varnote.filters.query.line.VCFHeaderLineFilter;
import org.mulinlab.varnote.operations.decode.VCFLocCodec;
import org.mulinlab.varnote.operations.readers.itf.LongLineReader;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;

import java.util.ArrayList;
import java.util.List;

public class VCFFilterIteratorTest {
    final Logger logger = LoggingUtils.logger;

    @Test
    public void test() throws Exception {
        long t1 = System.currentTimeMillis();

        String path = "src/test/resources/test5.vcf";

        List<LineFilter> lineFilters = new ArrayList<>();
        lineFilters.add(new VCFHeaderLineFilter());

        FilterParam filterParam = new FilterParam();
        filterParam.addVariantFilters(new InfoFilter("AF", "AF < 0.001"));

        VCFCodec codec = new VCFCodec();
        codec.readActualHeader(new NoFilterIterator(new LongLineReader(path)));

        VCFFilterIterator iterator = new VCFFilterIterator(new NoFilterIterator(new LongLineReader(path)), lineFilters, filterParam,
                new VCFLocCodec(Format.VCF, true, codec));

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
