package org.mulinlab.varnote.operations.readers.itf;

import java.io.IOException;

public interface QueryReaderItf {
    public void initReader() throws IOException;
    public String readLine() throws Exception;
    public long getPosition();
    public String getFilePath();
    public void closeReader() throws IOException;
}
