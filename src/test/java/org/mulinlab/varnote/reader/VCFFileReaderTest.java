package org.mulinlab.varnote.reader;

import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mulinlab.varnote.filters.iterator.LineFilterIterator;
import org.mulinlab.varnote.filters.query.VariantFilter;
import org.mulinlab.varnote.operations.readers.query.TABFileReader;
import org.mulinlab.varnote.operations.readers.query.VCFFileReader;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;
import org.mulinlab.varnote.utils.LoggingUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VCFFileReaderTest {

    final Logger logger = LoggingUtils.logger;

    @Test
    public void test() throws Exception {
        long t1 = System.currentTimeMillis();

        String path = "src/test/resources/test5.vcf";

        List<VariantFilter> locFilters = new ArrayList<>();
//        locFilters.add(new VCFContextFilter());

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


    @Test
    public void samperTest() throws Exception {
        long t1 = System.currentTimeMillis();

        String path = "/Users/hdd/Desktop/vanno/random/hg19/EUR.gz";
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("/Users/hdd/Desktop/vanno/random/hg19/sampler.txt"))));

        List<VariantFilter> locFilters = new ArrayList<>();
        Format format = Format.newTAB();
        format.sequenceColumn = 1;
        format.startPositionColumn = 2;
        format.endPositionColumn = 2;
        format.refPositionColumn = 3;
        format.altPositionColumn = 4;

        TABFileReader vcfFileReader = new TABFileReader(path, format);

        LineFilterIterator iterator = vcfFileReader.getFilterIterator();

        Random random = new Random();
        String chr = "";
        int min = 1000, scope = 8000, totalLines = 5;

        int i = 0, j = 0, count = min + random.nextInt(scope), num = random.nextInt(totalLines), printNum = 0;
        boolean hasMax = false;
        System.out.println(count);
        while(iterator.hasNext()) {

            LocFeature loc = iterator.next();
            if(loc != null) {
                if(!chr.equals(loc.chr)) {
                    chr = loc.chr;
                    printNum = 0;
                    hasMax = false;
                }

                if(!hasMax) {
                    if(i == count) {
                        if(j == num) {
                            count =  min + random.nextInt(scope);
                            num = random.nextInt(totalLines);
                            i = 0;
                            j = 0;
                        } else {
                            j++;
                            out.write(loc.chr + "\t" + (loc.beg + 1)  + "\t" + loc.ref + "\t" + loc.alt + "\n");
                            printNum++;
                            if(printNum == 10000) {
                                hasMax = true;
                            }
                        }
                    } else {
                        i++;
                    }
                }


            }
        }

        iterator.close();
        long t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %d total: %d\n", (t2 - t1), i));
    }
}


