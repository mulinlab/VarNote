/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mulinlab.varnote.utils.stream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.util.zip.GZIPInputStream;

import htsjdk.samtools.util.RuntimeIOException;

public final class GZInputStream {
	private final static byte NEWLINECHAR = "\n".getBytes()[0];
	private final static int CACHE = 1024;
	private final static int BUFSIZE = 1024 * 1024 * 10;
	private final File file;
	private int threadNum = 1;
	private GZReader[] readers;
	private long pos[];

	public GZInputStream(final String path, final int threadNum)  {
		try {
			this.threadNum = threadNum;
			pos = new long[this.threadNum + 1];
			
			file = new File(path);
	
			final long available = getUncompresedSize(file);
			
			pos[0] = 0;
			for (int i = 1; i < threadNum; i++) {
	            pos[i] = (available / threadNum * i);
	        }
			pos[pos.length - 1] = available;

			ajustPos();
			createReader();

		} catch (Exception e) {
        		throw new RuntimeIOException(e);
		}
	}
	
	 public long getUncompresedSize(File dataFile) throws Exception {
	        RandomAccessFile raf = new RandomAccessFile(dataFile, "r");
	        raf.seek(raf.length() - 4);
	        int b4 = raf.read();
	        int b3 = raf.read();
	        int b2 = raf.read();
	        int b1 = raf.read();
	        long availableUncompressedSize = (b1 << 24) | (b2 << 16) + (b3 << 8) + b4;
	        raf.close();
	        return availableUncompressedSize;
	}
	
	public void createReader() throws Exception {
		readers = new GZReader[this.threadNum];
		for (int i = 0; i < this.threadNum; i++) {
			readers[i] = new GZReader(pos[i], pos[i+1]-1);
		}
	}
	
	public void close() throws IOException {
		for (GZReader reader : readers) {
			reader.close();
		}
	}
	
	public class GZReader {
		private Reader reader;
		private RandomAccessFile rf;
		private long end;
		private StringBuilder sb;
		private CharBuffer rBuffer;
		private char[] newStrByte;
		private char[] tempBs;
		private long nowCur;
		private int fromIndex;
		private boolean isEnd;
		private boolean finalEnd;
		
		public GZReader(final long start, final long end) throws Exception {
//			InputStream is = new BufferedInputStream(new FileInputStream(file));
//            LimitInputStream cis = new LimitInputStream(is, end);
//            cis.skip(start);
            
			rf = new RandomAccessFile(file, "r");
	        reader = new InputStreamReader(new GZIPInputStream(Channels.newInputStream(rf.getChannel())));
	        reader.skip(start);

//	        read = new BufferedReader(new InputStreamReader(new GZIPInputStream(cis, 512, readHeader)));  
	        nowCur = start;
	        this.end = end;
	        rBuffer = CharBuffer.allocate(BUFSIZE);
	        tempBs = new char[0]; // 缓存  
	        isEnd = false;
	        finalEnd = false;
	        sb = new StringBuilder();
	        readBuffer();
		}
		
		public int readBuffer() throws Exception {
			char[] bs = new char[BUFSIZE]; // 每次读取的内容 
			int n = -1; 
			if(isEnd) return n;
			if((n = reader.read(rBuffer)) != -1) {
				nowCur += BUFSIZE;
				int rSize = rBuffer.position();
                rBuffer.rewind();
                rBuffer.get(bs);
                rBuffer.clear();
                newStrByte = bs;
                
                if (null != tempBs && tempBs.length > 0) {
                    int tL = tempBs.length;
                    newStrByte = new char[rSize + tL];
                    System.arraycopy(tempBs, 0, newStrByte, 0, tL);
                    System.arraycopy(bs, 0, newStrByte, tL, rSize);
                }
                
                //BufferedReader
                // 如果当前读取的位数已经比设置的结束位置大的时候，将读取的内容截取到设置的结束位置  
                if (end > 0 && nowCur > end) {
                    // 缓存长度 - 当前已经读取位数 - 最后位数  
                    int l = newStrByte.length - (int) (nowCur - end);
                    newStrByte = substring(newStrByte, 0, l);
                    isEnd = true;
                    return -1;
                }
                fromIndex = 0;
			} 
			return n;
		}

		public String readLine() throws Exception {
			if(finalEnd) return null;
			
			int endIndex = 0, n = 0;
			while ((endIndex = indexOf(newStrByte, fromIndex)) == -1) {
				tempBs = substring(newStrByte, fromIndex, newStrByte.length);
				n = readBuffer();
				if (n == -1)
					break;
			}
			
			if(endIndex != -1) {
				char[] bLine = substring(newStrByte, fromIndex, endIndex);
				sb.append(bLine);
				String line = sb.toString();
				sb.delete(0, sb.length());

				fromIndex = endIndex + 1;
				return line;
			} else {
				finalEnd = true;
				return new String(tempBs, 0, tempBs.length);
			}
		}

		public void close() throws IOException {
			rf.close();
			reader.close();
		}
	}
	
	public GZReader getReader(final int index) {
		return readers[index];
	}
	
	public void ajustPos() throws Exception {

        for (int i = 1; i < threadNum ; i++) {
        	 	RandomAccessFile rf = new RandomAccessFile(file, "r");
        	 	Reader reader = new InputStreamReader(new GZIPInputStream(Channels.newInputStream(rf.getChannel())));
        		reader.skip(pos[i]);
        		
        		long startNum = pos[i];
        		CharBuffer rBuffer = CharBuffer.allocate(CACHE);
        		char[] bs = new char[CACHE];
        		char[] tempBs = new char[0];

			while (reader.read(rBuffer) != -1) {
				int rSize = rBuffer.position();
				rBuffer.rewind();
				rBuffer.get(bs);
				rBuffer.clear();
				char[] newStrByte = bs;
				
				// 如果发现有上次未读完的缓存,则将它加到当前读取的内容前面
				if (null != tempBs) {
					int tL = tempBs.length;
					newStrByte = new char[rSize + tL];
					System.arraycopy(tempBs, 0, newStrByte, 0, tL);
					System.arraycopy(bs, 0, newStrByte, tL, rSize);
				}
				// 获取开始位置之后的第一个换行符
				int endIndex = indexOf(newStrByte, 0);
				if (endIndex != -1) {
					pos[i] = startNum + endIndex + 1;
					break;
				}
				tempBs = substring(newStrByte, 0, newStrByte.length);
				startNum += CACHE;
			}
			rf.close();
			reader.close();
        }
    }
	
    private int indexOf(char[] src, int fromIndex) throws Exception {
        for (int i = fromIndex; i < src.length; i++) {
            if (src[i] == NEWLINECHAR) {
                return i;
            }
        }
        return -1;
    }
	
    private char[] substring(char[] src, int fromIndex, int endIndex) throws Exception {
        int size = endIndex - fromIndex;
        char[] ret = new char[size];
        System.arraycopy(src, fromIndex, ret, 0, size);
        return ret;
    }
    
    public static void main(String args[]) throws FileNotFoundException, IOException {
    		try {
	    			String path = "/Users/hdd/Downloads/test_data/q3.sorted.vcf.gz";
	            BufferedWriter bos = new BufferedWriter(new FileWriter("/Users/hdd/Downloads/test_data/q3.sorted.vcf"));
//	    			
//	            BufferedReader gzReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(path))));
	    			String s;
//	    			while((s = gzReader.readLine()) != null) {
//					System.out.println(s);
//				}
//	    			gzReader.close();
    			
				GZInputStream in = new GZInputStream(path, 4);
				for (int i = 0; i < 4; i++) {
//					for (int j = 0; j < 40; j++) {
//						s = in.getReader(i).readLine();
					while((s = in.getReader(i).readLine()) != null) {
//						System.out.println(s);
						bos.write(s + "\n");
					}
				}
				in.close();
				bos.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
}
