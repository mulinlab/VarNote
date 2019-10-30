package org.mulinlab.varnote.cmdline.tools;

import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.mulinlab.varnote.cmdline.abstractclass.QueryProgram;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.cmdline.programgroups.QueryProgramGroup;
import org.mulinlab.varnote.config.run.QueryRegionConfig;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.RunFactory;

@CommandLineProgramProperties(
        summary = QueryRegion.USAGE_SUMMARY + QueryRegion.USAGE_DETAILS,
        oneLineSummary = QueryRegion.USAGE_SUMMARY,
        programGroup = QueryProgramGroup.class)

public final class QueryRegion extends QueryProgram {
    static final String USAGE_SUMMARY = "To quickly retrieve data lines from indexed database(or annotation) file(s) that “overlap” with the specified genomic region like \"chr:beginPos-endPos\"";
    static final String USAGE_DETAILS =
            "\n\nUsage example:" +
            "\n" +
            "java -jar " + GlobalParameter.PRO_NAME + ".jar QueryRegion -Q chr1-1-100000 -D /path/db.vcf.gz \n" ;


    @Argument( fullName = Arguments.QUERY_INPUT_LONG, shortName = Arguments.QUERY_INPUT_SHORT,
            doc = "Region specified as the format \"chr-beginPos-endPos\"", optional = false
    )
    public String queryRegion = null;

    @Argument( fullName = Arguments.QUERY_LABEL_LONG, shortName = Arguments.QUERY_LABEL_SHORT,
            doc = "A flag to determine whether or not to print database name as the first column of the result.", optional = true
    )
    public boolean isDisplayLabel = GlobalParameter.DEFAULT_DISPLAY_LABEL;

    @Override
    protected int doWork() {
        QueryRegionConfig queryRegionConfig = new QueryRegionConfig(queryRegion, dbArguments.getDBList(), isDisplayLabel);
        RunFactory.runQuery(queryRegionConfig);

        return -1;
    }
}
