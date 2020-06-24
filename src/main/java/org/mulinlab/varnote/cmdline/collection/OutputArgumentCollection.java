package org.mulinlab.varnote.cmdline.collection;

import org.broadinstitute.barclay.argparser.Argument;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.config.param.output.OutParam;
import org.mulinlab.varnote.constants.GlobalParameter;

public class OutputArgumentCollection {
    private static final long serialVersionUID = 1L;

    @Argument( shortName = Arguments.INTERSECT_OUT_SHORT, fullName = Arguments.INTERSECT_OUT_LONG, optional = true,
            doc = "Output file path. By default, output file will be written into the same folder as the input file."
    )
    public String outPath = null;

    @Argument( shortName = Arguments.INTERSECT_ZIP_SHORT, fullName = Arguments.INTERSECT_ZIP_LONG, optional = true,
            doc = "A flag to determine whether or not to compress the output results."
    )
    public boolean isGZip = GlobalParameter.DEFAULT_GZIP;

    @Argument( shortName = Arguments.INTERSECT_LOJ_SHORT, fullName = Arguments.INTERSECT_LOJ_LONG, optional = true,
            doc = "A flag to determine whether or not to use the left outer join mode. The 'left outer join' mode reports each of query record regardless of whether containing intersected records."
    )
    public boolean isLOJ = GlobalParameter.DEFAULT_LOJ;

    public OutParam getOutParam(OutParam outParam) {
        if(outPath != null) outParam.setOutputPath(outPath);
        outParam.setGzip(isGZip);
        outParam.setLoj(isLOJ);
        return outParam;
    }
}
