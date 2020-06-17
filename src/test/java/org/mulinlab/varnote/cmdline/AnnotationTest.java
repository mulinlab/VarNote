package org.mulinlab.varnote.cmdline;

import org.junit.Test;
import org.mulinlab.varnote.cmdline.tools.Annotation;
import org.mulinlab.varnote.cmdline.tools.Intersect;
import org.mulinlab.varnote.utils.LoggingUtils;
import utils.TestUtils;


public final class AnnotationTest {

    @Test
    public void testVCF() {
        String[] args = new String[]{
                "-D", "src/test/resources/database1.sorted.bed.gz",
//                "-D", "src/test/resources/database3.sorted.tab.gz",
                "-Q", "src/test/resources/q3.sorted.vcf.gz",
                "-T", "4",
                "--log", "true",
                "-FO", "true",
                "-Z", "false",
                "-OF", "BED",
                "-A", "src/test/resources/config/q3.extract.d1"

        };
        TestUtils.initClass(Annotation.class, args);
    }

    @Test
    public void testBED() {
        String[] args = new String[]{ "-D:db,tag=d1,index=TBI", "src/test/resources/database1.sorted.bed.gz",
            "-D:db,tag=d2", "src/test/resources/database2.sorted.tab.gz",
            "-D", "src/test/resources/database3.sorted.tab.gz",
            "-Q", "src/test/resources/q1.sorted.bed",
                    "-Z", "false",
    //              "-loj", "true",
                    "-OF", "VCF",
                    "-A", "src/test/resources/config/d1_d2.extract"
        };

        TestUtils.initClass(Annotation.class, args);
    }

}
