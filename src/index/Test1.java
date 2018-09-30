package index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import readers.AbstractReader;

public class Test1 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			long t1 = System.currentTimeMillis();
			
			MyBlockCompressedInputStream input1 = new MyBlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor("/Users/hdd/Downloads/dandan/data/linlin2.vcf_MIX_out/AF.ANN.bgz.bgz")));
			MyBlockCompressedInputStream input2 = new MyBlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor("/Users/hdd/Downloads/dandan/data/AF.ANN.bgz")));
			BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("/Users/hdd/Downloads/dandan/data/linlin2.vcf_MIX_out/AF.ANN.bgz.result"))));
			long f;
			while( (f = AbstractReader.readLong(input1)) > 0) {
				input2.seek(f);

				outWriter.write(input2.readLine() + "\n");
			}
			outWriter.flush();
			outWriter.close();
			
			input1.close();
			input2.close();
			
			 long t2 = System.currentTimeMillis();
			 System.out.println("Time:" + (t2 - t1));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
