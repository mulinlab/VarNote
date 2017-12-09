/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;


/**
 *
 *
 */
public class BZIP2InputStream {

    File input;
    int threadNum = 1;
    long pos[];
    RandomAccessFile raf;
    static final int BUF_SIZE = 1 * 1024 * 1024;
    byte[] buf = new byte[BUF_SIZE];
    boolean notBGZF = false;
    ////0: bgz; 1: bgz2; 2: txt;  
    int intFileFormat = 0;
    public Spider[] spider;
    int intBlockSize = 0;

    public String getFileName() {
    	return this.input.getName();
    }
    
    public BZIP2InputStream(String strFile, int n_thread) {
        this.input = new File(strFile);
        this.threadNum = n_thread;
        pos = new long[n_thread + 1];
        ////0: bgz; 1: bgz2; 2: txt;  
        if (strFile.endsWith(".gz")) {
            intFileFormat = 0;
        } else if (strFile.endsWith(".bz2")) {
            intFileFormat = 1;
        } else {
            intFileFormat = 2;
        }
    }

    public void adjustPos() throws IOException {
        for (int i = 0; i < threadNum; i++) {
            pos[i] = (input.length() / threadNum * i);
        }

        pos[pos.length - 1] = input.length();
        raf = new RandomAccessFile(input.getCanonicalFile(), "r");
        ////0: bgz; 1: bgz2; 2: txt;      
        if (intFileFormat == 1) {
            for (int i = 0; i < threadNum; i++) {
                raf.seek(pos[i]);
                boolean boolNoFound = true;
                int n_buf = -1;//if n_buf>0,there is something wrong. 
                int intCount = -1;
                do {
                    intCount = raf.read(buf);
                    n_buf++;
                    if (intCount == -1) {
                        threadNum = 1;
                        pos[i] = pos[pos.length - 1];
                        break;
                    }
                    for (int id = 0; id < intCount - 5; id++) {
                        if (buf[id] == 0x31 && buf[id + 1] == 0x41 && buf[id + 2] == 0x59 && buf[id + 3] == 0x26 && buf[id + 4] == 0x53 && buf[id + 5] == 0x59) {
                            pos[i] += (id + n_buf * buf.length);
                            boolNoFound = false;
                            break;
                        }
                    }
                } while (boolNoFound);
            }
        } else if (intFileFormat == 0) {
            for (int i = 1; i < threadNum; i++) {
                raf.seek(pos[i]);
                boolean boolNoFound = true;
                int n_buf = -1;//if n_buf>0,we can consider this file is not bgzf format, but the gzip format. 
                do {
                    raf.read(buf);
                    n_buf++;
                    if (n_buf > 0) {
                        notBGZF = true;
                        threadNum = 1;
                        pos[1] = pos[pos.length - 1];
                        System.out.println("The file is gzip-format, not bgzip-format!");
                        break;
                    }
                    for (int id = 0; id < buf.length - 1; id++) {
                        if (buf[id] == 31 && buf[id + 1] == -117 && buf[id + 2] == 8 && buf[id + 3] == 4) { //This should be used unsigned number or others. 
                            pos[i] += (id + n_buf * buf.length);
                            boolNoFound = false;
                            break;
                        }
                    }
                } while (boolNoFound);
                if (notBGZF) {
                    break;
                }
            }
        }
        raf.close();

        //For file with small size and many threads. 
        for (int i = 0; i < (threadNum - 1); i++) {
            if (pos[i] == pos[i + 1]) {
                threadNum = 1;
                pos[1] = pos[pos.length - 1];
            }
        }

    }

    public void creatSpider() throws IOException {
        spider = new BZIP2InputStream.Spider[this.threadNum];
        for (int i = 0; i < this.threadNum; i++) {
            spider[i] = new BZIP2InputStream.Spider(i, intFileFormat, this.pos[i], this.pos[i + 1], '\t');
            // System.out.println("Spider" + i + ": created!");
        }
    }

    public int getThreadNum() {
        return threadNum;
    }

    public class Spider {

        int spiderID;
        int intFormat;
        long longRemainSize = -1;
        InputStream inputStream;

        int intRead = -1;
        byte[] bytBuffer = new byte[BUF_SIZE];
        int intLineStart = 0;
        int intCurrPos = 0;
        int intTempBufferLengthACC = 0;
        byte[] bytTempBuffer = new byte[BUF_SIZE];
        byte[] bytStartLine = null;
        byte[] bytEndLine = null;
        byte bytDelimiter;
        long longStart;
        long longEnd;
        
        public long getLongStart() {
			return longStart;
		}

		public long getLongEnd() {
			return longEnd;
		}

		public Spider(int spiderID, int intFormat, long longStart, long longEnd, char chrDelimiter) throws IOException {
            longRemainSize = (longEnd - longStart);
            this.spiderID = spiderID;
            this.intFormat = intFormat;

            this.longStart = longStart;
            this.longEnd = longEnd;
            
            if (intFormat == 1) {//bz2 format
                if (this.spiderID == 0) {
                    InputStream is = new BufferedInputStream(new FileInputStream(input));
                    AdvancedBZip2CompressorInputStream bz0 = new AdvancedBZip2CompressorInputStream(is);
                    bz0.init0();
                    intBlockSize = bz0.getBlockSize100k();
                    bz0.close();
                }
                InputStream is = new BufferedInputStream(new FileInputStream(input));
                LimitInputStream cis = new LimitInputStream(is, longEnd);
                cis.skip(longStart);
                AdvancedBZip2CompressorInputStream bzIn = new AdvancedBZip2CompressorInputStream(cis);
                bzIn.setBlockSize100k(intBlockSize);
                bzIn.init();
                inputStream = bzIn;
            } else if (intFormat == 0) {//txt format
                InputStream is = new BufferedInputStream(new FileInputStream(input));
                LimitInputStream cis = new LimitInputStream(is, longEnd);
                cis.skip(longStart);
                inputStream = new GZIPInputStream(cis);
            } else {
                InputStream is = new BufferedInputStream(new FileInputStream(input));
                LimitInputStream cis = new LimitInputStream(is, longEnd);
                cis.skip(longStart);
                inputStream = cis;
            }

            if (this.spiderID != 0) {
                bytStartLine = readLine();
                spider[this.spiderID - 1].bytEndLine = bytStartLine;
//                System.out.println(new String(bytStartLine));
            }
            bytDelimiter = (byte) chrDelimiter;
        }

        public synchronized byte[] readLine(int intDelPos[]) throws IOException {
            int intDelPosMarker = 0;
            intDelPos[intDelPosMarker] = 0;
            int len = intDelPos.length;
            do {
                if (intRead == -1) {
                    intRead = inputStream.read(bytBuffer);
                    if (intRead == -1) {
                        //The end of the block is not a complete line. 
                        if (intTempBufferLengthACC != 0) {
                            bytBuffer = new byte[intTempBufferLengthACC + (bytEndLine == null ? 0 : bytEndLine.length) + 1];
                            System.arraycopy(bytTempBuffer, 0, bytBuffer, 0, intTempBufferLengthACC);
                            if (bytEndLine != null) {
                                System.arraycopy(this.bytEndLine, 0, bytBuffer, intTempBufferLengthACC, this.bytEndLine.length);
                            }
                            bytBuffer[this.bytBuffer.length - 1] = (byte) '\n';
                            intTempBufferLengthACC = 0;
                            intDelPosMarker = 0;
                        } else {
                            return null;
                        }
                    }
                }

                intLineStart = intCurrPos;
                while (intCurrPos != intRead) {
                    if (bytBuffer[intCurrPos] == bytDelimiter) {
                        ++intDelPosMarker;
                        if (intDelPosMarker < len) {
                            intDelPos[intDelPosMarker] = intCurrPos - intLineStart + intTempBufferLengthACC;
                        }
                    } else if (bytBuffer[intCurrPos] == 10) {
                        //parse the line. 
                        int intLineLength = intCurrPos - intLineStart;//don't contaion \n
                        byte[] bytLine = null;
                        if (intTempBufferLengthACC != 0) {
                            // bytLine = new byte[intTempBufferLengthACC + intLineLength];
                            //System.arraycopy(bytTempBuffer, 0, bytLine, 0, intTempBufferLengthACC);                        
                            bytLine = Arrays.copyOfRange(bytTempBuffer, 0, intTempBufferLengthACC + intLineLength);
                            System.arraycopy(bytBuffer, intLineStart, bytLine, intTempBufferLengthACC, intLineLength);
                            intTempBufferLengthACC = 0;
                        } else {
                            // bytLine = new byte[intLineLength];
                            // System.arraycopy(bytBuffer, intLineStart, bytLine, 0, intLineLength);
                            bytLine = Arrays.copyOfRange(bytBuffer, intLineStart, intCurrPos);
                        }
                        intCurrPos++;
                        if (intCurrPos == intRead) {
                            intRead = -1;
                            intCurrPos = 0;
                        }

                        ++intDelPosMarker;
                        if (intDelPosMarker < len) {
                            //return 13 new line 10
                            //the new line or return line symbol is not included
                            if (intCurrPos > 1 && bytBuffer[intCurrPos - 2] == 13) {
                                intDelPos[intDelPosMarker] = bytLine.length - 1;
                            } else {
                                intDelPos[intDelPosMarker] = bytLine.length;
                            }
                            intDelPos[0] = intDelPosMarker;
                        } else {
                            intDelPos[0] = len;
                        }

                        return bytLine;
                    }
                    intCurrPos++;
                }

                //The buffer ends with imcomplete line. 
                int intTempBufferLength = intCurrPos - intLineStart;
                if ((bytTempBuffer.length - intTempBufferLengthACC) < intTempBufferLength) {
                    bytTempBuffer = Arrays.copyOf(bytTempBuffer, bytTempBuffer.length + intTempBufferLength * 2);
                }
                System.arraycopy(bytBuffer, intLineStart, bytTempBuffer, intTempBufferLengthACC, intTempBufferLength);
                intTempBufferLengthACC += intTempBufferLength;
                intRead = -1;
                intCurrPos = 0;
            } while (intTempBufferLengthACC != 0);

            return null;
        }

        public synchronized byte[] readLine() throws IOException {
            do {
                if (intRead == -1) {
                    intRead = inputStream.read(bytBuffer);
               
                    if (intRead == -1) {
                        //The end of the block is not a complete line. 
                        if (intTempBufferLengthACC != 0) {
                            bytBuffer = new byte[intTempBufferLengthACC + (bytEndLine == null ? 0 : bytEndLine.length) + 1];
                            System.arraycopy(bytTempBuffer, 0, bytBuffer, 0, intTempBufferLengthACC);
                            if (bytEndLine != null) {
                                System.arraycopy(this.bytEndLine, 0, bytBuffer, intTempBufferLengthACC, this.bytEndLine.length);
                            }
                            bytBuffer[this.bytBuffer.length - 1] = (byte) '\n';
                            intTempBufferLengthACC = 0;
                        } else {
                            return null;
                        }
                    }
                }

                intLineStart = intCurrPos;
       
                while (intCurrPos != intRead) {
             
                    if (bytBuffer[intCurrPos] == 10) {
                   
                        //parse the line. 
                        int intLineLength = intCurrPos - intLineStart;//don't contaion \n
                        byte[] bytLine = null;
                        if (intTempBufferLengthACC != 0) {
                            // bytLine = new byte[intTempBufferLengthACC + intLineLength];
                            //System.arraycopy(bytTempBuffer, 0, bytLine, 0, intTempBufferLengthACC);                        
                            bytLine = Arrays.copyOfRange(bytTempBuffer, 0, intTempBufferLengthACC + intLineLength);
                            System.arraycopy(bytBuffer, intLineStart, bytLine, intTempBufferLengthACC, intLineLength);
                            intTempBufferLengthACC = 0;
                        } else {
                            // bytLine = new byte[intLineLength];
                            // System.arraycopy(bytBuffer, intLineStart, bytLine, 0, intLineLength);
                            bytLine = Arrays.copyOfRange(bytBuffer, intLineStart, intCurrPos);
                        }
                        intCurrPos++;
                        if (intCurrPos == intRead) {
                            intRead = -1;
                            intCurrPos = 0;
                        }
                        return bytLine;
                    }
                    intCurrPos++;
                }

                //The buffer ends with imcomplete line. 
                int intTempBufferLength = intCurrPos - intLineStart;
                if ((bytTempBuffer.length - intTempBufferLengthACC) < intTempBufferLength) {
                    bytTempBuffer = Arrays.copyOf(bytTempBuffer, bytTempBuffer.length + intTempBufferLength * 2);
                }
                System.arraycopy(bytBuffer, intLineStart, bytTempBuffer, intTempBufferLengthACC, intTempBufferLength);
                intTempBufferLengthACC += intTempBufferLength;
                intRead = -1;
                intCurrPos = 0;
            } while (intTempBufferLengthACC != 0);

            return null;
        }

        public void closeInputStream() throws IOException {
            inputStream.close();
        }
    }

    public static void main(String args[]) throws FileNotFoundException, IOException {
        String strFile = "D:\\01WORK\\KGGseq\\testdata\\1000Genome\\ALL.chr22.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf";
        BZIP2InputStream bz2 = new BZIP2InputStream(strFile, 6);
        int[] intDelPos = new int[3000];
        bz2.adjustPos();
        bz2.creatSpider();
        int intLine = 0;
        for (int i = 0; i < bz2.threadNum; i++) {
            byte[] bytTemp = bz2.spider[i].readLine(intDelPos);
            while (bytTemp != null) {
//            System.out.println(intLine+++": "+new String(bytTemp));   
//                System.out.println(new String(bytTemp));
                bytTemp = bz2.spider[i].readLine(intDelPos);
                System.out.println(intLine++);
            }
        }
    }
}
