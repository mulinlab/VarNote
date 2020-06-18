package org.mulinlab.varnote.filter;

import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mulinlab.varnote.operations.readers.itf.LongLineReader;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.utils.LoggingUtils;

public class NoFilterIteratorTest {
    final Logger logger = LoggingUtils.logger;

    @Test
    public void test() throws Exception {
        long t1 = System.currentTimeMillis();

        String path = "src/test/resources/test5.vcf";

        NoFilterIterator iterator = new NoFilterIterator(new LongLineReader(path));

        int i = 0;
        while(iterator.hasNext()) {
//            System.out.println(iterator.peek());
            iterator.next();
            i++;
        }

        iterator.close();

        long t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %d total: %d\n", (t2 - t1), i));
    }
}
