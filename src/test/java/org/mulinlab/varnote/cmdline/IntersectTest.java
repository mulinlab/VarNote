package org.mulinlab.varnote.cmdline;

import org.junit.Test;
import org.mulinlab.varnote.cmdline.query.Intersect;
import utils.TestUtils;


public final class IntersectTest {

    @Test
    public void testIndex() {

//        String[] args = new String[]{ "-D:db,tag=d1,index=TBI", "/Users/hdd/Downloads/test_data/database1.sorted.bed.gz",
//                "-D:db,tag=d2", "/Users/hdd/Downloads/test_data/database2.sorted.tab.gz",
//                "-D", "/Users/hdd/Downloads/test_data/database3.sorted.tab.gz",
//                "-Q", "/Users/hdd/Downloads/test_data/q1.sorted.bed.gz",
//        "-OM", "1"};
//
//        TestUtils.initClass(Intersect.class, args);

        String[] args1 = new String[]{
                "-D", "/Users/hdd/Downloads/test_data/database3.sorted.tab.gz",
                "-Q", "/Users/hdd/Downloads/test_data/q3.sorted.vcf.gz",
                "-T", "1", "--log", "true" , "-O", "/Users/hdd/Downloads/test_data/out/q3.out", "-Z", "false"};
        TestUtils.initClass(Intersect.class, args1);
    }
}
