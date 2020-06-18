package org.mulinlab.varnote.cmdline;

import org.junit.Test;
import org.mulinlab.varnote.cmdline.index.IndexInfo;
import utils.TestUtils;

public final class IndexInfoTest {


    @Test
    public void testIndex() {
        String[] args = new String[]{"-I=./test_data/database1.sorted.bed.gz", "-PH=true", "-PM=true", "-LC=true"};

        TestUtils.initClass(IndexInfo.class, args);
    }
}