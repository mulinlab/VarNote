package org.mulinlab.varnote.operations.readers.gt1000g;

import org.junit.Test;

import java.io.IOException;

public class VannoGTIndexReaderTest {

    @Test
    public void readIndex() throws IOException {
        VannoGTIndex idxReader = new VannoGTIndex("/Users/hdd/Desktop/vanno/random/hg19/1kg.phase3.v5.shapeit2.afr.hg19.all.vcf.gz.bit.idx");
        System.out.println();
    }
}