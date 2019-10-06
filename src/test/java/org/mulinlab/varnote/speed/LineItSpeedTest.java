package org.mulinlab.varnote.speed;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mulinlab.varnote.utils.*;
import org.mulinlab.varnote.utils.queryreader.LineIteratorImpl;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class LineItSpeedTest {

    @Test
    public void testIndex() throws IOException {
        final Logger logger = LoggingUtils.logger;

        long t1 = System.currentTimeMillis();

        String file = "/Users/hdd/Downloads/test_data/database1.sorted.bed.gz";
        LineIteratorImpl reader = new LineIteratorImpl(file, VannoUtils.FileType.TXT);

        String s;
        int i = 0;
        while((s = reader.advance()) != null) {

            if(!s.startsWith("#")) {
                i++;
            }
        }
        System.out.println("i=" + i);
        reader.close();


        long t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %d\n", (t2 - t1)));






        t1 = System.currentTimeMillis();
        file = "/Users/hdd/Downloads/test_data/database1.sorted.bed.gz";
        reader = new LineIteratorImpl(file, VannoUtils.FileType.GZ);

        i = 0;
        while((s = reader.advance()) != null) {

            if(!s.startsWith("#")) {
                i++;
            }
        }
        System.out.println("i=" + i);
        reader.close();

        t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %d\n", (t2 - t1)));




        t1 = System.currentTimeMillis();
        file = "/Users/hdd/Downloads/test_data/database1.sorted.bed.gz";
        reader = new LineIteratorImpl(file, VannoUtils.FileType.BGZ);

        i = 0;
        while((s = reader.advance()) != null) {

            if(!s.startsWith("#")) {
                i++;
            }
        }
        System.out.println("i=" + i);
        reader.close();

        t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %d\n", (t2 - t1)));


        t1 = System.currentTimeMillis();
        List<String> lines = IOUtils.readLines(new FileInputStream(file));
        t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %d\n", (t2 - t1)));
    }
}
