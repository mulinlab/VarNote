package org.mulinlab.varnote.config.io.temp;

import htsjdk.tribble.util.LittleEndianOutputStream;
import java.io.*;

public final class FlatThreadPrintter extends ThreadPrintter {

    public FlatThreadPrintter(String outputPath, int index) throws FileNotFoundException {
        super(outputPath, index);
        this.writer = new LittleEndianOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
    }

    @Override
    public void print(String s) throws IOException {
        this.writer.writeBytes(s);
        this.writer.writeByte(newline);
    }

}
