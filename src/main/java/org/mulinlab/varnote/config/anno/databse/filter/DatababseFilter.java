package org.mulinlab.varnote.config.anno.databse.filter;


import org.mulinlab.varnote.utils.node.LocFeature;


public interface DatababseFilter {
	public boolean isFilterLine(final LocFeature loc);
}
