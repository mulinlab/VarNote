package org.mulinlab.varnote.config.io.temp;

import htsjdk.tribble.util.LittleEndianOutputStream;
import org.mulinlab.varnote.utils.gz.MyBlockCompressedOutputStream;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public final class ZipThreadPrintter extends ThreadPrintter {

    public ZipThreadPrintter(String outputPath, int index) throws IOException {
        super(outputPath, index);
        this.writer = new LittleEndianOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(file))));
    }

    @Override
    public void print(String s) throws IOException {
        this.writer.writeBytes(s);
        this.writer.writeByte(newline);
    }

}
