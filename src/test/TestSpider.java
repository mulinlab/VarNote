package test;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedInputStream;
import index.interval.IntervalIndex;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import constants.BasicUtils;
import bean.writeIndex.Chunk;
import bean.writeIndex.IntervalIndexFeature;
import mapreduce.Mapper;
import mapreduce.Reducer;
import mapreduce.SimpleMapReduce;
import stream.BZIP2InputStream;
import stream.BZIP2InputStream.Spider;

public class TestSpider {
	
	public static void main(String[] args) {	
	    long t1 = System.currentTimeMillis();
		try {
			
//			final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/mulin/Desktop/linlin/out.vcf")));
//			String s;
//			int i=0;
//			while((s = reader.readLine()) != null) {
//				i++;
//			}
//			reader.close();
//			System.out.println(i);
			
//			int core = 6;
//		    BZIP2InputStream bz2_text = new BZIP2InputStream("/Users/mulin/Desktop/linlin/out.vcf", core);
//	
//		    bz2_text.adjustPos();
//		    bz2_text.creatSpider();
//		    
//		    Reducer<Integer, Mapper<Integer>, Integer> r = new Reducer<Integer, Mapper<Integer>, Integer>() {
//				public Integer doReducer(List<Mapper<Integer>> mappers) {
//					for (Mapper<Integer> mapper : mappers) {
//						System.out.println(mapper.getResult());
//					}		
//					return null;
//				}
//			};
//			
//		    SimpleMapReduce mr = new SimpleMapReduce<Integer, Integer>(core, r);
//		    Spider spider;
//		    for (int i = 0; i < core; i++) {
//		    	spider = bz2_text.spider[i];
//		    	mr.addMapper(new TestMapper<Integer>(spider));
//			}
//		    mr.getResult();
//
//
//			long t2 = System.currentTimeMillis();
//
//			System.out.println("Time:" + (t2 - t1));
//			System.out.println(Runtime.getRuntime().availableProcessors());
			
			
			BlockCompressedInputStream plus = new BlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor("/Users/mulin/Desktop/dbNSFP3.0a.ANN.bgz.plus")));
			int tid;
			while(true) {
				tid = BasicUtils.readInt(plus);  if(tid == IntervalIndex.PLUS_FILE_END) break;
				
				BasicUtils.readInt(plus);
				BasicUtils.readInt(plus);
				
		
				System.out.println(BasicUtils.readLong(plus));

			}
		
			long t2 = System.currentTimeMillis();
			System.out.println("Time:" + (t2 - t1));
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
