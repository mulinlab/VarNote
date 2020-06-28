package org.mulinlab.varnote.cmdline.tools.config;

import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.mulinlab.varnote.cmdline.abstractclass.ConfigProgram;
import org.mulinlab.varnote.cmdline.programgroups.AnnoProgramGroup;
import org.mulinlab.varnote.cmdline.txtreader.run.AnnoRunReader;
import org.mulinlab.varnote.config.run.AnnoRunConfig;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.RunFactory;

@CommandLineProgramProperties(
        summary = AnnotationConfig.USAGE_SUMMARY + AnnotationConfig.USAGE_DETAILS,
        oneLineSummary = AnnotationConfig.USAGE_SUMMARY,
        programGroup = AnnoProgramGroup.class)

public final class AnnotationConfig extends ConfigProgram {
    static final String USAGE_SUMMARY = "Run VarNote Annotation program with a config file. ";
    static final String USAGE_DETAILS =
            "\n\nUsage example:" +
            "\n" +
            "java -jar " + GlobalParameter.PRO_NAME + ".jar AnnotationConfig -I run.config\n" ;

    @Override
    protected int doWork() {
        AnnoRunConfig runConfig = new AnnoRunReader<AnnoRunConfig>().read(configFile.getAbsolutePath());

        RunFactory.run(runConfig);
        return 0;
    }
}
