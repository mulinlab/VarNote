package org.mulinlab.varnote.utils.headerparser;

import java.util.ArrayList;
import java.util.List;

import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;


public final class BEDHeaderParser {
	public static final String TAB = GlobalParameter.TAB;
	public static final String COMMA = GlobalParameter.COMMA;
	public static final String COL = GlobalParameter.COL;
	
//	public static List<String> readActualHeader(final String path, final Format format, final boolean fromHeaderPath, final FileType fileType) {
//		String line,  header = null, dataLine = null;
//
//		NoFilterIterator lineIterator = new NoFilterIterator(path, fileType);
//
//		while (lineIterator.hasNext()) {
//			line = lineIterator.next().trim();
//
//			if(line.startsWith(format.getCommentIndicator()) || line.equals("")) {
//			} else if(fromHeaderPath || format.isHasHeader() || line.startsWith(GlobalParameter.VCF_HEADER_INDICATOR)){
//				header =  line;
//				break;
//			} else {
//				dataLine =  line;
//				break;
//			}
//		}
//		lineIterator.close();
//
//
//        if((header == null) && (dataLine == null))  throw new InvalidArgumentException("No header or data found in file: " + path);
//
//        final List<String> headerList;
//
////        if(header != null) {
////        		format.setHeader(header);
////        		if(header.startsWith(GlobalParameter.VCF_HEADER_INDICATOR)) {
////        			header = header.substring(GlobalParameter.VCF_HEADER_INDICATOR.length());
////        		}
////        		headerList = parserHeaderComma(header);
////        } else {
//////        		format.setHeader(false);
////        		headerList = parserHeader(dataLine);
////    			for (int i = 0; i < headerList.size(); i++) {
////    				if(format.getColOriginalField(i+1) != null) {
////    					headerList.set(i, format.getColOriginalField(i+1));
////    				} else {
////    					headerList.set(i, AbstractParser.COL + (i+1));
////    				}
////    			}
////        }
//
////		if (headerList.size() < 2) throw new InvalidArgumentException("there are not enough columns present in header of file: " + path);
////		return headerList;
//		return null;
//	}

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
