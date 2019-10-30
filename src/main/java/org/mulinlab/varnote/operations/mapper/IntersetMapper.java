package org.mulinlab.varnote.operations.mapper;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.OverlapRunConfig;
import org.mulinlab.varnote.operations.readers.query.AbstractFileReader;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.IOException;

public final class IntersetMapper extends AbstractMapper{

	public IntersetMapper(final OverlapRunConfig config, final int index) {
		super(config, index);
		queryEngine = VannoUtils.getQuery(config.getRunParam().getMode(), config.getDatabses(), false);
	}

	@Override
	public AbstractFileReader getQueryForThread() {
		return ((QueryFileParam)config.getQueryParam()).getThreadReader(index);
	}

	@Override
	public void processResult(final LocFeature node) throws IOException {
//		System.out.println(node.toString() + "," + queryEngine.getResults().size());
		((OverlapRunConfig)this.config).printRecord(node, queryEngine.getResults(), index);
	}

	@Override
	public Integer getResult() {
		return null;
	}
}
