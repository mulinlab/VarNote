package main.java.vanno.bean.format;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import main.java.vanno.bean.config.anno.ab.AbstractParser;
import main.java.vanno.bean.query.LineIteratorImpl;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils.FileType;


public final class BEDHeaderParser {
	public static final String TAB = BasicUtils.TAB;
	public static final String COMMA = BasicUtils.COMMA;
	public static final String COL = BasicUtils.COL;
	
	public static List<String> readActualHeader(final String path, final Format format, final boolean fromHeaderPath, final FileType fileType) {
		String line,  header = null, dataLine = null;
        try {
			LineIteratorImpl lineIterator = new LineIteratorImpl(path, true, fileType);
			
			while (lineIterator.hasNext()) {
				line = lineIterator.next().trim();

				if(line.startsWith(format.getCommentIndicator()) || line.equals("")) {
				} else if(fromHeaderPath || format.isHasHeader() || line.startsWith(Format.VCF_HEADER_INDICATOR)){
					header =  line;
					break;
				} else {
					dataLine =  line;
					break;
				}
			}
			lineIterator.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        if((header == null) && (dataLine == null))  throw new InvalidArgumentException("No header or data found in file: " + path);
		
        final List<String> headerList;
     
        if(header != null) {
        		format.setHeader(header);
        		if(header.startsWith(Format.VCF_HEADER_INDICATOR)) {
        			header = header.substring(Format.VCF_HEADER_INDICATOR.length());
        		}
        		headerList = parserHeaderComma(header);
        } else {
        		format.setHasHeaderInFile(false);
        		headerList = parserHeader(dataLine);
    			for (int i = 0; i < headerList.size(); i++) {
    				if(format.getColOriginalField(i+1) != null) {
    					headerList.set(i, format.getColOriginalField(i+1));
    				} else {
    					headerList.set(i, AbstractParser.COL + (i+1));
    				}
    			}
        }

		if (headerList.size() < 2) throw new InvalidArgumentException("there are not enough columns present in header of file: " + path);
		return headerList;
	}

	public static List<String> parserHeader(final String header) {
		final List<String> headerList = new ArrayList<String>();
		String[] strings = header.split(TAB);

		if (strings.length < 2)
			throw new InvalidArgumentException("there are not enough columns present in the header line: " + header);

		for (int i = 0; i < strings.length; i++) {
			headerList.add(strings[i].trim());
		}
		return headerList;
	}
	
	public static List<String> parserHeaderComma(final String header) {
		final List<String> headerList = new ArrayList<String>();
		String[] strings = header.split(COMMA);
		if(strings.length < 2) {
			strings = header.split(TAB);
			
			if (strings.length < 2)
				throw new InvalidArgumentException("there are not enough columns present in the header line: " + header);
		}
		
		for (int i = 0; i < strings.length; i++) {
			headerList.add(strings[i].trim());
		}

		return headerList;
	}

	public static List<String> splitString(String val) {
		List<String> list = new ArrayList<String>();
		for (String str : val.split(COMMA)) {
			list.add(str.trim());
		}
		return list; 
	}
	
	public static List<Integer> splitInteger(String val) {
		List<Integer> list = new ArrayList<Integer>();
		for (String str : val.split(COMMA)) {
			list.add(Integer.parseInt(str.trim()));
		}
		return list; 
	}
}
