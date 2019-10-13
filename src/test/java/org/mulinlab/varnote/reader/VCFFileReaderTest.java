package org.mulinlab.varnote.reader;

import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mulinlab.varnote.filters.iterator.LineFilterIterator;
import org.mulinlab.varnote.filters.query.LocFeatureFilter;
import org.mulinlab.varnote.filters.query.VCFContextFilter;
import org.mulinlab.varnote.operations.readers.query.VCFFileReader;
import org.mulinlab.varnote.utils.node.LocFeature;
import org.mulinlab.varnote.utils.LoggingUtils;

import java.util.ArrayList;
import java.util.List;

public class VCFFileReaderTest {

    final Logger logger = LoggingUtils.logger;

    @Test
    public void test() throws Exception {
        long t1 = System.currentTimeMillis();

        String path = "src/test/resources/test5.vcf";

        List<LocFeatureFilter> locFilters = new ArrayList<>();
        locFilters.add(new VCFContextFilter());

        VCFFileReader vcfFileReader = new VCFFileReader(path);
//        vcfFileReader.setLocFilters(locFilters);

        LineFilterIterator iterator = vcfFileReader.getFilterIterator();

        int i = 0;
        while(iterator.hasNext()) {

            LocFeature loc = iterator.next();
            if(loc != null) {
//                System.out.println(loc.toString());
            }

            i++;
        }

        iterator.close();

        long t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %d total: %d\n", (t2 - t1), i));
    }
}
