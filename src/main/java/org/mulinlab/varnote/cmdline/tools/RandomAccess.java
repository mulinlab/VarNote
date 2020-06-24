package org.mulinlab.varnote.cmdline.tools;

import com.intel.gkl.compression.IntelInflaterFactory;
import htsjdk.samtools.util.BlockGunzipper;
import htsjdk.samtools.util.Log;
import htsjdk.samtools.util.zip.InflaterFactory;
import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.mulinlab.varnote.cmdline.abstractclass.CMDProgram;
import org.mulinlab.varnote.cmdline.abstractclass.QueryProgram;
import org.mulinlab.varnote.cmdline.collection.DBArgumentCollection;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.cmdline.programgroups.QueryProgramGroup;
import org.mulinlab.varnote.config.run.QueryRegionConfig;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.RunFactory;

@CommandLineProgramProperties(
        summary = RandomAccess.USAGE_SUMMARY + RandomAccess.USAGE_DETAILS,
        oneLineSummary = RandomAccess.USAGE_SUMMARY,
        programGroup = QueryProgramGroup.class)

public final class RandomAccess extends CMDProgram {
    static final String USAGE_SUMMARY = "To quickly retrieve (by independent random access) intersected records from indexed annotation database(s) given a genomic region like \"chrN:beginPos-endPos\".";
    static final String USAGE_DETAILS =
            "\n\nUsage example:" +
            "\n" +
            "java -jar " + GlobalParameter.PRO_NAME + ".jar RandomAccess -Q chr1:1-100000 -D /path/db.vcf.gz \n" ;

    @Argument(fullName = Arguments.LOG_LONG, doc = "Whether to print log.", optional = true)
    public Boolean islog = false;

    @Argument(fullName = Arguments.USE_JDKI_LONG, shortName = Arguments.USE_JDKI_SHORT,
            doc = "Use the JDK Inflater instead of the IntelInflater for reading index.", optional = true)
    public Boolean USE_JDK_INFLATER = false;

    @Argument( fullName = Arguments.QUERY_INPUT_LONG, shortName = Arguments.QUERY_INPUT_SHORT,
            doc = "Region specified as the format \"chrN:beginPos-endPos\"", optional = false
    )
    public String queryRegion = null;

    @ArgumentCollection
    public final DBArgumentCollection dbArguments = new DBArgumentCollection();


    @Argument( fullName = Arguments.QUERY_LABEL_LONG, shortName = Arguments.QUERY_LABEL_SHORT,
            doc = "A flag to determine whether or not to print database name as the first column of the result.", optional = true
    )
    public boolean isDisplayLabel = GlobalParameter.DEFAULT_DISPLAY_LABEL;


    @Override
    protected int doWork() {
        QueryRegionConfig queryRegionConfig = new QueryRegionConfig(queryRegion, dbArguments.getDBList(), isDisplayLabel);
        RunFactory.runQuery(queryRegionConfig);

        return 0;
    }

    @Override
    protected void onStartup() {
        if (!USE_JDK_INFLATER) {
            BlockGunzipper.setDefaultInflaterFactory(new IntelInflaterFactory());
        } else {
            BlockGunzipper.setDefaultInflaterFactory(new InflaterFactory());
        }

        if(!islog) LoggingUtils.setLoggingLevel(Log.LogLevel.ERROR);
        else LoggingUtils.setLoggingLevel(Log.LogLevel.INFO);
    }


    protected void onShutdown() {

    }
}
