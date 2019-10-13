package org.mulinlab.varnote.cmdline.txtreader.anno;

import org.mulinlab.varnote.cmdline.txtreader.abs.QueryReader;
import org.mulinlab.varnote.config.param.output.AnnoOutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.AnnoRunConfig;
import org.mulinlab.varnote.utils.format.Format;

public final class OverlapHeaderReader<T> extends QueryReader<T> {

	@Override
	public T doEnd() {
		super.doEnd();

		checkDB();

		Format format = null;
		if(valueHash.get(QUERY_FORMAT) != null) format =  Format.readFormatString(valueHash.get(QUERY_FORMAT));

		format = setformatHeader(format);
		QueryFileParam queryFileParam = new QueryFileParam(valueHash.get(QUERY), format);

		AnnoRunConfig annoRunConfig = new AnnoRunConfig(queryFileParam, dbParams);
		if(valueHash.get(OUTPUT_PATH) != null) {
			AnnoOutParam outParam = new AnnoOutParam();
			outParam.setOutputPath(valueHash.get(OUTPUT_PATH));
			annoRunConfig.setOutParam(outParam);
		}

		return (T)annoRunConfig;
	}
	
	@Override
	public boolean filterLine(String line) {
		return !line.startsWith(OVERLAP_NOTE);
	}

	@Override
	public boolean isBreak(String line) {
		return line.equalsIgnoreCase(OVERLAP_NOTE + END);
	}

	@Override
	public String[] splitLine(String line) {
		return super.splitLine(line.substring(1));
	}

	@Override
	public void putValue(String key, String val) {
		if(key.equals(DB_PATH)) {
			if(valueHash.get(DB_PATH) != null) setDB();
		}
		super.putValue(key, val);
	}
}
