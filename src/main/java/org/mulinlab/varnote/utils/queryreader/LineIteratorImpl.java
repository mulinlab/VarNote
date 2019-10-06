package org.mulinlab.varnote.utils.queryreader;

import htsjdk.samtools.util.AbstractIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.RuntimeIOException;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.tribble.readers.LongLineBufferedReader;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.VannoUtils.FileType;
import org.mulinlab.varnote.utils.gz.MyBlockCompressedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public final class LineIteratorImpl extends AbstractIterator<String> implements LineIterator, Closeable {
    private final MyBlockCompressedInputStream bgzStream;
    private final BufferedReader gzReader;
    private final LongLineBufferedReader reader;

    
    public LineIteratorImpl(final String path, final FileType fileType) throws IOException {
		if (fileType == FileType.BGZ) {
			this.bgzStream = VannoUtils.makeBGZ(path);
			this.gzReader = null;
			this.reader = null;
		} else if (fileType == FileType.GZ) {
			this.bgzStream = null;
			this.gzReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(path))));
			this.reader = null;
		} else {
			this.bgzStream = null;
			this.gzReader = null;
			this.reader = new LongLineBufferedReader(new InputStreamReader(new FileInputStream(path)));
		}
    }

    @Override
    public String advance() {
        try {
        	String line ;
        	if (this.bgzStream != null) {
				line = bgzStream.readLine();
			} else if (this.gzReader != null) {
				line = gzReader.readLine();
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
		if (this.bgzStream != null) {
			CloserUtil.close(bgzStream);
		} else if (this.gzReader != null) {
			CloserUtil.close(gzReader);
		} else {
			CloserUtil.close(reader);
		}
	}
}