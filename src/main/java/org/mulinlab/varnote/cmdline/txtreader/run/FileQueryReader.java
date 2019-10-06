package org.mulinlab.varnote.cmdline.txtreader.run;

import org.mulinlab.varnote.cmdline.txtreader.abs.AbstractQueryReader;
import org.mulinlab.varnote.cmdline.txtreader.abs.QueryReader;
import org.mulinlab.varnote.config.param.RunParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.format.Format;


public class FileQueryReader<T> extends QueryReader<T> {

	protected QueryFileParam queryFileParam;
	protected RunParam runParam;

	@Override
	public T doEnd() {
		super.doEnd();

		checkDB();
		Format format = null;
		if(valueHash.get(QUERY_FORMAT) != null) format =  VannoUtils.parseIndexFileFormat(valueHash.get(QUERY_FORMAT));
		queryFileParam = new QueryFileParam(valueHash.get(QUERY), format);

		format = queryFileParam.getQueryFormat();
		queryFileParam.setQueryFormat(setformat(format));

		runParam = new RunParam();
		if(valueHash.get(THREAD) != null) runParam.setThread(Integer.parseInt(valueHash.get(THREAD)));

		return null;
	}


	@Override
	public void processLine(String line) {
		super.processLine(line);
	}
}
