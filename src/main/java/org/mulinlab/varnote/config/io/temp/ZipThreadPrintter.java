package org.mulinlab.varnote.config.io.temp;

import htsjdk.tribble.util.LittleEndianOutputStream;
import org.mulinlab.varnote.utils.gz.MyBlockCompressedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public final class ZipThreadPrintter extends ThreadPrintter {

    public ZipThreadPrintter(String outputPath, int index) throws FileNotFoundException {
        super(outputPath, index);
        this.writer = new LittleEndianOutputStream(new MyBlockCompressedOutputStream(file));
    }

    @Override
    public void print(String s) throws IOException {
        this.writer.writeBytes(s + "\n");
    }

}
