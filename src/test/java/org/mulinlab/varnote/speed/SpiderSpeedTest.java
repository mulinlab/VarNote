package org.mulinlab.varnote.speed;

import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.operations.readers.itf.thread.SpiderReader;
import org.mulinlab.varnote.utils.stream.BZIP2InputStream;

public final class SpiderSpeedTest {

    @Test
    public void testIndex() throws Exception {
        final Logger logger = LoggingUtils.logger;
        long t1 = System.currentTimeMillis();

        final String file = "src/test/resources/database4.sorted.vcf";

        int thread = 8;

        BZIP2InputStream bz2_text = new BZIP2InputStream(file, thread);
        bz2_text.adjustPos();
        bz2_text.creatSpider();

//        BufferedWriter write = IOUtil.openFileForBufferedUtf8Writing(new File("/Users/hdd/Downloads/test_data/database4.vcf"));
        int i = 0;
        String s;


        for (int j = 0; j < thread ; j++) {
            SpiderReader reader = new SpiderReader(bz2_text.spider[j]);
            while((s = reader.readLine()) != null) {
                if(!s.startsWith("#")) {
//                    write.write(s);
//                    write.newLine();
                    i++;
                }
            }
            reader.closeReader();
        }


        System.out.println("i=" + i);

//        write.close();

        long t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %d\n", t2 - t1));
    }
}
