package org.mulinlab.varnote.utils.queryreader.reader;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public final class BufferReader implements QueryReader {

    private BufferedReader reader;

    public BufferReader(final String file) throws FileNotFoundException {
        this.reader = new BufferedReader(new FileReader(file));
    }

    public BufferReader(final BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public void initReader() throws IOException {

    }

    @Override
    public String readLine() throws Exception {
        return this.reader.readLine();
    }

    @Override
    public void closeReader() throws IOException {
        this.reader.close();
    }
}
