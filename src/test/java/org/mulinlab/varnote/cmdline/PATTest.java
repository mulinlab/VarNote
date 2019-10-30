package org.mulinlab.varnote.cmdline;

import org.junit.Test;
import org.mulinlab.varnote.cmdline.tools.advance.PAT;
import org.mulinlab.varnote.cmdline.tools.advance.REG;
import utils.TestUtils;


public final class PATTest {


    @Test
    public void testVCF() {
        String[] args = new String[]{
                "-D", "src/test/resources/database4.sorted.vcf.gz",
                "-Q", "/Users/hdd/Desktop/vanno/wkegg/FSGS.jointcall.new.vcf",
                "-T", "4", "--log", "false" };
        TestUtils.initClass(PAT.class, args);
    }

}
