package org.mulinlab.varnote.cmdline;

import org.junit.Test;
import org.mulinlab.varnote.cmdline.index.Index;
import utils.TestUtils;

public final class IndexTest {

    @Test
    public void testIndexVCF() {
        String[] args = new String[]{
                "-I", "src/test/resources/database4.sorted.vcf.gz" };

        TestUtils.initClass(Index.class, args, false);
    }

}
