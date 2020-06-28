package org.mulinlab.varnote.cmdline.tools;


import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.mulinlab.varnote.cmdline.abstractclass.QueryFileProgram;
import org.mulinlab.varnote.cmdline.abstractclass.QueryProgram;
import org.mulinlab.varnote.cmdline.collection.InputFileArgumentCollection;
import org.mulinlab.varnote.cmdline.collection.OutputArgumentCollection;
import org.mulinlab.varnote.cmdline.collection.RunArgumentCollection;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.cmdline.programgroups.QueryProgramGroup;
import org.mulinlab.varnote.config.param.output.OutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.CountRunConfig;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.RunFactory;
import org.mulinlab.varnote.utils.format.Format;

@CommandLineProgramProperties(
        summary = Count.USAGE_SUMMARY + Count.USAGE_DETAILS,
        oneLineSummary = Count.USAGE_SUMMARY,
        programGroup = QueryProgramGroup.class)

public final class Count extends QueryProgram {
    static final String USAGE_SUMMARY = "To quickly count intersected records in annotation database(s).";
    static final String USAGE_DETAILS =
            "\n\nUsage example:" +
            "\n" +
            "java -jar " + GlobalParameter.PRO_NAME + ".jar Count -Q q2.sort.bed -D db.1000G_p3.sort.vcf.gz \n" ;


    @ArgumentCollection()
    public final InputFileArgumentCollection inputArguments = new InputFileArgumentCollection();

    @Argument( shortName = Arguments.INTERSECT_THREAD_SHORT, fullName = Arguments.INTERSECT_THREAD_LONG, optional = true,
            doc = "Number of used threads. Sets thread to -1 to get thread number by available processors automatically."
    )
    private Integer threads = GlobalParameter.DEFAULT_THREAD;

    public String getQueryFilePath() {
        return inputArguments.getQueryFilePath();
    };

    @Argument( shortName = Arguments.INTERSECT_OUT_SHORT, fullName = Arguments.INTERSECT_OUT_LONG, optional = true,
            doc = "Output file path. By default output file will be written into the same folder as the input file."
    )
    public String outPath = null;


    protected Format getFormat() {
        Format format = inputArguments.getFormat(getQueryFilePath(), true);
        return format;
    }

    @Override
    protected int doWork() {
        Format format = getFormat();

        CountRunConfig runConfig = new CountRunConfig(new QueryFileParam(inputArguments.getQueryFilePath(), format, false), dbArguments.getDBList());

        if(outPath != null) {
            runConfig.setOutParam(new OutParam(outPath));
        }

        runConfig.setThread(threads);
        RunFactory.runCount(runConfig);

        return 0;
    }
}
