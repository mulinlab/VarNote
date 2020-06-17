package org.mulinlab.varnote.config.io.temp;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.tribble.util.LittleEndianOutputStream;
import org.mulinlab.varnote.config.param.output.OutParam;
import java.io.*;
import java.util.zip.GZIPOutputStream;


public class ZipPrintter extends Printter {

    public ZipPrintter(OutParam outParam) {
        super(outParam);
    }


    @Override
    public void addPrintter(String path, Integer index) throws IOException {
        threadPrintters.add(new ZipThreadPrintter(path, index));
    }
}
