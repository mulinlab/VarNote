package org.mulinlab.varnote.cmdline.query;

import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.mulinlab.varnote.cmdline.abstractclass.QueryFileProgram;
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

    static final String USAGE_SUMMARY = "Annotation extraction for a list of intervals or variants\n ";
    static final String USAGE_DETAILS = "This tool identifies desired annotation fields from databases. " +
            "It allows feature extraction using both interval-level overlap and variant-level exact match, " +
            "and supports allele-specific variant annotation for SNV/Indel and region-specific annotation for large variants." +
            "\n\nUsage example:" +
                    "\n" +
                    "java -jar " + GlobalParameter.PRO_NAME + ".jar Annotation -Q query.vcf -D /path/db.vcf.gz \n" ;

    @ArgumentCollection()
    public final AnnoOutputArgumentCollection outputArguments = new AnnoOutputArgumentCollection();

    @Argument( shortName = Arguments.ANNO_CONFIG_SHORT, fullName = Arguments.ANNO_CONFIG_LONG, optional = true,
            doc = "Path of annotate configuration file. The program annotates query record with all information in databases if without this option. \n" +
                    "If -a is not defined, the program will search file named /path/to/query/query_file + \".annoc\" under the folder of the query file."
    )
    private File annoConfig = null;

    @Argument( shortName = Arguments.ANNO_FO_SHORT, fullName = Arguments.ANNO_FO_LONG, optional = true,
            doc = "Force overlap mode. Force the program to omit REF and ALT matching and allele specific feature extraction."
    )
    private boolean forceOverlap = GlobalParameter.DEFAULT_FORCE_OVERLAP;

    @Argument( shortName = Arguments.ANNO_VH_SHORT, fullName = Arguments.ANNO_VH_LONG, optional = true,
            doc = "VCF output header file. This file is required when the format of query file is BED or TAB-delimited, but the format of annotation output is VCF."
    )
    private File vcfHeader = null;

    @Override
    protected int doWork() {
        Format format = getFormat();

        AnnoOutParam outParam = new AnnoOutParam();
        outParam = (AnnoOutParam)outputArguments.getOutParam(outParam);

        AnnoRunConfig annoRunConfig = new AnnoRunConfig(new QueryFileParam(queryFilePath, format), dbArguments.getDBList());

        annoRunConfig.setForceOverlap(forceOverlap);
        if(annoConfig != null) annoRunConfig.setAnnoConfig(annoConfig.getAbsolutePath());
        if(vcfHeader != null) annoRunConfig.setVcfHeaderPathForBED(vcfHeader.getAbsolutePath());

        annoRunConfig.setThread(runArguments.getThreads());
        RunFactory.run(annoRunConfig);
        return -1;
    }
}
