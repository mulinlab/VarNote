package org.mulinlab.varnote.utils.queryreader.reader;

import org.mulinlab.varnote.utils.queryreader.reader.QueryReader;
import org.mulinlab.varnote.utils.stream.BZIP2InputStream.Spider;

import java.io.IOException;

public final class SpiderReader implements QueryReader {

    private Spider spider;

    public SpiderReader(final Spider spider) {
        this.spider = spider;
    }

    @Override
    public void initReader() throws IOException {

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
}
