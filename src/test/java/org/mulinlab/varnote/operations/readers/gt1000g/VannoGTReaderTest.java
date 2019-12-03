package org.mulinlab.varnote.operations.readers.gt1000g;

import org.junit.Test;
import org.mulinlab.varnote.utils.node.LocFeature;

import java.io.IOException;

import static org.junit.Assert.*;

public class VannoGTReaderTest {

    @Test
    public void computeLD() {
        try {
            VannoGTReader gtReader = new VannoGTReader("/Users/hdd/Desktop/vanno/random/hg19/1kg.phase3.v5.shapeit2.afr.hg19.all.vcf.gz");
            gtReader.computeLD(new LocFeature(16318509, 16318510, "1"), new LocFeature(16318528, 16318529, "1"));
//            gtReader.test();
            gtReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}