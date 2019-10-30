package org.mulinlab.varnote.cmdline.tools.advance;

import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.mulinlab.varnote.cmdline.abstractclass.QueryFileProgram;
import org.mulinlab.varnote.cmdline.collection.OutputArgumentCollection;
import org.mulinlab.varnote.cmdline.programgroups.AdvanceProgramGroup;
import org.mulinlab.varnote.config.param.FilterParam;
import org.mulinlab.varnote.config.param.output.OutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.CEPIPRunConfig;
import org.mulinlab.varnote.config.run.PATRunConfig;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.filters.mendelian.MendelianInheritanceADFilter;
import org.mulinlab.varnote.filters.query.gt.DepthFilter;
import org.mulinlab.varnote.filters.query.gt.GenotypeQualityFilter;
import org.mulinlab.varnote.filters.query.line.LineFilter;
import org.mulinlab.varnote.filters.query.line.VCFHeaderLineFilter;
import org.mulinlab.varnote.utils.RunFactory;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.pedigree.PedFiles;
import org.mulinlab.varnote.utils.pedigree.Pedigree;
import org.mulinlab.varnote.utils.pedigree.PedigreeConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@CommandLineProgramProperties(
        summary = PAT.USAGE_SUMMARY + PAT.USAGE_DETAILS,
        oneLineSummary = PAT.USAGE_SUMMARY,
        programGroup = AdvanceProgramGroup.class)

public final class PAT extends QueryFileProgram {
    static final String USAGE_SUMMARY = "PAT";
    static final String USAGE_DETAILS =
            "\n\nUsage example:" +
            "\n" +
            "java -jar " + GlobalParameter.PRO_NAME + ".jar PAT -Q query.vcf\n" ;


    @ArgumentCollection()
    public final OutputArgumentCollection outputArguments = new OutputArgumentCollection();


    @Override
    protected int doWork() {
        Format format = getFormat();


        PATRunConfig runConfig = new PATRunConfig(new QueryFileParam(inputArguments.getQueryFilePath(), format, false, PATRunConfig.getFilterParam()));
        runConfig.setOutParam(outputArguments.getOutParam(new OutParam()));
        runConfig.setThread(runArguments.getThreads());

        RunFactory.runPAT(runConfig);
        return -1;
    }
}
