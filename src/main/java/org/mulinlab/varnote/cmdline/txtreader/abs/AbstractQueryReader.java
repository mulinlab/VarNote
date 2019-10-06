package org.mulinlab.varnote.cmdline.txtreader.abs;

import com.intel.gkl.compression.IntelInflaterFactory;
import htsjdk.samtools.util.BlockGunzipper;
import htsjdk.samtools.util.Log;
import htsjdk.samtools.util.zip.InflaterFactory;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.config.param.output.OutParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import htsjdk.samtools.util.IOUtil;
import org.mulinlab.varnote.utils.enumset.OutMode;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.queryreader.LineIteratorImpl;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.VannoUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
		try {
			doInit();
			final LineIteratorImpl reader = new LineIteratorImpl(filePath, VannoUtils.checkFileType(filePath));
			String line;
			while((line = reader.advance()) != null) {
				line = line.trim();
				if(isBreak(line)) break;
				if(!filterLine(line)) {
					processLine(line);
				}
			}
			reader.close();
			obj = doEnd();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		if(line.startsWith(COMMENT_LINE) && line.equals("")) {
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
