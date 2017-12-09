package bean.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class Printter {
	private Writer writer;
	private File file;
	private final int index;
//	private PrintSetting setting;

	public Printter(final String outputPath, final int index) {
		super();
//		this.setting = setting;
		this.index = index;
			
		try {
			file = new File(outputPath + ".temp" + index);
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			
//			System.out.println("create temp file: " + file.getAbsolutePath() + " , they will be delete after program end normally.");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public Writer getWriter() {
		return this.writer;
	}
	
	public File getFile() {
		return this.file;
	}
	
	public int getIndex() {
		return index;
	}

	public void print(String s) {
		try {
//			System.out.println("hit========" + s.substring(0,30));
			this.writer.write(s + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void tearDownPrintter() {
		if(this.writer != null) {
			try {
				this.writer.flush();
				this.writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
}
