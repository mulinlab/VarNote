package org.mulinlab.varnote.cmdline.index;


import org.mulinlab.varnote.cmdline.abstractclass.RunProgram;
import org.mulinlab.varnote.cmdline.collection.IndexArgumentCollection;
import org.mulinlab.varnote.cmdline.programgroups.IndexProgramGroup;
import org.mulinlab.varnote.config.index.IndexWriteConfig;
import org.mulinlab.varnote.config.param.IndexParam;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.broadinstitute.barclay.argparser.CommandLineProgramProperties;

import java.io.File;

@CommandLineProgramProperties(
        summary = IndexInfo.USAGE_SUMMARY + IndexInfo.USAGE_DETAILS,
        oneLineSummary = IndexInfo.USAGE_SUMMARY,
        programGroup = IndexProgramGroup.class)

public final class IndexInfo extends RunProgram {
    static final String USAGE_SUMMARY = "To query related information (such as header, format, meta information or sequence name) stored in the VarNote index file of each annotation database.";
    static final String USAGE_DETAILS =
            "\nUsage example:" +
            "\n" +
            "java -jar " + GlobalParameter.PRO_NAME + ".jar IndexInfo -" +  Arguments.INPUT_SHORT + " /path/test.gz -"
                    + Arguments.INDEX_PRINT_HEADER_SHORT + " true -" + Arguments.INDEX_PRINT_META_SHORT + " true -" + Arguments.INDEX_LITS_CHROM_SHORT + " true" +
            "\n\n" ;

    @Argument(
            fullName = Arguments.INPUT_LONG, shortName = Arguments.INPUT_SHORT,
            doc = "The indexed annotation database file, must be indexed by VarNote."
    )
    public File vannoFile = null;

    @ArgumentCollection
    protected final IndexArgumentCollection indexArguments = new IndexArgumentCollection();

    @Override
    protected int doWork() {
        IndexParam indexParam = new IndexParam(vannoFile);

        IndexWriteConfig config = new IndexWriteConfig(indexParam);

        if(indexArguments.printHeader) config.printHeader();
        if(indexArguments.printMeta) config.printMetaData();
        if(indexArguments.listSeq) config.listChromsome();
        if(indexArguments.printFormat) config.printFormat();
        if(indexArguments.replaceHeader != null) config.replaceHeader(indexArguments.replaceHeader);

        return 0;
    }
}
