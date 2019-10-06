package org.mulinlab.varnote.config.io.temp;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.tribble.util.LittleEndianOutputStream;
import org.mulinlab.varnote.config.param.output.OutParam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class ZipPrintter extends Printter {

    public ZipPrintter(OutParam outParam) {
        super(outParam);
        finalOutputStream = new LittleEndianOutputStream(new BlockCompressedOutputStream(new File(outParam.getOutputPath())));
    }

    @Override
    public void mergeResults() throws IOException {
        byte[] buf = new byte[1024 * 128];
        for (int i = 0; i < threadPrintters.size(); i++) {
            threadPrintters.get(i).tearDownPrintter();
            File tempFile = threadPrintters.get(i).getFile();
            if (tempFile != null) {
                BlockCompressedInputStream in = new BlockCompressedInputStream(tempFile);
                int n = 0;
                while((n = in.read(buf)) != -1) {
                    finalOutputStream.write(buf, 0, n);
                }
                in.close();

            }
        }
    }

    @Override
    public void addPrintter(String path, Integer index) throws FileNotFoundException {
        threadPrintters.add(new ZipThreadPrintter(path, index));
    }
}
