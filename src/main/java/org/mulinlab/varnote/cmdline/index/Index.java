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
    static final String USAGE_SUMMARY = "Generates VarNote index files(\".vanno\" and \".vanno.vi\") for the compressed database(or annotation) file. \n ";
    static final String USAGE_DETAILS = "This tool creates two index files (\".vanno\" and \".vanno.vi\") for the compressed database(or annotation) file " +
            "(The original file is VCF, BED or TAB-delimited format, then compressed by \"tabix bgzip\" function) " +
            ", like the index on a database. \nNote that the input file must be sorted in coordinate order.\n\n" +
            "Usage example:" +
            "\n" +
            "java -jar " + GlobalParameter.PRO_NAME + ".jar Index -I input.vcf.gz\n" +
            "java -jar " + GlobalParameter.PRO_NAME + ".jar Index -I input.tab.gz -" + Arguments.FORMAT_CHROM_SHORT + " 1 -" +
            Arguments.FORMAT_BEGIN_SHORT + " 2 -" +
            Arguments.FORMAT_END_SHORT + " 2 --" + Arguments.FORMAT_REF_LONG + " 4 --" + Arguments.FORMAT_ALT_LONG + " 5" +
            "\n\n" ;

    @Argument(
            fullName = Arguments.INDEX_OUTPUT_LONG, shortName = Arguments.INDEX_OUTPUT_SHORT,
            doc = "Output directory. By default the output files will be written into the same folder as the input file.",
            optional = true
    )
    public String outputFolder = null;

    @Argument(shortName = Arguments.FORMAT_SKIP_SHORT, fullName = Arguments.FORMAT_SKIP_LONG, optional = true,
            doc = "Skip first INT lines(including comment lines) in the data file.")
    public int skip = GlobalParameter.DEFAULT_SKIP;

    @Override
    protected int doWork() {
        IndexParam indexParam = setParam();

        if(skip > 0) indexParam.getFormat().numHeaderLinesToSkip = skip;

        IndexWriteConfig config = new IndexWriteConfig(indexParam);
        RunFactory.writeIndex(config);

        return -1;
    }

    public static void main(final String[] argv) throws IllegalAccessException, InstantiationException {
    }
}
