package org.mulinlab.varnote.operations.readers.gt1000g;

import javafx.util.Pair;
import org.junit.Test;
import org.mulinlab.varnote.utils.node.LocFeature;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class VannoGTReaderTest {

    @Test
    public void computeLD() {
        try {
            VannoGTReader gtReader = new VannoGTReader("/Users/hdd/Desktop/vanno/random/hg19/1kg.phase3.v5.shapeit2.eur.hg19.all.vcf.gz.bit");
            Pair<Double, Double> r = gtReader.computeLD(new LocFeature(16318509, 16318510, "1"), new LocFeature(16318528, 16318529, "1"));

            System.out.println();
//            gtReader.test();
            gtReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getLDList() {
        try {
            VannoGTReader gtReader = new VannoGTReader("/Users/hdd/Desktop/vanno/random/hg19/1kg.phase3.v5.shapeit2.eur.hg19.all.vcf.gz.bit");

            List<VannoGTReader.LDVariant> list = gtReader.getLDList(new LocFeature(5453459, 5453460, "9"), 0.8);
            System.out.println();
//            gtReader.test();
            gtReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}