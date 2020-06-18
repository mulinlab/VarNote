package org.mulinlab.varnote.cmdline;

import org.junit.Test;
import org.mulinlab.varnote.cmdline.tools.QueryRegion;
import utils.TestUtils;

public final class QueryRegionTest {

    @Test
    public void testIndex() {

        String[] args = new String[]{ "-D:db,tag=d1,index=TBI", "src/test/resources/database1.sorted.bed.gz",
                "-D:db,tag=d2", "src/test/resources/database2.sorted.tab.gz",
                "-Q", "chr1:1-100000",
                "--log=false", "-L=true"};

        TestUtils.initClass(QueryRegion.class, args);
    }
}
