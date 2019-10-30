package org.mulinlab.varnote.decode;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.mulinlab.varnote.operations.decode.BEDLocCodec;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.node.LocFeature;

public class BEDLocCodecTest {
    final Logger logger = LoggingUtils.logger;


    @Test
    public void test() throws Exception {

        BEDLocCodec decode = new BEDLocCodec(false);

        LocFeature feature = decode.decode("chr22	2000	6000	cloneB	900	-	2000    6000    0   2   433,399,0,3601");
        Assert.assertEquals(feature.chr, "chr22");
        Assert.assertEquals(feature.beg, 2000);
        Assert.assertEquals(feature.end, 6000);


        feature = decode.decode("chr22	1000	5000	cloneA	960	+	1000	5000	0	2	567,488,0,3512");
        Assert.assertEquals(feature.beg, 1000);
        Assert.assertEquals(feature.end, 5000);

    }
}
