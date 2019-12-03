package org.mulinlab.varnote.cmdline.tools.advance;

import org.junit.Test;
import utils.TestUtils;

public class CANTest {

    @Test
    public void testVCF() {
        String[] args = new String[]{
                "-D:bed,tag=regbase,index=TBI", "http://202.113.53.226/VarNoteDB_dandan/hg19/v1.0/FA/VarNoteDB_FA_regBase_prediction/VarNoteDB_FA_regBase_prediction.gz",
                "-D:vcf,tag=gnomad", "http://202.113.53.226/VarNoteDB_dandan/hg19/v1.0/AF/VarNoteDB_AF_gnomAD_Genome/VarNoteDB_AF_gnomAD_Genome.vcf.gz",
                "-D:vcf,tag=cosmic", "http://202.113.53.226/VarNoteDB_dandan/hg19/v1.0/TA/VarNoteDB_TA_COSMIC_Coding/VarNoteDB_TA_COSMIC_Coding.vcf.gz",
                "-D:vcf,tag=icgc", "http://202.113.53.226/VarNoteDB_dandan/hg19/v1.0/TA/VarNoteDB_TA_ICGC/VarNoteDB_TA_ICGC.vcf.gz",
                "-Q:tab,c=1,b=2,e=2,ref=4,alt=5", "/Users/hdd/Desktop/vanno/cepip/VCF.txt",


                "-SP", "/Users/hdd/Desktop/vanno/ser/",
                "-G", "hg19",
                "-TS", "refseq",

                "-T", "4",
                "-Z", "false",
                "--log", "true" };
        TestUtils.initClass(CAN.class, args);

        //http://147.8.193.36/regBase1/v1.0/regBase_prediction.gz
    }
}