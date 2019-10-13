package org.mulinlab.varnote.cmdline;

import org.junit.Test;
import org.mulinlab.varnote.cmdline.index.IndexQuery;
import utils.TestUtils;

public final class IndexQueryTest {

//    @Test
//    public void testIndexHelp() {
//        final Set<Class<?>> classes = new HashSet<>();
//        classes.add(QueryIndexMeta.class);
//
//        VarNoteCommandLine.printUsage(System.out, classes, GlobalParameter.PRO_CMD);
//    }

    @Test
    public void testIndex() {

        String[] args = new String[]{"-I=/Users/hdd/Downloads/test_data/database1.sorted.bed.gz", "-PH=true", "-PM=true", "-LC=true"};

        TestUtils.initClass(IndexQuery.class, args);
    }
}