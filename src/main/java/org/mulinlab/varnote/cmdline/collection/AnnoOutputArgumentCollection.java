package org.mulinlab.varnote.cmdline.collection;

import org.broadinstitute.barclay.argparser.Argument;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.config.param.output.AnnoOutParam;
import org.mulinlab.varnote.config.param.output.OutParam;
import org.mulinlab.varnote.utils.enumset.AnnoOutFormat;

public final class AnnoOutputArgumentCollection extends OutputArgumentCollection{
    private static final long serialVersionUID = 1L;

    @Argument( shortName = Arguments.ANNO_OF_SHORT, fullName = Arguments.ANNO_OF_LONG, optional = true, doc = "Output format.")
    public AnnoOutFormat outFormat = null;

    public OutParam getOutParam(OutParam outParam) {
        outParam = super.getOutParam(outParam);
        if(outFormat != null) ((AnnoOutParam)outParam).setAnnoOutFormat(outFormat);
        return outParam;
    }
}
