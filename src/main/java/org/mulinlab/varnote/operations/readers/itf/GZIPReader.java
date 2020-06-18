package org.mulinlab.varnote.operations.readers.itf;



import java.io.*;
import java.util.zip.GZIPInputStream;

public final class GZIPReader implements QueryReaderItf {

    private BufferedReader reader;
    private String path;

    public GZIPReader(final String path) throws IOException {
        this.reader =  new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(path))));
        this.path = path;
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
