package org.mulinlab.varnote.speed;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mulinlab.varnote.utils.LoggingUtils;
import java.io.File;

public final class VCFReaderSpeedTest {

    @Test
    public void testIndex() {
        final Logger logger = LoggingUtils.logger;
        long t1 = System.currentTimeMillis();

        final String file = "src/test/resources/test5.vcf";
        final VCFFileReader fileReader = new VCFFileReader(new File(file), false);
        final VCFHeader fileHeader = fileReader.getFileHeader();

        final CloseableIterator<VariantContext> iterator = fileReader.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            final VariantContext context = iterator.next();
             i++;
        }

        CloserUtil.close(iterator);
        CloserUtil.close(fileReader);

        long t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %d\n", (t2 - t1)));
    }
}
