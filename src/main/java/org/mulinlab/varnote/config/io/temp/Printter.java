package org.mulinlab.varnote.config.io.temp;

import htsjdk.tribble.util.LittleEndianOutputStream;
import org.mulinlab.varnote.config.param.output.OutParam;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public abstract class Printter {

    protected OutParam outParam;
    protected String tempFolderPath;
    protected List<ThreadPrintter> threadPrintters;

    protected LittleEndianOutputStream finalOutputStream;

    public Printter(final OutParam outParam) {
        this.outParam = outParam;
    }

    public void mergeFile(List<String> comments) throws IOException {
        printComments(comments);
        mergeResults();
        doEnd();
    }

    public void printComments(List<String> comments) throws IOException {
        for (String comment: comments) {
            finalOutputStream.writeBytes(comment + "\n");
        }
    }

    public abstract void mergeResults() throws IOException;

    public void doEnd() throws IOException {
        if(finalOutputStream != null) finalOutputStream.close();

        for (int i = 0; i < threadPrintters.size(); i++) {
            File tempFile = threadPrintters.get(i).getFile();
            if (!tempFile.delete()) {
                System.err.println("Delete " + tempFile.getAbsolutePath() + " is failed.");
            }
        }
        if (!new File(tempFolderPath).delete()) {
            System.err.println("Delete " + tempFolderPath + " is failed.");
        } else {
//			log.printKVKCYNSystem("Delete temp folder", tempFolderPath);
        }
    }

    public void init() {
        makeTemp();
    }

    public void makeTemp() {
        File tempFolder =  new File(new File(this.outParam.getOutputPath()).getParent() + File.separator + "temp");
        if (!tempFolder.exists()) {
//			log.printKVKCYNSystem("Creating temp folder", tempFolder.getAbsolutePath());
            try{
                tempFolder.mkdir();
            } catch(SecurityException se) {
//		    	log.printStrWhiteSystem("Creating directory " + tempFolder.getAbsolutePath() + " with error! please check.");
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public abstract void addPrintter(final String path, final Integer index) throws FileNotFoundException;

    public void print(final String result, final int index) throws IOException {
        if(result != null) {
            threadPrintters.get(index).print(result);
        }
    }

    public ThreadPrintter getPrintter(final int i) {
        return threadPrintters.get(i);
    }
}
