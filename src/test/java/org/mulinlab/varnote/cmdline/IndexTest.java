package org.mulinlab.varnote.cmdline;

import org.junit.Test;
import org.mulinlab.varnote.cmdline.index.Index;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.format.Format;
import utils.TestUtils;

public final class IndexTest {

    @Test
    public void testIndexVCF() {
        String[] args = new String[]{
                "-I", "src/test/resources/database4.sorted.vcf.gz" };

        TestUtils.initClass(Index.class, args, false);
    }

    @Test
    public void testIndexVCF1() {
        String[] args = new String[]{"-I", "/Users/hdd/Desktop/vanno/random/hg19/VarNoteDB_AF_1000G_phase3.vcf.gz"};

        TestUtils.initClass(Index.class, args, false);
    }

    @Test
    public void testIndexTAB() {
        String[] args = new String[]{
                "-I:tab,c=1,b=2,e=2", "src/test/resources/database3.sorted.tab.gz" ,
                "-HP", "src/test/resources/database3.sorted.tab.gz.header"};

        TestUtils.initClass(Index.class, args, false);
    }

    @Test
    public void testIndexTAB1() {
        String[] args = new String[]{
                "-I:tab,c=1,b=4,e=5,0=true", "/Users/hdd/Desktop/vanno/random/hg19/gencode.v32lift37.annotation.gene.sort.gtf.gz"};

        TestUtils.initClass(Index.class, args, false);
    }

    @Test
    public void testIndexTAB2() {
        String[] args = new String[]{
                "-I:tab,c=1,b=2,e=2,ref=3,alt=4,0=false", "/Users/hdd/Desktop/vanno/random/hg19/EUR.gz"};

        TestUtils.initClass(Index.class, args, false);
    }

    @Test
    public void testIndexBED() {
        String[] args = new String[]{
                "-I", "/Users/hdd/Desktop/vanno/cepip/hg19/VarNoteDB_FP_Roadmap_127Epi.bed.gz",
                "-HP", "/Users/hdd/Desktop/vanno/cepip/hg19/header"};

        TestUtils.initClass(Index.class, args, false);
    }

    @Test
    public void testIndexBED1() {
        String[] args = new String[]{
                "-I", "/Users/hdd/Desktop/vanno/random/hg19/VarNoteDB_FP_Roadmap_127Epi.bed.gz",
                "-HP", "/Users/hdd/Desktop/vanno/random/hg19/roadmap.header"
        };
        TestUtils.initClass(Index.class, args, false);
    }

    @Test
    public void format() {
        Format format = VannoUtils.checkQueryFormat("tab");

        System.out.println(format);
    }
}
