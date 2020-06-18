package org.mulinlab.varnote.config.anno.databse;

import org.junit.Test;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.headerparser.HeaderFormatReader;

public class HeaderFormatReaderTest {

    @Test
    public void readHeaderFromArray() {
        Format format = Format.newTAB();

        format = HeaderFormatReader.readHeaderFromArray("CHROM,POS,RSID,REF,ALT".split(","), format);
        System.out.println();
    }
}