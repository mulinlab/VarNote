package org.mulinlab.varnote.cmdline.abstractclass;


import com.intel.gkl.compression.IntelDeflaterFactory;
import htsjdk.samtools.util.IOUtil;
import org.mulinlab.varnote.cmdline.collection.TagArgument;
import org.mulinlab.varnote.config.param.IndexParam;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.zip.DeflaterFactory;
import org.broadinstitute.barclay.argparser.Argument;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.format.Format;
import java.io.File;

public abstract class IndexProgram extends RunProgram {

    @Argument(shortName = Arguments.INPUT_SHORT, fullName = Arguments.INPUT_LONG, doc = Arguments.INDEX_DOC)
    public TagArgument fileToIndex = null;

    @Argument(shortName = Arguments.FORMAT_HEADER_SHORT, fullName = Arguments.FORMAT_HEADER_LONG, optional = true,
            doc = "Indicate whether the first line below the comment line is the header line." +
                    "\nHeader line could start with '#' or have no indicator.")
    public Boolean hasHeader = GlobalParameter.DEFAULT_HAS_HEADER;

    @Argument(shortName = Arguments.FORMAT_HEADER_PATH_SHORT, fullName = Arguments.FORMAT_HEADER_PATH_LONG, optional = true,
            doc = "Path of external file to include the header lines.")
    public File extHeaderFile;

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
        Format format = fileToIndex.getFormat(false);

        if(format == null) format = Format.defaultFormat(fileToIndex.getArgValue(), false);
        format = fileToIndex.setFormat(format);

        if(hasHeader) format.setHasHeaderInFile(true);
        if(extHeaderFile != null) format.setHeaderPath(extHeaderFile.getAbsolutePath());

        IOUtil.assertInputIsValid(fileToIndex.getArgValue());

        IndexParam indexParam = new IndexParam(new File(fileToIndex.getArgValue()));
        indexParam.setFormat(format);
        return indexParam;
    }
}
