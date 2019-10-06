package org.mulinlab.varnote.cmdline.abstractclass;


import com.intel.gkl.compression.IntelDeflaterFactory;
import htsjdk.samtools.util.Log;
import org.apache.logging.log4j.Level;
import org.mulinlab.varnote.cmdline.collection.FormatArgumentCollection;
import org.mulinlab.varnote.config.param.IndexParam;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.zip.DeflaterFactory;
import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.mulinlab.varnote.utils.LoggingUtils;

import java.io.File;

public abstract class IndexProgram extends CMDProgram {

    @Argument(
            fullName = Arguments.INDEX_INPUT_LONG, shortName = Arguments.INDEX_INPUT_SHORT,
            doc = "Path of compressed annotation file (\".gz\" format) to be indexed. The file must be position-sorted (first by sequence name and then by leftmost coordinate)."
    )
    public File fileToIndex = null;


    @ArgumentCollection()
    protected final FormatArgumentCollection formatArguments = new FormatArgumentCollection();

    @Argument(fullName = Arguments.USE_JDK_LONG, shortName = Arguments.USE_JDK_SHORT,
            doc = "Use the JDK Deflater instead of the IntelDeflater for writing index.", optional = true)
    public Boolean USE_JDK_DEFLATER = false;

    @Override
    protected void onStartup() {
        if (!USE_JDK_DEFLATER) {
            BlockCompressedOutputStream.setDefaultDeflaterFactory(new IntelDeflaterFactory());
        } else {
            BlockCompressedOutputStream.setDefaultDeflaterFactory(new DeflaterFactory());
        }
    }

    protected IndexParam setParam() {
        IndexParam indexParam = new IndexParam(fileToIndex);
        return indexParam;
    }
}
