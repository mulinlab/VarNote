/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

/**
 *
 * @author mxli
 */
public class ByteInputStream {

    File fleInput = null;
    int intBufferSize = 1024 * 1024;
    InputStream inputStream = null;

    int intRead = -1;
    int intCurrPos = 0;
    byte[] bytBuffer = null;
    int intTempBufferLengthACC = 0;
    byte[] bytTempBuffer = null;
    int intLineStart = 0;
    int intCol = -1;
//    int intDelPosMarker=0;
    //'\t'
    byte bytDelimiter = 9;

    public ByteInputStream(File fleInput, int intBufferSize, char chrDelimiter) throws IOException {
        this.fleInput = fleInput;
        this.intBufferSize = intBufferSize;
        this.inputStream = getInputStream(fleInput);
        this.bytBuffer = new byte[intBufferSize];
        this.bytTempBuffer = new byte[intBufferSize];
        bytDelimiter = (byte) chrDelimiter;
    }

    private InputStream getInputStream(File fleInput) throws IOException {
        String strSuffix = fleInput.getName().toLowerCase();
        if (strSuffix.endsWith("gz")) {
            // inputStream = new BufferedInputStream(new GZIPInputStream(new FileInputStream(fleInput)),1024 * 1024 * 100);
            inputStream = new GZIPInputStream(new FileInputStream(fleInput), 1024 * 1024 * 100);
            //  inputStream=new GZIPInputStream(Channels.newInputStream(new RandomAccessFile(fleInput, "r").getChannel()),1024 * 1024 * 100);

        } else if (strSuffix.endsWith("zip")) {
            inputStream = new BufferedInputStream(new ZipInputStream(new FileInputStream(fleInput)), 1024 * 1024 * 100);
        } else {
            inputStream = new BufferedInputStream(new FileInputStream(fleInput), 1024 * 1024 * 100);
        }
        return inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    //
    public synchronized byte[] readLine(int intDelPos[]) throws IOException {
        int intDelPosMarker = 0;
        intDelPos[intDelPosMarker] = 0;
        int len = intDelPos.length;
        do {
            if (intRead == -1) {
                intRead = inputStream.read(bytBuffer);
                //  System.out.println(intRead);
                if (intRead == -1) {
                    return null;
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
                intRead = this.inputStream.read(bytBuffer);
                //System.out.println(intRead);
                if (intRead == -1) {
                    return null;
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

    public static void main(String[] args) {
        try {
            int runs = 1000;
            String val = "" + Math.PI;
            // val = "0.923423423423444334343444323432e+9";
            // System.out.println(Util.parseFloat(val));
            long start = System.nanoTime();
            long start2, time2;

            //BufferedReader br = LocalFileFunc.getBufferedReader("SCZ_raw_redo_snp_recal_indel_recal.vcf.gz");
            // BufferedReader br = LocalFileFunc.getBufferedReader("SCZ_raw_redo_snp_recal_indel_recal.vcf.gz");
            // BufferedReader br = LocalFileFunc.getBufferedReader( "E:\\home\\mxli\\MyJava\\kggseq4\\resources\\hg19\\1kg\\p3v5\\EAS\\1kg.phase3.v5.shapeit2.eas.hg19.chr1.vcf.gz");
            // String fileName = "SCZ_raw_redo_snp_recal_indel_recal.vcf.gz";
            // String fileName = "test1.log";
            //String fileName = "E:\\home\\mxli\\MyJava\\kggseq3\\resources\\hg19\\1kg\\p3v5\\EAS\\1kg.phase3.v5.shapeit2.eas.hg19.chr11.vcf.gz";
            // String fileName = "E:\\home\\mxli\\MyJava\\kggseq3\\resources\\hg19\\hg19_mdbNSFP3.0.chr1.gz";
             String fileName = "1kgafreur.20150813.flt.vcf.gz";
            File file = new File(fileName);
            ByteInputStream br = new ByteInputStream(file, 1024 * 1024, '\t');

            String line;
            start2 = System.nanoTime();
            byte[] tmpByte;
            int[] tmpIndex = new int[3000];
            String tmpStr;
            int rowNum = 0;
            int len = 0;
            byte[] sByte;

            while ((tmpByte = br.readLine(tmpIndex)) != null) {
                rowNum++;
                len = tmpIndex[0] - 1;
 for (int i = 1; i < len; i++) {
                    sByte = Arrays.copyOfRange(tmpByte, i == 0 ? 0 : tmpIndex[i] + 1, tmpIndex[i + 1]);
                    if (tmpByte[100] == 1 && tmpByte[102] == 1) {

                    }
                    //sByte = new byte[tmpIndex[i + 1] - (i == 0 ? 0 : tmpIndex[i] + 1)];
                    //sByte = new byte[tmpIndex[i + 1] - tmpIndex[i] - 1];
                    // System.arraycopy(tmpByte, tmpIndex[i] + 1, sByte, 0, sByte.length);
                    //  tmpStr = new String(sByte);

                    /*
                     if (tmpStr.equals("NA06984")) {
                     int sss = 0;
                     }
                     System.out.print(tmpStr);
                     System.out.print("\t");
                     */
                }
               
                // System.out.println();

            }
            br.getInputStream().close();
            //  br.close();
            //  System.out.println(same + " out of " + all);
            time2 = (System.nanoTime() - start2) / runs;
            System.out.println("File Parsing " + time2 + " ns.  " + rowNum);

            /*
             //   br = LocalFileFunc.getBufferedReader("SCZ_raw_redo_snp_recal_indel_recal.vcf.gz");
             br = LocalFileFunc.getBufferedReader(fileName);
             start2 = System.nanoTime();
             while ((line = br.readLine()) != null) {
             // Util.learningTokenize1(line, '\t', fixedLen[0]);
             Util.tokenize(line, '\t');
             }
             br.close();
             time2 = (System.nanoTime() - start2) / runs;
             System.out.println("File Parsing 1 " + time2 + " ns.");
             */
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
