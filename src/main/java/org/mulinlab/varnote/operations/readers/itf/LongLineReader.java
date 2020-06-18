package org.mulinlab.varnote.operations.readers.itf;


import htsjdk.tribble.readers.LongLineBufferedReader;
import java.io.*;

public final class LongLineReader implements QueryReaderItf {

    private LongLineBufferedReader reader;
    private String path;

    public LongLineReader(final String path
    ) throws FileNotFoundException {
        this.reader = new LongLineBufferedReader(new InputStreamReader(new FileInputStream(path)));
        this.path = path;
    }

    public LongLineReader(final LongLineBufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public void initReader() throws IOException {

    }

    @Override
    public String getFilePath() {
        return path;
    }

    @Override
    public String readLine() throws Exception {
        return this.reader.readLine();
    }

    @Override
    public long getPosition() {
        return 0;
    }

    @Override
    public void closeReader() throws IOException {
        this.reader.close();
    }
}
