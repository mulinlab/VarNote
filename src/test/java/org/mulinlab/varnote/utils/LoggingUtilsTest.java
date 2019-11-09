package org.mulinlab.varnote.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class LoggingUtilsTest {

    @Test
    public void setLoggingLevel() {
        LoggingUtils.setLog4JLoggingPath("src/test/resources/log.txt", "vanno");
    }
}