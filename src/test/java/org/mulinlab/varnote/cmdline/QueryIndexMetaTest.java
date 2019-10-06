package org.mulinlab.varnote.cmdline;

import org.mulinlab.varnote.cmdline.abstractclass.CMDProgram;
import org.junit.Assert;
import org.junit.Test;
import org.mulinlab.varnote.cmdline.index.QueryIndexMeta;
import org.mulinlab.varnote.cmdline.query.QueryRegion;
import utils.TestUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public final class QueryIndexMetaTest {

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

        TestUtils.initClass(QueryIndexMeta.class, args);
    }
}