package org.mulinlab.varnote.cmdline.abstractclass;


import com.intel.gkl.compression.IntelInflaterFactory;
import htsjdk.samtools.util.BlockGunzipper;
import htsjdk.samtools.util.zip.InflaterFactory;
import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.mulinlab.varnote.cmdline.collection.DBArgumentCollection;
import org.mulinlab.varnote.cmdline.collection.FormatArgumentCollection;
import org.mulinlab.varnote.cmdline.collection.RunArgumentCollection;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.utils.format.Format;

public abstract class QueryFileProgram extends QueryProgram {

    @Argument(
        fullName = Arguments.INTERSECT_INPUT_LONG, shortName = Arguments.INTERSECT_INPUT_SHORT,
        doc = "Path of query file (support plain text or compressed files, including gzip and block gzip)."
    )
    protected String queryFilePath = null;

    @ArgumentCollection()
    protected final FormatArgumentCollection formatArguments = new FormatArgumentCollection();

    @ArgumentCollection
    protected final RunArgumentCollection runArguments = new RunArgumentCollection();

    protected Format getFormat() {
        return formatArguments.getFormat(queryFilePath, true);
    }
}
