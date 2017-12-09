package test;


import htsjdk.tribble.util.ParsingUtils;
import htsjdk.tribble.util.TabixUtils;
import index.interval.IntervalIndex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import bean.config.Printter;
import bean.print.PrintSetting;

public class DBBeanBak {
//	private final String path;
//	private final boolean isTabixFirst;
//	private final String idxFileForPlus;
//	private final String plusFile;
//	private final String idxFileForTabix;
//	private final boolean isExact;
//	private final File output;
//	private final List<Printter> printters;
//	private final String useIndex;
//	
//	public DBBeanBak(final String path, final boolean exact, final boolean isTabixFirst, final String mode, final String inputName, final File outFolder) {
//		super();
//		this.path = path;
//		this.isExact = exact;
//		this.isTabixFirst = isTabixFirst;
//
//		File f = new File(ParsingUtils.appendToPath(path, TabixUtils.STANDARD_INDEX_EXTENSION));
//		if(f.exists()) {
//			idxFileForTabix = f.getPath();
//		} else {
//			idxFileForTabix = null;
//		}
//
//		File plusf = new File(ParsingUtils.appendToPath(path, IntervalIndex.PLUS_EXTENSION));
//		f = new File(ParsingUtils.appendToPath(path, IntervalIndex.PLUS_INDEX_EXTENSION));
//		
//		if(f.exists()) {
//			idxFileForPlus = f.getPath();
//		} else {
//			idxFileForPlus = null;
//		}
//		
//		if(plusf.exists()) {
//			plusFile = plusf.getPath();
//		} else {
//			plusFile = null;
//		}
//		
//		
//		String outputPath = outFolder.getAbsolutePath() + "/" + inputName + "_" + new File(path).getName()  + "_" + mode;
//		if(isExact) {
//			outputPath = outputPath + "_exact" ;
//		}
//		
//		 
//		if(isTabixFirst) {
//			if(idxFileForTabix == null) {
//				if(idxFileForPlus == null) {
//					throw new IllegalArgumentException("We support two types of index file. The first is the tabix index file, the second type needs plus file and plus index file both. Please put the index in the same folder with the database file!");
//				} else {
//					if(plusFile == null) {
//						throw new IllegalArgumentException("you must have plus file and plus index file both!");
//					} else {
//						System.out.println("You set use tabix index first, but we don't find the tabix index. So we use plus index. Mode=" + mode + ", Exact=" + exact );
//						useIndex = idxFileForPlus;
//					}
//				}
//			} else {
//				System.out.println("Tabix index is used. Mode=" + mode + ", Exact=" + exact );
//				useIndex = idxFileForTabix;
//			}
//		} else {
//			if(idxFileForPlus == null) {
//				if(idxFileForTabix == null) {
//					throw new IllegalArgumentException("We support two types of index file. The first is the tabix index file, the second type needs plus file and plus index file both. Please put the index in the same folder with the database file!");
//				} else {
//					System.out.println("You set use my index first, but we don't find the plus index. So we use tabix index. Mode=" + mode + ", Exact=" + exact );
//					useIndex = idxFileForTabix;
//				}
//			} else {
//				if(plusFile == null) {
//					throw new IllegalArgumentException("you must have plus file and plus index file both!");
//				} else {
//					System.out.println("My index is used. Mode=" + mode + ", Exact=" + exact );
//					useIndex = idxFileForPlus;
//				}
//			}
//		}
//		
//		if(useIndex == idxFileForPlus) {
//			outputPath = outputPath + "_my"  ;
//		} else {
//			outputPath = outputPath + "_tabix"  ;
//		}
//		
//		output = new File(outputPath);
//		printters = new ArrayList<Printter>();
//	}
//	
//	public String getUseIndex() {
//		return useIndex;
//	}
//
//	public void addPritter(final PrintSetting setting, int coreIndex) {
//		printters.add(new Printter(setting, output.getAbsolutePath(), coreIndex));
//	}
//	
//	public void printResults() {
//		  WritableByteChannel outChannel = null;
//		  ReadableByteChannel inChannel = null;
//		  ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//		  try {
//			  	outChannel = Channels.newChannel(new FileOutputStream(output));
//				for (int i = 0; i < printters.size(); i++) {
//					printters.get(i).tearDownPrintter();
//					File tempFile = printters.get(i).getFile();
//					 if(tempFile != null) {
//						 inChannel = Channels.newChannel(new FileInputStream(tempFile));
//			
//						 while (inChannel.read(byteBuffer) > 0) {
//							 byteBuffer.flip();
//							 outChannel.write(byteBuffer);
//							 byteBuffer.clear(); 
//						 }
////						 outChannel.write(ByteBuffer.wrap(new String("\n").getBytes()));
//						 inChannel.close();
//			    		 if(tempFile.delete()){
//			    			
//			    		 } else{
//			    			 System.err.println("Delete " + tempFile.getAbsolutePath() + " is failed.");
//			    		 }
//					 }									
//			   }
//			   outChannel.close();
//				
//		  } catch (IOException e) {
//				System.err.println("create output file with error");
//				e.printStackTrace();
//		  }	 
//	}
//	
//	public File getResult(int i) {
//		printters.get(i).tearDownPrintter();
//		return printters.get(i).getFile();
//	}
//	
//	public Printter getPrintter(int i) {
//		return printters.get(i);
//	}
//
//	public void print(String s, int index) {
//		printters.get(index).print(s);
//	}
//	
//	public String getPath() {
//		return path;
//	}
//
//	public String getIdxFileForPlus() {
//		return idxFileForPlus;
//	}
//
//	public String getIdxFileForTabix() {
//		return idxFileForTabix;
//	}
//
//	public boolean hasTabixIndex() {
//		return idxFileForTabix != null;
//	}
//	
//	public boolean hasMyIndex() {
//		return idxFileForPlus != null;
//	}
//
//	public boolean isTabixFirst() {
//		return isTabixFirst;
//	}
//
//	public String getPlusFile() {
//		return plusFile;
//	}
//
//	public boolean isExact() {
//		return isExact;
//	}
//
//	public File getOutput() {
//		return output;
//	}
}
