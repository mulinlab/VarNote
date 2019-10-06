package org.mulinlab.varnote.config.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.tribble.util.LittleEndianOutputStream;

public class PrintGZ {
	private final LittleEndianOutputStream gzOut;
	private final BufferedWriter nonGZWriter;
	
	public PrintGZ(final String path, final boolean isGzip) throws FileNotFoundException {
		 if(isGzip) {
			 gzOut = new LittleEndianOutputStream(new BlockCompressedOutputStream(new File(path)));
			 nonGZWriter = null;
		 } else {
			 gzOut = null;
			 nonGZWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path))));
		 }
	}
	
	public void writeLine(final String s) throws IOException {
		if(gzOut != null) {
			gzOut.writeBytes(s + "\n");
		} else if(nonGZWriter != null) {
			nonGZWriter.write( s + "\n");
		}
	}
	
	public void close() throws IOException {
		if(gzOut != null) {
			gzOut.close();
		} else if(nonGZWriter != null) {
			nonGZWriter.close();
		}
	}

	public LittleEndianOutputStream getGzOut() {
		return gzOut;
	}

	public BufferedWriter getNonGZWriter() {
		return nonGZWriter;
	}
}
