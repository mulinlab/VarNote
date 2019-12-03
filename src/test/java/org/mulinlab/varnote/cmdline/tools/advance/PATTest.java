package org.mulinlab.varnote.cmdline.tools.advance;

import org.junit.Test;
import utils.TestUtils;


public final class PATTest {


    @Test
    public void testVCF() {
        String[] args = new String[]{
                "-D:vcf,tag=gnomad", "http://202.113.53.226/VarNoteDB_dandan/hg19/v1.0/AF/VarNoteDB_AF_gnomAD_Genome/VarNoteDB_AF_gnomAD_Genome.vcf.gz",
                "-D:tab,c=1,b=2,e=2,ref=3,alt=4,tag=dbNSFP", "http://202.113.53.226/VarNoteDB_dandan/hg19/v1.0/FA/VarNoteDB_FA_dbNSFP/VarNoteDB_FA_dbNSFP.gz",
                "-D:bed,tag=regbase,index=TBI", "http://202.113.53.226/VarNoteDB_dandan/hg19/v1.0/FA/VarNoteDB_FA_regBase_prediction/VarNoteDB_FA_regBase_prediction.gz",
                "-Q", "/Users/hdd/Desktop/vanno/wkegg/FSGS.jointcall.new.vcf",
                "-P", "/Users/hdd/Desktop/vanno/wkegg/FSGS_0.ped",
                "-T", "4", "--log", "false" };
        TestUtils.initClass(PAT.class, args);
    }

}
