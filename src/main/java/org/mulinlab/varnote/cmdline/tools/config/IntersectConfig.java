package org.mulinlab.varnote.cmdline.tools.config;

import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.mulinlab.varnote.cmdline.abstractclass.ConfigProgram;
import org.mulinlab.varnote.cmdline.programgroups.QueryProgramGroup;
import org.mulinlab.varnote.cmdline.txtreader.run.IntersectRunReader;
import org.mulinlab.varnote.config.run.OverlapRunConfig;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.RunFactory;


@CommandLineProgramProperties(
        summary = IntersectConfig.USAGE_SUMMARY + IntersectConfig.USAGE_DETAILS,
        oneLineSummary = IntersectConfig.USAGE_SUMMARY,
        programGroup = QueryProgramGroup.class)

public final class IntersectConfig extends ConfigProgram {
    static final String USAGE_SUMMARY = "Run VarNote Intersect program with a config file. ";
    static final String USAGE_DETAILS =
            "\n\nUsage example:" +
            "\n" +
            "java -jar " + GlobalParameter.PRO_NAME + ".jar IntersectConfig -I run.config\n" ;

    @Override
    protected int doWork() {
        OverlapRunConfig runConfig = new IntersectRunReader<OverlapRunConfig>().read(configFile.getAbsolutePath());
        RunFactory.run(runConfig);
        return 0;
    }
}
