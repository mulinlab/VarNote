package org.mulinlab.varnote.cmdline.tools;

import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.mulinlab.varnote.cmdline.abstractclass.CMDProgram;
import org.mulinlab.varnote.cmdline.abstractclass.QueryFileProgram;
import org.mulinlab.varnote.cmdline.abstractclass.RunProgram;
import org.mulinlab.varnote.cmdline.collection.AnnoConfigArgumentCollection;
import org.mulinlab.varnote.cmdline.collection.AnnoOutputArgumentCollection;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.cmdline.programgroups.AnnoProgramGroup;
import org.mulinlab.varnote.config.param.RunParam;
import org.mulinlab.varnote.config.param.output.AnnoOutParam;
import org.mulinlab.varnote.config.run.AnnoRunConfig;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.RunFactory;
import java.io.File;

@CommandLineProgramProperties(
        summary = AnnotationIntersectFile.USAGE_SUMMARY + AnnotationIntersectFile.USAGE_DETAILS,
        oneLineSummary = AnnotationIntersectFile.USAGE_SUMMARY,
        programGroup = AnnoProgramGroup.class)

public final class AnnotationIntersectFile extends RunProgram {

    static final String USAGE_SUMMARY = "To quickly extract desired annotation fields from an existing VarNote intersection file. \n\n";
    static final String USAGE_DETAILS =
            "Usage example:" + "\n" + "java -jar " + GlobalParameter.PRO_NAME + ".jar AnnotationIntersectFile -I XXX.overlap \n" ;

    @Argument( shortName = Arguments.INPUT_SHORT, fullName = Arguments.INPUT_LONG, optional = false,
            doc = "VarNote intersection file path."
    )
    public File intersectFile = null;

    @ArgumentCollection()
    public final AnnoOutputArgumentCollection outputArguments = new AnnoOutputArgumentCollection();

    @ArgumentCollection()
    public final AnnoConfigArgumentCollection configArguments = new AnnoConfigArgumentCollection();

    @Override
    protected int doWork() {

        AnnoRunConfig annoRunConfig = new AnnoRunConfig(intersectFile.getAbsolutePath());
        annoRunConfig = configArguments.setConfig(annoRunConfig);
        annoRunConfig.setOutParam(outputArguments.getOutParam(new AnnoOutParam()));

        RunFactory.runAnnoFromOverlapFile(annoRunConfig);
        return 0;
    }
}
