package org.mulinlab.varnote.decode;

import htsjdk.variant.vcf.VCFHeader;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.mulinlab.varnote.operations.decode.VCFLocCodec;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;

public class VCFLocCodecTest {
    final Logger logger = LoggingUtils.logger;


    @Test
    public void test() throws Exception {
        VCFLocCodec decode = new VCFLocCodec(Format.newVCF(), false, (VCFHeader) null);
        LocFeature feature = decode.decode("1	10177	.	C\tA\t22041.2\t10X_RESCUED_MOLECULE_HIGH_DIVERSITY\t.\tGT\t0/1");
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

        feature = decode.decode("1	869465	1	N	<DEL>	1293.8	.	SVTYPE=DEL;POS=869465;SVLEN=-752;END=870217;STRANDS=+-:31;IMPRECISE;CIPOS=-10,157;CIEND=-84,10;CIPOS95=-3,31;CIEND95=-36,3;");
        Assert.assertEquals(feature.beg, 869454);
        Assert.assertEquals(feature.end, 870227);

        feature = decode.decode("1	1157791	4345_1	N	N[4:76212291[	0.0	.	SVTYPE=BND;POS=1157791;STRANDS=+-:5;IMPRECISE;CIPOS=-8,8;CIEND=-9,6");
        Assert.assertEquals(feature.beg, 1157782);
        Assert.assertEquals(feature.end, 1157797);

        feature = decode.decode("1	668630	esv3584976	G	<CN2>	100	PASS	AC=64;AF=0.0127796;AN=5008;CIEND=-150,150;CIPOS=-150,150;CS=DUP_delly;END=850204;NS=2504;SVTYPE=DUP;IMPRECISE;DP=22135;EAS_AF=0.0595;AMR_AF=0;AFR_AF=0.0015;EUR_AF=0.001;SAS_AF=0.001;VT=SV;EX_TARGET");
        Assert.assertEquals(feature.beg, 668479);
        Assert.assertEquals(feature.end, 850354);

    }
}
