package org.mulinlab.varnote.filter;

import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mulinlab.varnote.filters.iterator.LocFilterIterator;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.filters.query.line.LineFilter;
import org.mulinlab.varnote.filters.query.line.VCFHeaderLineFilter;
import org.mulinlab.varnote.operations.decode.VCFLocCodec;
import org.mulinlab.varnote.operations.readers.itf.GZIPReader;
import org.mulinlab.varnote.operations.readers.itf.LongLineReader;
import org.mulinlab.varnote.utils.node.LocFeature;
import org.mulinlab.varnote.utils.LoggingUtils;

import java.util.ArrayList;
import java.util.List;

public class LocFilterIteratorTest {
    final Logger logger = LoggingUtils.logger;

    @Test
    public void testGZIPReader() throws Exception {
        long t1 = System.currentTimeMillis();

        String path = "src/test/resources/q3.sorted.vcf.gz";

        List<LineFilter> lineFilters = new ArrayList<>();
        lineFilters.add(new VCFHeaderLineFilter());

        LocFilterIterator iterator = new LocFilterIterator(new NoFilterIterator(new GZIPReader(path)), lineFilters, new VCFLocCodec());

        int i = 0;
        while(iterator.hasNext()) {

            LocFeature loc = iterator.next();
            if(loc != null) {
                i++;
            }
        }

        iterator.close();

        long t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %d total: %d\n", (t2 - t1), i));
    }


    @Test
    public void testTXT() throws Exception {
        long t1 = System.currentTimeMillis();

        String path = "src/test/resources/test5.vcf";

        List<LineFilter> lineFilters = new ArrayList<>();
        lineFilters.add(new VCFHeaderLineFilter());

        LocFilterIterator iterator = new LocFilterIterator(new NoFilterIterator(new LongLineReader(path)), lineFilters, new VCFLocCodec());

        int i = 0;
        while(iterator.hasNext()) {
            LocFeature loc = iterator.next();
            if(loc != null) {
                i++;
            }
        }

        iterator.close();

        long t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %d total: %d\n", (t2 - t1), i));
    }


}
