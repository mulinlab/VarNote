package org.mulinlab.varnote.config.io.temp;

import htsjdk.tribble.util.LittleEndianOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.config.param.output.OutParam;
import org.mulinlab.varnote.utils.LoggingUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;


public abstract class Printter {

    protected final Logger logger = LoggingUtils.logger;

    protected OutParam outParam;
    protected String tempFolderPath;
    protected List<ThreadPrintter> threadPrintters;

    public Printter(final OutParam outParam) {
        this.outParam = outParam;
    }

    public void mergeFile() throws IOException {
        mergeResults();
    }

    public void mergeResults() throws IOException {
        if(threadPrintters.size() == 1) {
            File dest = new File(outParam.getOutputPath());
            if(dest.exists()) {
                dest.delete();
            }
            threadPrintters.get(0).tearDownPrintter();
            FileUtils.moveFile(threadPrintters.get(0).getFile(), new File(outParam.getOutputPath()));
        } else {
            WritableByteChannel outChannel = Channels.newChannel(new FileOutputStream(outParam.getOutputPath()));
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 128);
            ReadableByteChannel inChannel = null;

            for (int i = 0; i < threadPrintters.size(); i++) {
                threadPrintters.get(i).tearDownPrintter();
                File tempFile = threadPrintters.get(i).getFile();
                if (tempFile != null) {
                    inChannel = Channels.newChannel(new FileInputStream(tempFile));
                    while (inChannel.read(byteBuffer) > 0) {
                        byteBuffer.flip();
                        outChannel.write(byteBuffer);
                        byteBuffer.clear();
                    }

                    inChannel.close();
                }
            }
            outChannel.close();
            doEnd();
        }
    }

    public void doEnd() {
        for (int i = 0; i < threadPrintters.size(); i++) {
            File tempFile = threadPrintters.get(i).getFile();
            if (!tempFile.delete()) {
                System.err.println("Delete " + tempFile.getAbsolutePath() + " failed.");
            }
        }
        if (!new File(tempFolderPath).delete()) {
            System.err.println("Delete " + tempFolderPath + " failed.");
        }
    }

    public void init() {
        makeTemp();
    }

    public void makeTemp() {
        File tempFolder =  new File(new File(this.outParam.getOutputPath()).getParent() + File.separator + "temp");
        if (!tempFolder.exists()) {
            try{
                tempFolder.mkdir();
            } catch(SecurityException se) {
                logger.error("Creating directory " + tempFolder.getAbsolutePath() + " with error! please check.");
            }
        }

        this.tempFolderPath = tempFolder.getAbsolutePath();
    }

    public void setPrintter(final int thread) {
        threadPrintters = new ArrayList<ThreadPrintter>();
        try {
            for (int i = 0; i < thread; i++) {
                addPrintter(this.tempFolderPath + File.separator + outParam.getOutputName(), i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printHeader(final List<String> comments) throws IOException {
        if(comments != null && comments.size() > 0 && threadPrintters != null && threadPrintters.size() > 0)
            for (String comment: comments) {
                getPrintter(0).print(comment);
            }
    }

    public abstract void addPrintter(final String path, final Integer index) throws FileNotFoundException, IOException;

    public void print(final String result, final int index) throws IOException {
        if(result != null) {
            threadPrintters.get(index).print(result);
        }
    }

    public ThreadPrintter getPrintter(final int i) {
        return threadPrintters.get(i);
    }
}
