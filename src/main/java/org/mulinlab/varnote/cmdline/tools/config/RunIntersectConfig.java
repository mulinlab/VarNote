package org.mulinlab.varnote.cmdline.tools.config;

import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.mulinlab.varnote.cmdline.abstractclass.ConfigProgram;
import org.mulinlab.varnote.cmdline.programgroups.ConfigProgramGroup;
import org.mulinlab.varnote.cmdline.txtreader.run.IntersectRunReader;
import org.mulinlab.varnote.config.run.OverlapRunConfig;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.RunFactory;


@CommandLineProgramProperties(
        summary = RunIntersectConfig.USAGE_SUMMARY + RunIntersectConfig.USAGE_DETAILS,
        oneLineSummary = RunIntersectConfig.USAGE_SUMMARY,
        programGroup = ConfigProgramGroup.class)

public final class RunIntersectConfig extends ConfigProgram {
    static final String USAGE_SUMMARY = "Run Intersect program with a config file. ";
    static final String USAGE_DETAILS =
            "\n\nUsage example:" +
            "\n" +
            "java -jar " + GlobalParameter.PRO_NAME + ".jar RunIntersectConfig -I run.config\n" ;

    @Override
    protected int doWork() {
        OverlapRunConfig runConfig = new IntersectRunReader<OverlapRunConfig>().read(configFile.getAbsolutePath());
        RunFactory.run(runConfig);
        return 0;
    }
}
