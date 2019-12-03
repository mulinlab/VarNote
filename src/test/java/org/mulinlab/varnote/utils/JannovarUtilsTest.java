package org.mulinlab.varnote.utils;

import org.junit.Test;
import org.mulinlab.varnote.utils.jannovar.VariantAnnotation;
import org.mulinlab.varnote.utils.node.LocFeature;

import static org.junit.Assert.*;

public class JannovarUtilsTest {

    @Test
    public void annotate() {
        JannovarUtils jannovarUtils = new JannovarUtils("/Users/hdd/Desktop/vanno/ser/");

        LocFeature locFeature = new LocFeature(169579971, 169579971, "3");
        locFeature.ref = "G";
        locFeature.alt = "A";
        VariantAnnotation annotation = jannovarUtils.annotate(locFeature);

        System.out.println(annotation.toString());
    }
}