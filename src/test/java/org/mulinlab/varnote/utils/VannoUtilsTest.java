package org.mulinlab.varnote.utils;

import org.junit.Test;
import org.mulinlab.varnote.utils.node.LocFeature;

import static org.junit.Assert.*;

public class VannoUtilsTest {

    @Test
    public void posToNode() {
        LocFeature locFeature = VannoUtils.regionToNode("X_GL456233_RANDOM:36727-41667");
        assertEquals(locFeature.beg, 36727);
        assertEquals(locFeature.end, 41667);

        locFeature = VannoUtils.posAlleleToNode("X_GL456233_RANDOM:1234-AAAAA-G");
        assertEquals(locFeature.beg, 1233);
        assertEquals(locFeature.end, 1238);
        assertEquals(locFeature.ref, "AAAAA");
        assertEquals(locFeature.alt, "G");

        locFeature = VannoUtils.posToNode("chr1:1234");
        assertEquals(locFeature.beg, 1233);
        assertEquals(locFeature.end, 1234);

        locFeature = VannoUtils.posAlleleToNode("1:1234-A-G");
        assertEquals(locFeature.beg, 1233);
        assertEquals(locFeature.end, 1234);
        assertEquals(locFeature.ref, "A");
        assertEquals(locFeature.alt, "G");

        locFeature = VannoUtils.posAlleleToNode("chr1:1234-AAAAA-G");
        assertEquals(locFeature.beg, 1233);
        assertEquals(locFeature.end, 1238);
        assertEquals(locFeature.ref, "AAAAA");
        assertEquals(locFeature.alt, "G");

        locFeature = VannoUtils.regionToNode("chr1:1234-1235");
        assertEquals(locFeature.beg, 1234);
        assertEquals(locFeature.end, 1235);

        locFeature = VannoUtils.regionToNode("1:1234-1238");
        assertEquals(locFeature.beg, 1234);
        assertEquals(locFeature.end, 1238);
    }
}