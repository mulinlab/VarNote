package org.mulinlab.varnote.speed;

import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mulinlab.varnote.operations.readers.itf.BGZReader;
import org.mulinlab.varnote.operations.readers.itf.GZIPReader;
import org.mulinlab.varnote.operations.readers.itf.LongLineReader;
import org.mulinlab.varnote.utils.*;

public class LineItSpeedTest {
    final Logger logger = LoggingUtils.logger;

    @Test
    public void testTXT() throws Exception {
        long t1 = System.currentTimeMillis();
        String path = "src/test/resources/q1.sorted.bed";

        LongLineReader reader = new LongLineReader(path);
        String s;
        int i = 0;
        while((s = reader.readLine()) != null) {
            if(!s.startsWith("#")) {
                i++;
            }
        }
        reader.closeReader();

        long t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %d total: %d\n", (t2 - t1), i));
    }


    @Test
    public void testBGZ() throws Exception {
        long t1 = System.currentTimeMillis();
        String path = "src/test/resources/database2.sorted.tab.gz";

        BGZReader reader = new BGZReader(path);
        String s;
        int i = 0;
        while((s = reader.readLine()) != null) {
            if(!s.startsWith("#")) {
                i++;
            }
        }
        reader.closeReader();

        long t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %d total: %d\n", (t2 - t1), i));
    }


    @Test
    public void testGZ() throws Exception {
        long t1 = System.currentTimeMillis();
        String path = "src/test/resources/q2.sorted.tab.gz";

        GZIPReader reader = new GZIPReader(path);
        String s;
        int i = 0;
        while((s = reader.readLine()) != null) {
            if(!s.startsWith("#")) {
                i++;
            }
        }
        reader.closeReader();

        long t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %d total: %d\n", (t2 - t1), i));
    }

}
