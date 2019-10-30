package org.mulinlab.varnote.cmdline;

import org.junit.Test;
import org.mulinlab.varnote.cmdline.tools.AnnotationIntersectFile;
import utils.TestUtils;


public final class AnnotationIntersectFileTest {

    @Test
    public void testVCF() {
        String[] args = new String[]{
                "-I", "src/test/resources/out/q3.out",
                "-loj", "true",
                "-Z", "false",
                "-O", "src/test/resources/out/q3.out.anno",
                "--log", "true"
        };
        TestUtils.initClass(AnnotationIntersectFile.class, args);
    }
}
