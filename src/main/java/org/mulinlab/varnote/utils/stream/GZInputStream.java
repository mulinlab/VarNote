/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mulinlab.varnote.utils.stream;
import java.io.*;
import java.nio.CharBuffer;
import java.util.zip.GZIPInputStream;

import htsjdk.samtools.util.RuntimeIOException;

public final class GZInputStream {
	private final static byte NEWLINECHAR = "\n".getBytes()[0];
	private final static int CACHE = 1024;
	private final String path;
	private int threadNum = 1;
	private GZReader[] readers;
	private long pos[];

	public GZInputStream(final String path, final int threadNum)  {
		try {
			this.threadNum = threadNum;
			pos = new long[this.threadNum + 1];

			this.path = path;

			final long available = getUncompresedSize(new File(path));
			
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
		private BufferedReader reader;
		private long end;
		private long curr;

		public GZReader(final long start, final long end) throws Exception {
			reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(path))));
			reader.skip(start);
			curr = start;
			this.end = end + 1;
		}

		public String getFilePath() {
			return path;
		}

		public String readLine() throws Exception {
			String s = reader.readLine();
			if(s == null) return null;
			else {
				curr += s.length() + 1;
				if(curr > end) return null;
				else return s;
			}
		}

		public void close() throws IOException {
			reader.close();
		}
	}
	
	public GZReader getReader(final int index) {
		return readers[index];
	}
	
	public void ajustPos() throws Exception {

		InputStreamReader reader;
        for (int i = 1; i < threadNum ; i++) {
			reader = new InputStreamReader(new GZIPInputStream(new FileInputStream(path)));
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
}
