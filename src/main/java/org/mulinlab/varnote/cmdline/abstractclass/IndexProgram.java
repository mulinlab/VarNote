package org.mulinlab.varnote.cmdline.abstractclass;


import com.intel.gkl.compression.IntelDeflaterFactory;
import htsjdk.samtools.util.IOUtil;
import org.mulinlab.varnote.cmdline.collection.InputFileArgumentCollection;
import org.mulinlab.varnote.cmdline.collection.TagArgument;
import org.mulinlab.varnote.config.param.IndexParam;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.zip.DeflaterFactory;
import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.format.Format;

import java.io.File;

public abstract class IndexProgram extends CMDProgram {

    @Argument(
            fullName = Arguments.INDEX_INPUT_LONG, shortName = Arguments.INDEX_INPUT_SHORT,
            doc = "Path of compressed database (or annotation file, \".gz\" format) to be indexed. The file must be position-sorted (first by sequence name and then by leftmost coordinate). \n\n" +
            "Possible Tags: {vcf, bed, tab} \n\n" +
            "Possible attributes for \"tab\" tag: {c, b, e, ref, alt, ci}\n" +
            "c: column of sequence name (1-based)\n" +
            "b: column of start chromosomal position (1-based)\n" +
            "e: column of end chromosomal position (1-based)\n" +
            "ref: column of reference allele\n" +
            "alt: column of alternative allele\n" +
            "0: specify the position in the data file is 0-based rather than 1-based\n" +
            "ci: comment indicator\n"
    )
    public TagArgument fileToIndex = null;

    @Argument(shortName = Arguments.FORMAT_HEADER_SHORT, fullName = Arguments.FORMAT_HEADER_LONG, optional = true,
            doc = "Indicate whether input file contains a column header line. If --" + Arguments.FORMAT_HEADER_LONG + " is included, the first line below the comment line will be considered as a header line." +
                    "\nHeader line could start with '#' or have no indicator.\n" +
                    "To facilitate the automatic inference, header should at least include CHROM and POS (or, alternatively, BEGIN and END) columns, while, REF, ALT and other columns are optional. Other column names could be any words defined by user.")
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
        Format format = fileToIndex.getFormat();

        if(format == null) format = Format.defaultFormat(fileToIndex.getArgValue(), false);
        format = fileToIndex.setFormat(format);

        if(hasHeader) format.setHasHeader(true);
        if(extHeaderFile != null) format.setHeaderPath(extHeaderFile.getAbsolutePath());

        IOUtil.assertInputIsValid(fileToIndex.getArgValue());

        IndexParam indexParam = new IndexParam(new File(fileToIndex.getArgValue()));
        indexParam.setFormat(format);
        return indexParam;
    }
}
