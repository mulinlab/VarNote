package org.mulinlab.varnote.cmdline.tools.advance;

import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.mulinlab.varnote.cmdline.abstractclass.QueryFileProgram;
import org.mulinlab.varnote.cmdline.collection.OutputArgumentCollection;
import org.mulinlab.varnote.cmdline.collection.VariantEffectArgumentCollection;
import org.mulinlab.varnote.cmdline.programgroups.AdvanceProgramGroup;
import org.mulinlab.varnote.config.param.output.OutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.CANRunConfig;
import org.mulinlab.varnote.config.run.PATRunConfig;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.RunFactory;
import org.mulinlab.varnote.utils.format.Format;
import java.io.File;

@CommandLineProgramProperties(
        summary = CAN.USAGE_SUMMARY + CAN.USAGE_DETAILS,
        oneLineSummary = CAN.USAGE_SUMMARY,
        programGroup = AdvanceProgramGroup.class)

public final class CAN extends QueryFileProgram {
    static final String USAGE_SUMMARY = "CAN";
    static final String USAGE_DETAILS =
            "\n\nUsage example:" +
            "\n" +
            "java -jar " + GlobalParameter.PRO_NAME + ".jar CAN -Q query.vcf\n" ;

    @ArgumentCollection()
    public final OutputArgumentCollection outputArguments = new OutputArgumentCollection();

    @ArgumentCollection()
    public final VariantEffectArgumentCollection variantEffectArgument = new VariantEffectArgumentCollection();

    @Override
    protected int doWork() {
        Format format = getFormat();

        CANRunConfig runConfig = new CANRunConfig(new QueryFileParam(inputArguments.getQueryFilePath(), format, false), dbArguments.getDBList(), variantEffectArgument.getJannovarUtils());
        runConfig.setOutParam(outputArguments.getOutParam(new OutParam()));
        runConfig.setThread(runArguments.getThreads());

        RunFactory.runADTools(runConfig, "CAN");
        return -1;
    }
}
