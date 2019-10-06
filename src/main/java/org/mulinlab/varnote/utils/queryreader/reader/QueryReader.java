package org.mulinlab.varnote.utils.queryreader.reader;

import java.io.IOException;

public interface QueryReader {
    public void initReader() throws IOException;
    public String readLine() throws Exception;
    public void closeReader() throws IOException;
}
