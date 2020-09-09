package org.mulinlab.varnote.cmdline;

import org.junit.Test;
import org.mulinlab.varnote.cmdline.index.Index;
import utils.TestUtils;

public final class IndexTest {

    @Test
    public void testIndexVCF() {
        String[] args = new String[]{
                "-I:bed", "~/test.bedgraph.gz" };

        TestUtils.initClass(Index.class, args, false);
    }

    @Test
    public void testIndexCood() {
        String[] args = new String[]{
                "-I:coordOnly", "~/test.bedgraph.gz" };

        TestUtils.initClass(Index.class, args, false);
    }

}
