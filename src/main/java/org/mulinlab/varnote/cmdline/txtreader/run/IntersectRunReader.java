package org.mulinlab.varnote.cmdline.txtreader.run;


import org.mulinlab.varnote.config.param.output.IntersetOutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.OverlapRunConfig;
import org.mulinlab.varnote.utils.VannoUtils;


public final class IntersectRunReader<T> extends FileQueryReader<T> {

	@Override
	public T doEnd() {
		super.doEnd();

		queryFileParam = new QueryFileParam(valueHash.get(QUERY), format, false);

		IntersetOutParam outParam = new IntersetOutParam();
		outParam = (IntersetOutParam)setOutParam(outParam);

		if(valueHash.get(OUTPUT_MODE) != null) outParam.setOutputMode(VannoUtils.checkOutMode(Integer.parseInt(valueHash.get(OUTPUT_MODE))));
		if(valueHash.get(REMOVE_COMMENT) != null) outParam.setRemoveCommemt(VannoUtils.strToBool(valueHash.get(REMOVE_COMMENT)));

		OverlapRunConfig runConfig = new OverlapRunConfig(queryFileParam, dbParams);
		runConfig.setOutParam(outParam);
		runConfig.setRunParam(runParam);

		return (T)runConfig;
	}
}
