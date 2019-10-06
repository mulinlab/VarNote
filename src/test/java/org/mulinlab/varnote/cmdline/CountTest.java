package org.mulinlab.varnote.cmdline;

import org.junit.Test;
import org.mulinlab.varnote.cmdline.query.Count;
import utils.TestUtils;

public final class CountTest {

    @Test
    public void testIndex() {

        String[] args = new String[]{ "-D:db,tag=d1,index=TBI", "/Users/hdd/Downloads/test_data/database1.sorted.bed.gz",
                "-D:db,tag=d2", "/Users/hdd/Downloads/test_data/database2.sorted.tab.gz",
                "-D", "/Users/hdd/Downloads/test_data/database3.sorted.tab.gz",
                "-Q", "/Users/hdd/Downloads/test_data/q1.sorted.bed.gz"};

        TestUtils.initClass(Count.class, args);
    }
}
