package postanno;

import htsjdk.samtools.util.AbstractIterator;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.RuntimeIOException;
import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.tribble.readers.LongLineBufferedReader;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import constants.BasicUtils;

public class LineIteratorImpl extends AbstractIterator<String> implements LineIterator, Closeable {
    private final BlockCompressedInputStream gzStream;
    private final LongLineBufferedReader reader;

    public LineIteratorImpl(final File file) throws FileNotFoundException {
    	if (AbstractFeatureReader.hasBlockCompressedExtension(file)) {
    		 this.gzStream = BasicUtils.checkStream(file);
    		 this.reader = null;
    	} else {
    		this.reader = new LongLineBufferedReader(new InputStreamReader(new FileInputStream(file)));
    		this.gzStream = null;
    	}
       
    }

    @Override
    protected String advance() {
        try {
        	String line ;
        	if(this.gzStream != null) {
        		line = gzStream.readLine();
        	} else {
        		line = reader.readLine();
        	}
//        	System.out.println(line);
            return line;
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    @Override
    public void close() throws IOException {
    	if(this.gzStream != null) {
    		 CloserUtil.close(gzStream);
    	} else {
    		 CloserUtil.close(reader);
    	}
    }
}