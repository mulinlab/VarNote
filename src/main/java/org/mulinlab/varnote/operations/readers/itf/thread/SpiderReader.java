package org.mulinlab.varnote.operations.readers.itf.thread;

import org.mulinlab.varnote.operations.readers.itf.QueryReaderItf;
import org.mulinlab.varnote.utils.stream.BZIP2InputStream.Spider;

import java.io.IOException;

public final class SpiderReader implements QueryReaderItf {

    private Spider spider;

    public SpiderReader(final Spider spider) {
        this.spider = spider;
    }

    @Override
    public void initReader() throws IOException {

    }

    @Override
    public String getFilePath() {
        return spider.getFilePath();
    }

    @Override
    public String readLine() throws Exception {
        byte[] bytTemp = this.spider.readLine();
        if(bytTemp != null) {
            return new String(bytTemp);
        } else return null;
    }

    @Override
    public void closeReader() throws IOException {
        this.spider.closeInputStream();
    }

    @Override
    public long getPosition() {
        return 0;
    }
}
