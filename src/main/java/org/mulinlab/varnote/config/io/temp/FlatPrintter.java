package org.mulinlab.varnote.config.io.temp;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.tribble.util.LittleEndianOutputStream;
import org.mulinlab.varnote.config.param.output.OutParam;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;

public final class FlatPrintter extends Printter {

    public FlatPrintter(OutParam outParam) throws FileNotFoundException {
        super(outParam);
        finalOutputStream = new LittleEndianOutputStream(new BufferedOutputStream(new FileOutputStream(new File(outParam.getOutputPath()))));
    }


    @Override
    public void mergeResults() throws IOException {
        byte[] buf = new byte[1024 * 128];
        for (int i = 0; i < threadPrintters.size(); i++) {
            threadPrintters.get(i).tearDownPrintter();
            File tempFile = threadPrintters.get(i).getFile();
            if (tempFile != null) {
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(tempFile));
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
        threadPrintters.add(new FlatThreadPrintter(path, index));
    }

}
