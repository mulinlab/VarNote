package org.mulinlab.varnote.speed;

import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.operations.readers.itf.thread.GZIPThreadReader;
import org.mulinlab.varnote.utils.stream.GZInputStream;

public final class GZReaderSpeedTest {

    @Test
    public void testIndex() throws Exception {
        final Logger logger = LoggingUtils.logger;
        long t1 = System.currentTimeMillis();

        final String file = "src/test/resources/q1.sorted.bed.gz";


        int thread = 4;
        GZInputStream in = new GZInputStream(file, thread);


//        BufferedWriter write = IOUtil.openFileForBufferedUtf8Writing(new File("/Users/hdd/Downloads/test_data/database4.vcf"));
        int i = 0;
        String s;


        for (int j = 0; j < thread ; j++) {
            GZIPThreadReader reader = new GZIPThreadReader(in.getReader(j));
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
