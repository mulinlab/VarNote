package org.mulinlab.varnote.cmdline;

import org.junit.Test;
import org.mulinlab.varnote.cmdline.query.Annotation;
import org.mulinlab.varnote.cmdline.query.Intersect;
import utils.TestUtils;


public final class AnnotationTest {

    @Test
    public void testVCF() {
        String[] args = new String[]{
                "-D", "src/test/resources/database1.sorted.bed.gz",
                "-Q", "src/test/resources/test_data/q3.sorted.vcf.gz",
                "-T", "4", "--log", "true" ,"-FO", "true"};
        TestUtils.initClass(Annotation.class, args);
    }

    @Test
    public void testBED() {
                String[] args = new String[]{ "-D:db,tag=d1,index=TBI", "/Users/hdd/Downloads/test_data/database1.sorted.bed.gz",
                "-D:db,tag=d2", "/Users/hdd/Downloads/test_data/database2.sorted.tab.gz",
                "-D", "/Users/hdd/Downloads/test_data/database3.sorted.tab.gz",
                "-Q", "/Users/hdd/Downloads/test_data/q1.sorted.bed.gz"};

        TestUtils.initClass(Annotation.class, args);
    }

    @Test
    public void testTAB() {
        String[] args = new String[]{
                "-D", "/Users/hdd/Downloads/test_data/database1.sorted.bed.gz",
                "-Q", "/Users/hdd/Downloads/test_data/q4.sorted.tab.gz",
                "-T", "4", "--log", "true"};
        TestUtils.initClass(Annotation.class, args);
    }
}
