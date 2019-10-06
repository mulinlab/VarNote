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
    public void testIndex() {

        String[] args = new String[]{"-I=/Users/hdd/Downloads/test_data/database1.sorted.bed.gz", "-HP=/Users/hdd/Downloads/test_data/database1.sorted.bed.gz.header"};

        TestUtils.initClass(Index.class, args);
    }
}
