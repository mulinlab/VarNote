package test;
//
//
//import htsjdk.samtools.seekablestream.ISeekableStreamFactory;
//import htsjdk.samtools.seekablestream.SeekableStream;
//import htsjdk.samtools.seekablestream.SeekableStreamFactory;
//import htsjdk.samtools.util.BlockCompressedInputStream;
//import htsjdk.samtools.util.StringUtil;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//
public class ConvertInfoField {
//
//	public ConvertInfoField() {
//	}
//	
//	@SuppressWarnings("resource")
//	public void doConvert(File file, int[] fieldCols, String[] names, int processColomn) {
//		if(processColomn < 1) {
//			throw new IllegalArgumentException("Colomn should not less than 1.");
//		}
//		
//		ExtracInfo processor = new ExtracInfo(fieldCols, names);
//		processColomn = processColomn - 1;
//		BufferedWriter write = null;
//		
//		try {
//			String s;
//			String[] tokens;
//		    
//			final ISeekableStreamFactory ssf = SeekableStreamFactory.getInstance();
//	        final SeekableStream seekableStream =
//	                    ssf.getBufferedStream(ssf.getStreamFor(file.getAbsolutePath()));
//	        BlockCompressedInputStream is = new BlockCompressedInputStream(seekableStream);
//			
//			write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(file.getAbsolutePath() + ".cvt"))));
//			while((s = is.readLine()) != null) {
//				tokens = s.split(FIELDS_SEPARATOR);
//				
//				if(processColomn >= tokens.length) {
//					throw new IllegalArgumentException("Colomn should not larger than " + tokens.length + ".");
//				}
//				
//				tokens[processColomn] = processor.processInfo(tokens[processColomn]);
//				write.write(StringUtil.join(FIELDS_SEPARATOR, tokens) + "\n");
//			}
//			is.close();
//			write.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	public static void main(String[] args) {
//		String path = "./AF.ANN.bgz";
//		ConvertInfoField convertor = new ConvertInfoField();
//		convertor.doConvert(new File(path), new int[]{2,3,4}, new String[]{"OneKG_AFR_AF", "OneKG_AMR_AF", "OneKG_EAS_AF"}, 6);
//	}
}
