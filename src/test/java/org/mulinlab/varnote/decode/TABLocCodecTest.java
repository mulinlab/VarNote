package org.mulinlab.varnote.decode;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.mulinlab.varnote.operations.decode.TABLocCodec;
import org.mulinlab.varnote.utils.node.LocFeature;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.format.Format;

public class TABLocCodecTest {
    final Logger logger = LoggingUtils.logger;


    @Test
    public void testZeroBased() throws Exception {
        Format format = Format.newTAB();
        format.sequenceColumn = 1;
        format.startPositionColumn = 2;
        format.endPositionColumn = 2;
        format.refPositionColumn = 4;
        format.altPositionColumn = 5;

        format.setZeroBased();

        TABLocCodec decode = new TABLocCodec(format);
        LocFeature feature = decode.decode("1	10177	.	A	A	22041.2");
        Assert.assertEquals(feature.chr, "1");
        Assert.assertEquals(feature.beg, 10177);
        Assert.assertEquals(feature.end, 10178);


        feature = decode.decode("1	10177	.	A	ACC");
        Assert.assertEquals(feature.beg, 10177);
        Assert.assertEquals(feature.end, 10178);

        feature = decode.decode("1	10177	.	ACC	A");
        Assert.assertEquals(feature.beg, 10177);
        Assert.assertEquals(feature.end, 10180);


        format.endPositionColumn = 3;
        format.setZeroBased();
        decode = new TABLocCodec(format);

        feature = decode.decode("1	10177	10178	A	ACC");
        Assert.assertEquals(feature.beg, 10177);
        Assert.assertEquals(feature.end, 10178);

        feature = decode.decode("1	10177	10179	ACC	A");
        Assert.assertEquals(feature.beg, 10177);
        Assert.assertEquals(feature.end, 10179);
    }

    @Test
    public void testOneBased() throws Exception {
        Format format = Format.newTAB();
        format.sequenceColumn = 1;
        format.startPositionColumn = 2;
        format.endPositionColumn = 2;
        format.refPositionColumn = 4;
        format.altPositionColumn = 5;


        TABLocCodec decode = new TABLocCodec(format);
        LocFeature feature = decode.decode("1	10177	.	A	ACC");
        Assert.assertEquals(feature.chr, "1");
        Assert.assertEquals(feature.beg, 10176);
        Assert.assertEquals(feature.end, 10177);

        feature = decode.decode("1	10177	.	ACC	A");
        Assert.assertEquals(feature.beg, 10176);
        Assert.assertEquals(feature.end, 10179);


        format.endPositionColumn = 3;
        decode = new TABLocCodec(format);

        feature = decode.decode("1	10177	10180	A	ACC");
        Assert.assertEquals(feature.beg, 10176);
        Assert.assertEquals(feature.end, 10180);

        feature = decode.decode("1	10177	10178	A	ACC");
        Assert.assertEquals(feature.beg, 10176);
        Assert.assertEquals(feature.end, 10178);
    }
}
