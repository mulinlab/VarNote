package org.mulinlab.varnote.config.io.temp;

import htsjdk.tribble.util.LittleEndianOutputStream;

import java.io.*;

public abstract class ThreadPrintter {
    protected File file;
    protected final int index;
    LittleEndianOutputStream writer;

    public ThreadPrintter(final String outputPath, final int index) throws FileNotFoundException {
        super();
        this.index = index;
        file = new File(outputPath + ".temp" + index);
    }

    public OutputStream getWriter() {
        return this.writer;
    }
    public File getFile() {
        return this.file;
    }
    public int getIndex() {
        return index;
    }

    public abstract void print(String s) throws IOException;

    public void tearDownPrintter() {
        if (this.writer != null) {
            try {
                this.writer.flush();
                this.writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
