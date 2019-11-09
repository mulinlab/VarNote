package org.mulinlab.varnote.cmdline;

import org.junit.Test;
import org.mulinlab.varnote.cmdline.tools.Intersect;
import org.mulinlab.varnote.utils.LoggingUtils;
import utils.TestUtils;


public final class IntersectTest {


    @Test
    public void testVCF() {

        String[] args = new String[]{
                "-D", "src/test/resources/database4.sorted.vcf.gz",
                "-Q", "src/test/resources/test5.vcf",
                "-T", "1",
                "-O", "src/test/resources/out/q3.out",
                "-Z", "false",
                "-OM", "2",
                "-RC", "false",
                "--log", "true"
        };
        TestUtils.initClass(Intersect.class, args);
    }


    @Test
    public void testBED() {
        LoggingUtils.setLog4JLoggingPath("src/test/resources/log1.txt", "vanno");
        String[] args = new String[]{ "-D:db,tag=d1,index=TBI", "src/test/resources/database1.sorted.bed.gz",
                "-D:db,tag=d2", "src/test/resources/database2.sorted.tab.gz",
                "-D", "src/test/resources/database3.sorted.tab.gz",
                "-Q", "src/test/resources/q1.sorted.bed.gz"};

        TestUtils.initClass(Intersect.class, args);
    }

    @Test
    public void testTAB() {
        LoggingUtils.setLog4JLoggingPath("src/test/resources/log3.txt", "vanno");
        LoggingUtils.removeLog4JLoggingPath("vanno");
        String[] args = new String[]{ "-D:db,tag=d1,index=TBI", "src/test/resources/database1.sorted.bed.gz",
            "-D:db", "src/test/resources/database2.sorted.tab.gz",
            "-D", "src/test/resources/database3.sorted.tab.gz",
            "-Q:tab,c=2,b=3,e=4,ref=5,alt=6", "src/test/resources/q4.sorted.tab.gz",
            "-OM", "1"};

        TestUtils.initClass(Intersect.class, args, true);
    }
}
