package org.mulinlab.varnote.cmdline.tools.ld;

import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;
import org.mulinlab.varnote.cmdline.abstractclass.QueryFileProgram;
import org.mulinlab.varnote.cmdline.abstractclass.RunProgram;
import org.mulinlab.varnote.cmdline.collection.OutputArgumentCollection;
import org.mulinlab.varnote.cmdline.collection.VariantEffectArgumentCollection;
import org.mulinlab.varnote.cmdline.programgroups.AdvanceProgramGroup;
import org.mulinlab.varnote.cmdline.programgroups.LDProgramGroup;
import org.mulinlab.varnote.config.param.output.OutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.CEPIPRunConfig;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.RunFactory;
import org.mulinlab.varnote.utils.enumset.CellType;
import org.mulinlab.varnote.utils.format.Format;

import java.io.File;
import java.io.IOException;
import java.util.List;

@CommandLineProgramProperties(
        summary = Index1000G.USAGE_SUMMARY + Index1000G.USAGE_DETAILS,
        oneLineSummary = Index1000G.USAGE_SUMMARY,
        programGroup = LDProgramGroup.class)

public final class Index1000G extends RunProgram {
    static final String USAGE_SUMMARY = "Index1000G";
    static final String USAGE_DETAILS =
            "\n\nUsage example:" +
            "\n" +
            "java -jar " + GlobalParameter.PRO_NAME + ".jar Index1000G -I 1kg.phase3.v5.shapeit2.afr.hg19.all.vcf.gz\n" ;


    @Argument( shortName = "I", fullName = "input", optional = false,
            doc = "Path of 1000G file."
    )
    public File file;

    @Override
    protected int doWork() {
        try {
            org.mulinlab.varnote.operations.index.Index1000G index = new org.mulinlab.varnote.operations.index.Index1000G(new File(file.getAbsolutePath()));
            index.makeIndex();
            index.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
