package org.mulinlab.varnote.cmdline.index;

import org.mulinlab.varnote.cmdline.abstractclass.CMDProgram;
import org.mulinlab.varnote.cmdline.abstractclass.IndexProgram;
import org.mulinlab.varnote.cmdline.programgroups.IndexProgramGroup;
import org.mulinlab.varnote.config.index.IndexWriteConfig;
import org.mulinlab.varnote.config.param.IndexParam;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.RunFactory;
import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.mulinlab.varnote.utils.format.Format;

@CommandLineProgramProperties(
        summary = Index.USAGE_SUMMARY + Index.USAGE_DETAILS,
        oneLineSummary = Index.USAGE_SUMMARY,
        programGroup = IndexProgramGroup.class)

public final class Index extends IndexProgram {
    static final String USAGE_SUMMARY = "To generate VarNote index (\".vanno\" and \".vanno.vi\") for compressed (block gzip) annotation database file.";
    static final String USAGE_DETAILS = "This tool creates two index files (\".vanno\" and \".vanno.vi\") for compressed annotation database file " +
            "(The original database file is VCF, BED or TAB-delimited format, then compressed by \"tabix bgzip\" function)" +
            ". \nNote that the input file must be position-sorted (first by sequence name and then by leftmost coordinate).\n\n" +
            "Usage example:" +
            "\n" +
            "java -jar " + GlobalParameter.PRO_NAME + ".jar Index -I input.vcf.gz\n\n" ;

    @Argument(
            fullName = Arguments.INDEX_OUTPUT_LONG, shortName = Arguments.INDEX_OUTPUT_SHORT,
            doc = "Output directory. By default, the output files will be written into the same folder as the input file.",
            optional = true
    )
    public String outputFolder = null;

    @Argument(shortName = Arguments.FORMAT_SKIP_SHORT, fullName = Arguments.FORMAT_SKIP_LONG, optional = true,
            doc = "Skip first INT lines (including comment lines) in the data file.")
    public int skip = GlobalParameter.DEFAULT_SKIP;

    @Override
    protected int doWork() {
        IndexParam indexParam = setParam();

        if(skip > 0) indexParam.getFormat().numHeaderLinesToSkip = skip;

        IndexWriteConfig config = new IndexWriteConfig(indexParam);
        RunFactory.writeIndex(config);

        return 0;
    }

    public static void main(final String[] argv) throws IllegalAccessException, InstantiationException {
    }
}
