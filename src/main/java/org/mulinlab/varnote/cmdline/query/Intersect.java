package org.mulinlab.varnote.cmdline.query;

import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.mulinlab.varnote.cmdline.abstractclass.QueryFileProgram;
import org.mulinlab.varnote.cmdline.collection.OutputArgumentCollection;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.cmdline.programgroups.QueryProgramGroup;
import org.mulinlab.varnote.config.param.output.IntersetOutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.OverlapRunConfig;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.RunFactory;
import org.mulinlab.varnote.utils.format.Format;

@CommandLineProgramProperties(
        summary = Intersect.USAGE_SUMMARY + Intersect.USAGE_DETAILS,
        oneLineSummary = Intersect.USAGE_SUMMARY,
        programGroup = QueryProgramGroup.class)

public final class Intersect extends QueryFileProgram {
    static final String USAGE_SUMMARY = "To quickly retrieve data lines from indexed database(or annotation) file(s) that “overlap” with genomic features defined in the query file. \n ";
    static final String USAGE_DETAILS =
            "\n\nUsage example:" +
            "\n" +
            "java -jar " + GlobalParameter.PRO_NAME + ".jar Intersect -Q query.vcf -D /path/db.vcf.gz \n" ;


    @ArgumentCollection()
    public final OutputArgumentCollection outputArguments = new OutputArgumentCollection();

    @Argument( shortName = Arguments.INTERSECT_RC_SHORT, fullName = Arguments.INTERSECT_RC_LONG, optional = true,
            doc = "A flag to determine whether or not to remove the comment lines(start with \'@\') in the output file. Note that the comment lines are required for the Annotation program."
    )
    public boolean isRemoveComment = GlobalParameter.DEFAULT_REMOVE_COMMENT;

    @Argument( shortName = Arguments.INTERSECT_OUT_MODE_SHORT, fullName = Arguments.INTERSECT_OUT_MODE_LONG, optional = true,
            doc = "Output recording mode (default 2). 0 for \"only output query records\"; 1 for \"only output matched database records\"; 2 for \"output both query records and matched database records\""
    )
    public int outModeValue = GlobalParameter.DEFALT_OUT_MODE_VAL;

    @Override
    protected int doWork() {
        Format format = getFormat();

        IntersetOutParam outParam = new IntersetOutParam();
        outParam = (IntersetOutParam)outputArguments.getOutParam(outParam);
        outParam.setRemoveCommemt(isRemoveComment);
        outParam.setOutputMode(outModeValue);

        OverlapRunConfig runConfig = new OverlapRunConfig(new QueryFileParam(inputArguments.getQueryFilePath(), format), dbArguments.getDBList());
        runConfig.setOutParam(outParam);
        runConfig.setThread(runArguments.getThreads());

        RunFactory.run(runConfig);
        return -1;
    }
}
