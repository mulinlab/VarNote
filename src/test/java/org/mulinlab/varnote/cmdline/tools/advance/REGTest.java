package org.mulinlab.varnote.cmdline.tools.advance;

import org.junit.Test;
import utils.TestUtils;

import static org.junit.Assert.*;

public class REGTest {

    @Test
    public void testVCF() {
        String[] args = new String[]{
                "-D:bed,tag=regbase,index=TBI", "http://202.113.53.226/VarNoteDB_dandan/hg19/v1.0/FA/VarNoteDB_FA_regBase_prediction/VarNoteDB_FA_regBase_prediction.gz",
                "-D:bed,tag=roadmap", "/Users/hdd/Desktop/vanno/random/hg19/VarNoteDB_FP_Roadmap_127Epi.bed.gz",
//                "-Q:tab,c=1,b=3,e=4,ref=5,alt=6,0=true", "/Users/hdd/Desktop/HT/5mC/N_NS/sigin.txt",
                "-Q:tab,c=1,b=2,e=2,ref=4,alt=5", "/Users/hdd/Desktop/vanno/cepip/immuno_pleiotropic_variants.txt",
                "-ID", "E029",
                "-ID", "E030",
                "-ID", "E031",
                "-ID", "E032",
                "-ID", "E033",
                "-ID", "E034",
                "-ID", "E035",
                "-ID", "E036",
                "-ID", "E037",
                "-ID", "E038",
                "-ID", "E039",
                "-ID", "E040",
                "-ID", "E041",
                "-ID", "E042",
                "-ID", "E043",
                "-ID", "E044",
                "-ID", "E045",
                "-ID", "E046",
                "-ID", "E047",
                "-ID", "E048",
                "-ID", "E116",
                "-ID", "E124",

                "-SP", "/Users/hdd/Desktop/vanno/ser/",
                "-G", "hg19",
                "-TS", "refseq",

                "-T", "4",
                "-Z", "false",
                "--log", "true"
        };
        TestUtils.initClass(REG.class, args);

        //http://147.8.193.36/regBase1/v1.0/regBase_prediction.gz
    }
}