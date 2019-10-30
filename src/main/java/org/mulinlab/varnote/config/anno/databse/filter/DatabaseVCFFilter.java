package org.mulinlab.varnote.config.anno.databse.filter;

import org.mulinlab.varnote.config.anno.databse.VCFParser;
import org.mulinlab.varnote.filters.query.VariantFilter;
import org.mulinlab.varnote.operations.decode.VCFLocCodec;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.node.LocFeature;

import java.util.List;


public final class DatabaseVCFFilter extends AbstractDatabaseFilter {

	private final VCFParser vcfParser;
	private final List<VariantFilter> filters;

	public DatabaseVCFFilter(final List<VariantFilter> filters, final VCFParser vcfParser) {
		this.filters = filters;
		this.vcfParser = vcfParser;
	}


	@Override
	public boolean isFilterLine(final LocFeature loc) {
		return false;
	}
}
