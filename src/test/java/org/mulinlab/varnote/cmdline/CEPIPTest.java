package org.mulinlab.varnote.cmdline;

import org.junit.Test;
import org.mulinlab.varnote.cmdline.tools.advance.REG;
import utils.TestUtils;


public final class CEPIPTest {

    @Test
    public void testVCF() {
        String[] args = new String[]{
                "-D:bed,tag=regbase,index=TBI", "http://147.8.193.36/regBase1/v1.0/regBase_prediction.gz",
                "-D:bed,tag=roadmap", "/Users/hdd/Desktop/vanno/cepip/VarNoteDB_FP_Roadmap_127Epi.bed.gz",
                "-Q:tab,c=1,b=3,e=4,ref=5,alt=6,0=true", "/Users/hdd/Desktop/HT/5mC/N_NS/sigin.txt",
                "-T", "4",
                "-Z", "false",
                "--log", "true" };
        TestUtils.initClass(REG.class, args);

        //http://147.8.193.36/regBase1/v1.0/regBase_prediction.gz
    }

}
