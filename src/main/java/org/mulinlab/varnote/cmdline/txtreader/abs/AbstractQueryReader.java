package org.mulinlab.varnote.cmdline.txtreader.abs;

import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import htsjdk.samtools.util.IOUtil;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.VannoUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractQueryReader<T> implements RunReaderInterface<T> {

	public static final String COMMENT_LINE = GlobalParameter.COMMENT_LINE;
	public static final String OVERLAP_NOTE = GlobalParameter.OVERLAP_NOTE;
	public static final String OVERLAP_EQUAL = GlobalParameter.OVERLAP_EQUAL;
	public static final String END = "end";

	protected Map<String, String> valueHash;

	@Override
	public T read(final String filePath) {
		IOUtil.assertInputIsValid(filePath);
		T obj = null;

		doInit();
		final NoFilterIterator iterator = new NoFilterIterator(filePath, VannoUtils.checkFileType(filePath));
		String line;
		while(iterator.hasNext()) {
			line = iterator.peek().trim();
			if(isBreak(line)) break;
			if(!filterLine(line)) {
				processLine(line);
			}
			iterator.next();
		}
		iterator.close();
		obj = doEnd();

		return obj;
	}

	@Override
	public boolean isBreak(String line) {
		return false;
	}

	public String[] splitLine(String line) {
		if(line.indexOf(OVERLAP_EQUAL) != -1) {
			return line.split(OVERLAP_EQUAL);
		} else if(line.indexOf(':') != -1) {
			return line.split(":");
		} else {
			throw new InvalidArgumentException("Cannot parse line : \"" + line +"\", properties should have a format like key=value or key:value.");
		}
	}

	public void putValue(String key, String val) {
		valueHash.put(key, val);
	}

	@Override
	public void doInit() {
		valueHash = new HashMap<>();
	}

	@Override
	public void processLine(String line) {
		String[] lineSplit = splitLine(line);
		if(lineSplit.length > 1) {
			putValue(lineSplit[0].trim().toLowerCase(), lineSplit[1].trim());
		}
	}

	@Override
	public boolean filterLine(String line) {
		if(line.startsWith(COMMENT_LINE) || line.trim().equals("")) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public T doEnd() {
		return null;
	}
}
