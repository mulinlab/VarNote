package org.mulinlab.varnote.config.anno.databse.filter;


import org.mulinlab.varnote.operations.decode.LocCodec;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.node.LocFeature;


public final class DatabaseBEDFilter extends AbstractDatabaseFilter {

	public DatabaseBEDFilter() {

	}

	@Override
	public boolean isFilterLine(final LocFeature loc) {
		return false;
	}
}
