package org.mulinlab.varnote.cmdline;

import org.junit.Test;
import org.mulinlab.varnote.cmdline.query.Annotation;
import org.mulinlab.varnote.cmdline.query.Intersect;
import utils.TestUtils;


public final class AnnotationTest {

    @Test
    public void testIndex() {

//        String[] args = new String[]{ "-D:db,tag=d1,index=TBI", "/Users/hdd/Downloads/test_data/database1.sorted.bed.gz",
//                "-D:db,tag=d2", "/Users/hdd/Downloads/test_data/database2.sorted.tab.gz",
//                "-D", "/Users/hdd/Downloads/test_data/database3.sorted.tab.gz",
//                "-Q", "/Users/hdd/Downloads/test_data/q1.sorted.bed.gz"};
//
//        TestUtils.initClass(Annotation.class, args);

        String[] args1 = new String[]{
                "-D", "/Users/hdd/Downloads/test_data/database1.sorted.bed.gz",
                "-Q", "/Users/hdd/Downloads/test_data/q3.sorted.vcf.gz",
                "-T", "4", "--log", "true" ,"-FO", "true"};
        TestUtils.initClass(Annotation.class, args1);
    }
}
