/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mulinlab.varnote.utils.stream;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

/**
 *
 * @author MX Li
 */
public class LocalFile {

    /**
     * retrieve data from a text file whith limited rows
     *
     * @param FileName
     * @param indices
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean retrieveData(String fileName, List<String[]> arry,
            int limitedRowNumber, String delimi, String startLabel, boolean useTokenizer) throws Exception {
        File file = new File(fileName);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
        } catch (ZipException ex) {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        }
        String line = "";
        String[] row = null;
        int lineNumber = 0;
        String delmilit = "\t\" \"\n,";
        if (delimi != null) {
            delmilit = delimi;
            //usually some files donot start with data but with breif annoation, so we need filter the latter.
        }
        if (startLabel != null) {
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith(startLabel)) {
                    break;
                }
            }
        }

        if (useTokenizer) {
            int colNum = -1;
            int i;
            StringBuilder tmpStr = new StringBuilder();
            do {
                if (line.trim().length() == 0) {
                    continue;
                }
                StringTokenizer tokenizer = new StringTokenizer(line, delmilit);
                if (colNum < 0) {
                    colNum = tokenizer.countTokens();
                }
                row = new String[colNum];
                for (i = 0; i < colNum; i++) {
                    //sometimes tokenizer.nextToken() can not release memory
                    row[i] = tmpStr.append(tokenizer.nextToken().trim()).toString();
                    tmpStr.delete(0, tmpStr.length());
                }
                arry.add(row);

                lineNumber++;
                if (lineNumber > limitedRowNumber) {
                    break;
                }
            } while ((line = br.readLine()) != null);
        } else {
            if (delmilit.equals("\t\" \"\n")) {
                delmilit = "[" + delmilit + "]";
            }
            do {
                if (line.trim().length() == 0) {
                    continue;
                }
                arry.add(line.split(delmilit, -1));
                lineNumber++;
                if (lineNumber > limitedRowNumber) {
                    break;
                }
            } while ((line = br.readLine()) != null);
        }
        br.close();
        return true;
    }

    /**
     * simply retrieve data from a file
     *
     * @param FileName
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean retrieveData(String fileName, List<String[]> arry, String delimiter) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = null;
        String delmilit = "\t\" \"\n";
        if (delimiter != null) {
            delmilit = delimiter;        //usually some files donot start with data but with breif annoation, so we need filter the latter.

        }
        int colNum = -1;
        String[] row = null;
        StringBuilder tmpStr = new StringBuilder();
        while ((line = br.readLine()) != null) {
            if (line.trim().length() == 0) {
                continue;
            }
            StringTokenizer tokenizer = new StringTokenizer(line, delmilit);
            if (colNum < 0) {
                colNum = tokenizer.countTokens();
            }
            row = new String[colNum];
            for (int i = 0; i < colNum; i++) {
                //sometimes tokenizer.nextToken() can not release memory
                row[i] = tmpStr.append(tokenizer.nextToken().trim()).toString();
                tmpStr.delete(0, tmpStr.length());
            }
            arry.add(row);
        }
        br.close();
        return true;
    }

    static public boolean retrieveData(String fileName, Map<String, String> arry, String delimiter, int keyIndex, int valueIndex) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = null;
        String delmilit = "\t\" \"\n";
        if (delimiter != null) {
            delmilit = delimiter;        //usually some files donot start with data but with breif annoation, so we need filter the latter. 
        }

        String[] row = null;
        StringBuilder tmpStr1 = new StringBuilder();
        StringBuilder tmpStr2 = new StringBuilder();
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            row = line.split(delmilit);
            tmpStr1.append(row[keyIndex]);
            tmpStr2.append(row[valueIndex]);
            arry.put(tmpStr1.toString(), tmpStr1.toString());
            tmpStr1.delete(0, tmpStr1.length());
            tmpStr2.delete(0, tmpStr2.length());
        }
        br.close();
        return true;
    }

    /**
     * simply retrieve data from a file
     *
     * @param FileName
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean retrieveData(String fileName, StringBuilder tmpBf) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = null;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            tmpBf.append(line);
            tmpBf.append('\n');
        }
        br.close();
        return true;
    }

    /**
     * simply retrieve data from a file
     *
     * @param FileName
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean retrieveData(String fileName, Set<String> arry) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = null;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.length() > 1) {
                arry.add(line);
            }
        }
        br.close();
        return true;
    }

    /**
     * simply retrieve data from a file
     *
     * @param FileName
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean retrieveData(String fileName, Set<String> arry, int index) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = null;
        int i = 0;
        while ((line = br.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            for (i = 0; i < index; i++) {
                tokenizer.nextToken();
            }
            arry.add(tokenizer.nextToken());
        }
        br.close();
        return true;
    }

    /**
     * retrieve data from a text file it is based on split,
     *
     * @param FileName
     * @param indices
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean retrieveData(String fileName, List<String[]> arry, int[] orgIndices,
            Set<String> refList, int[] refIndexes, String delimiter) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = null;
        String[] cells = null;
        String[] row = null;
        int selectedColNum = orgIndices.length;

        int i;
        boolean contain = false;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.trim().length() == 0) {
                continue;
            }
            cells = line.split(delimiter, -1);
            contain = true;
            for (int j = 0; j < refIndexes.length; j++) {
                if (!refList.contains(cells[refIndexes[j]])) {
                    contain = false;
                    break;
                }
            }

            if (contain) {
                row = new String[selectedColNum];
                for (i = 0; i < selectedColNum; i++) {
                    row[i] = cells[orgIndices[i]];
                }
                arry.add(row);
            }
        }
        br.close();
        return true;
    }

    /**
     * retrieve data from a text file it is based on tokenizor, but the order in
     * the indices will be changed to the order of the files. it is a
     * consideration of speed.
     *
     * @param FileName
     * @param indices
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean retrieveData(String fileName, List<String[]> arry, int[] orgIndices,
            String delimiter) throws Exception {
        File dataFile = new File(fileName);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(dataFile))));
        } catch (ZipException ex) {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile)));
        }
        String line = null;
        String[] cells = null;
        String[] row = null;
        int selectedColNum = orgIndices.length;
        int i;
        String finalDelmiliter = "\t\" \"\n";
        if (delimiter != null) {
            finalDelmiliter = delimiter;        //usually some files donot start with data but with breif annoation, so we need filter the latter. 
        }
        //ignore the head
        br.readLine();
        while ((line = br.readLine()) != null) {
            //line = line.trim();
            if (line.trim().length() == 0) {
                continue;
            }
            cells = line.split(finalDelmiliter, -1);
            row = new String[selectedColNum];
            for (i = 0; i < selectedColNum; i++) {
                row[i] = cells[orgIndices[i]];
            }
            arry.add(row);
        }
        br.close();
        return true;
    }

    /**
     * retrieve data from a text file it is based on tokenizor, but the order in
     * the indices will be changed to the order of the files. it is a
     * consideration of speed.
     *
     * @param FileName
     * @param indices
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public StringBuilder retrieveData(String fileName, int skipLine) throws Exception {
        File dataFile = new File(fileName);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(dataFile))));
        } catch (ZipException ex) {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile)));
        }

        String line = null;
        StringBuilder content = new StringBuilder();

        int i = 0;
        while (i < skipLine) {
            br.readLine();
            i++;
        }
        while ((line = br.readLine()) != null) {
            //line = line.trim();
            if (line.trim().length() == 0) {
                continue;
            }
            content.append(line.trim());
        }
        br.close();
        return content;
    }

    /**
     * retrieve data from a text file it is based on split,
     *
     * @param FileName
     * @param indices
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean retrieveData(String fileName, ArrayList<String[]> arry, int[] orgIndices,
            String[] refList, int refIndex, String delimiter) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = null;
        String[] cells = null;
        String[] row = null;
        int selectedColNum = orgIndices.length;
        int i, pos;
        Arrays.sort(refList);
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.trim().length() == 0) {
                continue;
            }
            cells = line.split(delimiter, -1);
            pos = Arrays.binarySearch(refList, cells[refIndex]);
            if (pos >= 0) {
                row = new String[selectedColNum];
                for (i = 0; i < selectedColNum; i++) {
                    row[i] = cells[orgIndices[i]];
                }
                arry.add(row);
            }
        }
        br.close();
        return true;
    }

    /**
     * write data to a text file
     *
     * @param FileName
     * @param indices
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean writeObject2Text(String fileName, List<Object[]> arry, String delmilit) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        Object[] linecells = null;
        int linenumber = arry.size();
        int cols = 0;
        for (int i = 0; i < linenumber; i++) {
            linecells = arry.get(i);
            cols = linecells.length - 1;
            for (int j = 0; j < cols; j++) {
                if (linecells[j] == null) {
                    bw.write(" ");
                } else {
                    bw.write(linecells[j].toString());
                }
                bw.write("\t");
            }
            if (linecells[cols] == null) {
                bw.write(" ");
            } else {
                bw.write(linecells[cols].toString());
            }
            bw.write("\n");

        }
        bw.flush();
        bw.close();
        return true;
    }

    /**
     * write data to a text file
     *
     * @param FileName
     * @param indices
     * @param arry
     * @throws java.lang.Exception
     * @return
     */
    static public boolean writeData(String fileName, List<String[]> arry, String delmilit, boolean append) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, append));
        String[] linecells = null;
        int linenumber = arry.size();
        if (linenumber == 0) {
        		bw.close();
            return false;
        }
        int cols = 0;
        cols = arry.get(0).length - 1;
        // System.out.println(cols);
        for (int i = 0; i < linenumber; i++) {
            linecells = arry.get(i);

            for (int j = 0; j < cols; j++) {
                if (linecells[j] == null) {
                    bw.write(".");
                } else {
                    bw.write(linecells[j]);
                }
                bw.write(delmilit);
            }

            if (linecells[cols] == null) {
                bw.write(".");
            } else {
                bw.write(linecells[cols]);
            }

            bw.write("\n");
        }
        bw.close();
        return true;
    }

    static public boolean writeData(BufferedWriter bw, List<String[]> arry, String delmilit) throws Exception {
        String[] linecells = null;
        int linenumber = arry.size();
        if (linenumber == 0) {
            return false;
        }
        int cols = 0;
        cols = arry.get(0).length - 1;
        // System.out.println(cols);
        for (int i = 0; i < linenumber; i++) {
            linecells = arry.get(i);

            //  cols = arry.get(i).length - 1;
            for (int j = 0; j < cols; j++) {
                if (linecells[j] == null) {
                    bw.write(".");
                } else {
                    bw.write(linecells[j]);
                }
                bw.write(delmilit);
            }

            if (linecells[cols] == null) {
                bw.write(".");
            } else {
                bw.write(linecells[cols]);
            }

            bw.write("\n");
        }

        return true;
    }

    public static void main(String[] args) {
//        try {
//            String inFile = "/psychipc01/disk2/references/1000Genome/release/20130502_v5a/ALL.chr1.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz";
//            String outFile = "/psychipc01/disk2/references/1000Genome/release/20130502_v5a/ALL.chr1.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes1.vcf.gz";
//            BlockCompressedInputStream br = new BlockCompressedInputStream(new File(inFile));
//            BlockCompressedOutputStream bw = new BlockCompressedOutputStream(new File(outFile));
//            String line = null;
//            String[] cells = null;
//
//            int[] orgIndices = new int[]{0, 1, 2, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 25, 33, 34, 35, 36, 37, 38, 39, 40};
//            int selectedColNum = orgIndices.length;
//            int i, pos;
//            String delimiter = "\t";
//
//            while ((line = br.readLine()) != null) {
//                line = line.trim();
//                if (line.trim().length() == 0) {
//                    continue;
//                }
//
//                bw.write(line.replaceAll("[|]", "/").getBytes());
//                bw.write("\n".getBytes());
//            }
//            bw.close();
//            br.close();
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }

    }
}
