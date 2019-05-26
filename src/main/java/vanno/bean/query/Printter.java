package main.java.vanno.bean.query;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


import htsjdk.tribble.util.LittleEndianOutputStream;
import main.java.vanno.index.MyBlockCompressedOutputStream;

public final class Printter {
	private LittleEndianOutputStream writer;
	private File file;
	private final int index;

	public Printter(final String outputPath, final int index, final boolean isGzip) throws FileNotFoundException {
		super();
		this.index = index;
			
		file = new File(outputPath + ".temp" + index);
		if(isGzip) {
			writer = new LittleEndianOutputStream(new MyBlockCompressedOutputStream(file));
		} else {
			writer = new LittleEndianOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		}
	}
	
	public OutputStream getWriter() {
		return this.writer;
	}
	
	public File getFile() {
		return this.file;
	}
	
	public int getIndex() {
		return index;
	}

	public void print(String s) throws IOException {
		this.writer.writeBytes(s + "\n");
	}

	public void tearDownPrintter() {
		if(this.writer != null) {
			try {
				this.writer.flush();
				this.writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}
}
