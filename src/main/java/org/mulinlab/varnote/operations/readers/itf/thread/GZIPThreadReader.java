package org.mulinlab.varnote.operations.readers.itf.thread;


import org.mulinlab.varnote.operations.readers.itf.QueryReaderItf;
import org.mulinlab.varnote.utils.stream.GZInputStream.GZReader;
import java.io.IOException;

public final class GZIPThreadReader implements QueryReaderItf {

    private GZReader gzReader;

    public GZIPThreadReader(final GZReader gzReader) {
        this.gzReader = gzReader;
    }

    @Override
    public void initReader() throws IOException {

    }

    @Override
    public String getFilePath() {
        return gzReader.getFilePath();
    }

    @Override
    public String readLine() throws Exception {
        return this.gzReader.readLine();
    }

    @Override
    public void closeReader() throws IOException {
        this.gzReader.close();
    }

    @Override
    public long getPosition() {
        return 0;
    }
}
