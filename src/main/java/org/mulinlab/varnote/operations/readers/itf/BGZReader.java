package org.mulinlab.varnote.operations.readers.itf;


import htsjdk.samtools.util.BlockCompressedInputStream;
import org.mulinlab.varnote.utils.VannoUtils;
import java.io.IOException;

public final class BGZReader implements QueryReaderItf {

    private BlockCompressedInputStream bgzStream;
    private String path;

    public BGZReader(final String path) throws IOException {
        this.path = path;
        this.bgzStream = VannoUtils.makeBGZ(path);
    }

    @Override
    public void initReader() throws IOException {

    }


    @Override
    public String readLine() throws Exception {
        return this.bgzStream.readLine();
    }

    @Override
    public String getFilePath() {
        return path;
    }

    public long getPosition() {
        return bgzStream.getFilePointer();
    }

    @Override
    public void closeReader() throws IOException {
        this.bgzStream.close();
    }
}
