package bean.config;


import java.util.HashMap;
import java.util.Map;
import htsjdk.tribble.TribbleException;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.variant.vcf.VCFConstants;
import htsjdk.variant.vcf.VCFHeader;


public final class BEDHeaderParser {

	public static Map<String, Integer> readActualHeader(final LineIterator lineIterator, final boolean isHeader) {
		String line;
		while (lineIterator.hasNext()) {
			line = lineIterator.peek();
			if (line.startsWith(VCFHeader.METADATA_INDICATOR)) {
				continue;
			} else if (line.startsWith(VCFHeader.HEADER_INDICATOR)) {
				return parserHeader(line.substring(1));
			} else {
				if (isHeader) {
					return parserHeader(line);
				} else {
					return null;
				}
			}
		}
		return null;
	}

	public static Map<String, Integer> parserHeader(final String header) {
		final Map<String, Integer> headerMap = new HashMap<String, Integer>();
		String[] strings = header.split(VCFConstants.FIELD_SEPARATOR);

		if (strings.length < 2)
			throw new TribbleException.InvalidHeader(
					"there are not enough columns present in the header line: " + header);

		for (int i = 0; i < strings.length; i++) {
			headerMap.put(strings[i], i);
		}

		return headerMap;
	}
}
