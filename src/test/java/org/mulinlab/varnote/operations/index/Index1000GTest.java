package org.mulinlab.varnote.operations.index;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class Index1000GTest {

    @Test
    public void makeIndex() throws IOException {
        Index1000G index = new Index1000G(new File("/Users/hdd/Desktop/vanno/random/hg19/1kg.phase3.v5.shapeit2.afr.hg19.all.vcf.gz"));
        index.makeIndex();
        index.close();
    }
}