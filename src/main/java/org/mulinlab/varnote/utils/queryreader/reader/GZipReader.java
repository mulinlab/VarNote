package org.mulinlab.varnote.utils.queryreader.reader;


import org.mulinlab.varnote.utils.stream.GZInputStream.GZReader;
import java.io.IOException;

public final class GZipReader implements QueryReader {

    private GZReader gzReader;

    public GZipReader(final GZReader gzReader) {
        this.gzReader = gzReader;
    }

    @Override
    public void initReader() throws IOException {

    }

    @Override
    public String readLine() throws Exception {
        return this.gzReader.readLine();
    }

    @Override
    public void closeReader() throws IOException {
        this.gzReader.close();
    }
}
