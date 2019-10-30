package org.mulinlab.varnote.cmdline;

import org.junit.Test;
import org.mulinlab.varnote.cmdline.tools.Count;
import utils.TestUtils;

public final class CountTest {

    @Test
    public void testIndex() {

        String[] args = new String[]{ "-D:db,tag=d1,index=TBI", "src/test/resources/database1.sorted.bed.gz",
                "-D:db,tag=d2", "src/test/resources/database2.sorted.tab.gz",
                "-D", "src/test/resources/database3.sorted.tab.gz",
                "-Q", "src/test/resources/test_data/q1.sorted.bed.gz"};

        TestUtils.initClass(Count.class, args);
    }


    @Test
    public void testIndex2() {

        String[] args = new String[]{
                "-D", "src/test/resources/database4.sorted.vcf.gz",
                "-Q", "src/test/resources/test5.vcf",
                "-T", "4", "--log", "true" };

        TestUtils.initClass(Count.class, args);
    }
}
