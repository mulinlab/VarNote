package org.mulinlab.varnote.cmdline.tools;


import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.mulinlab.varnote.cmdline.abstractclass.QueryFileProgram;
import org.mulinlab.varnote.cmdline.programgroups.QueryProgramGroup;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.CountRunConfig;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.RunFactory;
import org.mulinlab.varnote.utils.format.Format;

@CommandLineProgramProperties(
        summary = Count.USAGE_SUMMARY + Count.USAGE_DETAILS,
        oneLineSummary = Count.USAGE_SUMMARY,
        programGroup = QueryProgramGroup.class)

public final class Count extends QueryFileProgram {
    static final String USAGE_SUMMARY = "To quickly count data lines from database(or annotation) file(s) that “overlap” with genomic features defined in the query file.";
    static final String USAGE_DETAILS =
            "\n\nUsage example:" +
            "\n" +
            "java -jar " + GlobalParameter.PRO_NAME + ".jar Count -Q query.vcf -D /path/db.vcf.gz \n" ;


    @Override
    protected int doWork() {
        Format format = getFormat();

        CountRunConfig runConfig = new CountRunConfig(new QueryFileParam(inputArguments.getQueryFilePath(), format, false), dbArguments.getDBList());
        runConfig.setThread(runArguments.getThreads());
        RunFactory.runCount(runConfig);

        return -1;
    }
}
