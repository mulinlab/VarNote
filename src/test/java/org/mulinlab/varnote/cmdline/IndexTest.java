package org.mulinlab.varnote.cmdline;

import org.mulinlab.varnote.cmdline.abstractclass.CMDProgram;
import org.junit.Assert;
import org.junit.Test;
import org.mulinlab.varnote.cmdline.index.Index;
import org.mulinlab.varnote.cmdline.query.Count;
import utils.TestUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public final class IndexTest {

    @Test
    public void testIndexVCF() {
        String[] args = new String[]{
                "-I", "src/test/resources/database4.sorted.vcf.gz" };

        TestUtils.initClass(Index.class, args, false);
    }

    @Test
    public void testIndexTAB() {
        String[] args = new String[]{
                "-I", "src/test/resources/database3.sorted.tab.gz" ,
                "-HP", "src/test/resources/database3.sorted.tab.gz.header"};

        TestUtils.initClass(Index.class, args, false);
    }
}
