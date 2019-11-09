package org.mulinlab.varnote.utils;

import org.junit.Test;
import org.mulinlab.varnote.utils.format.Format;

import static org.junit.Assert.*;

public class VannoUtilsTest {

    @Test
    public void formatIsMatch() {
        VannoUtils.formatIsMatch("src/test/resources/database4.sorted.vcf.gz", Format.VCF);
        VannoUtils.formatIsMatch("src/test/resources/database1.sorted.bed.gz", Format.BED);
        VannoUtils.formatIsMatch("src/test/resources/database2.sorted.tab.gz", Format.BED);
    }
}