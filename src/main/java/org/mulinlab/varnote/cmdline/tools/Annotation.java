package org.mulinlab.varnote.cmdline.tools;

import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.mulinlab.varnote.cmdline.abstractclass.QueryFileProgram;
import org.mulinlab.varnote.cmdline.collection.AnnoConfigArgumentCollection;
import org.mulinlab.varnote.cmdline.collection.AnnoOutputArgumentCollection;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.cmdline.programgroups.AnnoProgramGroup;
import org.mulinlab.varnote.config.param.output.AnnoOutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.AnnoRunConfig;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.RunFactory;
import org.mulinlab.varnote.utils.format.Format;

import java.io.File;

@CommandLineProgramProperties(
        summary = Annotation.USAGE_SUMMARY + Annotation.USAGE_DETAILS,
        oneLineSummary = Annotation.USAGE_SUMMARY,
        programGroup = AnnoProgramGroup.class)

public final class Annotation extends QueryFileProgram {

    static final String USAGE_SUMMARY = "Annotation extraction for a list of intervals or variants";
    static final String USAGE_DETAILS = "This tool identifies desired annotation fields from databases. " +
            "It allows feature extraction using both interval-level overlap and variant-level exact match, " +
            "and supports allele-specific variant annotation for SNV/Indel and region-specific annotation for large variants." +
            "\n\nUsage example:" +
                    "\n" +
                    "java -jar " + GlobalParameter.PRO_NAME + ".jar Annotation -Q query.vcf -D /path/db.vcf.gz\n" ;

    @ArgumentCollection()
    public final AnnoOutputArgumentCollection outputArguments = new AnnoOutputArgumentCollection();
    @ArgumentCollection()
    public final AnnoConfigArgumentCollection configArguments = new AnnoConfigArgumentCollection();


    @Override
    protected int doWork() {
        Format format = getFormat();

        AnnoOutParam outParam = (AnnoOutParam)outputArguments.getOutParam(new AnnoOutParam());

        AnnoRunConfig annoRunConfig = new AnnoRunConfig(new QueryFileParam(inputArguments.getQueryFilePath(), format, true), dbArguments.getDBList());
        annoRunConfig = configArguments.setConfig(annoRunConfig);

        annoRunConfig.setOutParam(outParam);
        annoRunConfig.setThread(runArguments.getThreads());
        RunFactory.run(annoRunConfig);
        return 0;
    }
}
