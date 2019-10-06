package org.mulinlab.varnote.speed;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.queryreader.ThreadLineReader;
import org.mulinlab.varnote.utils.queryreader.reader.BufferReader;
import org.mulinlab.varnote.utils.queryreader.reader.GZipReader;
import org.mulinlab.varnote.utils.stream.GZInputStream;

import java.io.File;

public final class GZReaderSpeedTest {

    @Test
    public void testIndex() throws Exception {
        final Logger logger = LoggingUtils.logger;
        long t1 = System.currentTimeMillis();

        final String file = "/Users/hdd/Downloads/test_data/database4.sorted.vcf.gz";
        GZInputStream in = new GZInputStream(file, 1);
        ThreadLineReader reader = new ThreadLineReader(new GZipReader(in.getReader(0)), Format.newVCF(), 1);

        String s;
        int i = 0;
        while((s = reader.readLine()) != null) {
            i++;
            if(i % 10000 == 0) System.out.println(i);
        }
        System.out.println("i=" + i);
        reader.close();


        long t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %ds\n", (t2 - t1)/1000));
    }
}
