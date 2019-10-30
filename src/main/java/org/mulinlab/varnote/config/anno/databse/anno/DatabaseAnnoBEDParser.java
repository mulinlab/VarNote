package org.mulinlab.varnote.config.anno.databse.anno;


import org.mulinlab.varnote.utils.enumset.AnnoOutFormat;


public final class DatabaseAnnoBEDParser extends AbstractDatababseAnnoParser {
	public DatabaseAnnoBEDParser(final ExtractConfig config, boolean isForceOverlap, final AnnoOutFormat annoOutFormat) {
		super(config, isForceOverlap, annoOutFormat);

		config.getDb().setDefaultLocCodec(true);
	}
}
