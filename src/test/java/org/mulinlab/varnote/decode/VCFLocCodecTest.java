package org.mulinlab.varnote.decode;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.mulinlab.varnote.operations.decode.VCFLocCodec;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.node.LocFeature;

public class VCFLocCodecTest {
    final Logger logger = LoggingUtils.logger;


    @Test
    public void test() throws Exception {
        VCFLocCodec decode = new VCFLocCodec();
        LocFeature feature = decode.decode("1	10177	.	A	A	22041.2");
        Assert.assertEquals(feature.chr, "1");
        Assert.assertEquals(feature.beg, 10176);
        Assert.assertEquals(feature.end, 10177);


        feature = decode.decode("1	10177	.	ACC	A	22041.2	10X_RESCUED_MOLECULE_HIGH_DIVERSITY	.	GT	0/1");
        Assert.assertEquals(feature.beg, 10176);
        Assert.assertEquals(feature.end, 10179);

        feature = decode.decode("1	10177	.	A	ACC	22041.2	10X_RESCUED_MOLECULE_HIGH_DIVERSITY	.	GT	0/1");
        Assert.assertEquals(feature.beg, 10176);
        Assert.assertEquals(feature.end, 10177);


        feature = decode.decode("1	10177	.	GGCGCG	TCCGCA	701.53	.	AB=0;ABP=0;AC=6;");
        Assert.assertEquals(feature.beg, 10176);
        Assert.assertEquals(feature.end, 10182);

        feature = decode.decode("1	10177	rs771917038	A	ACCGTCAGCT	701.53	.	AB=0;ABP=0");
        Assert.assertEquals(feature.beg, 10176);
        Assert.assertEquals(feature.end, 10177);

        feature = decode.decode("1	10177	rs781085493	GGGGGGCGC	G	701.53	.	AB=0;ABP=0");
        Assert.assertEquals(feature.beg, 10176);
        Assert.assertEquals(feature.end, 10185);

        feature = decode.decode("1	10177	rs534090028	CGCA	TGCA,C	701.53	.	AB=1;ABP=1");
        Assert.assertEquals(feature.beg, 10176);
        Assert.assertEquals(feature.end, 10180);

        feature = decode.decode("1	10177	rs534090028	CGCA	TGCA,C	701.53	.	AB=1;ABP=1");
        Assert.assertEquals(feature.beg, 10176);
        Assert.assertEquals(feature.end, 10180);

//        feature = decode.decode("1	869465	1	N	<DEL>	1293.8	.	SVTYPE=DEL;POS=869465;SVLEN=-752;END=870217;STRANDS=+-:31;IMPRECISE;CIPOS=-10,157;CIEND=-84,10;CIPOS95=-3,31;CIEND95=-36,3;");
//        Assert.assertEquals(feature.beg, 869454);
//        Assert.assertEquals(feature.end, 870227);
//
//        feature = decode.decode("1	1157791	4345_1	N	N[4:76212291[	0.0	.	SVTYPE=BND;POS=1157791;STRANDS=+-:5;IMPRECISE;CIPOS=-8,8;CIEND=-9,6");
//        Assert.assertEquals(feature.beg, 1157782);
//        Assert.assertEquals(feature.end, 1157797);

    }
}
