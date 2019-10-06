package org.mulinlab.varnote.cmdline.index;


import org.mulinlab.varnote.cmdline.abstractclass.CMDProgram;
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
        summary = QueryIndexMeta.USAGE_SUMMARY + QueryIndexMeta.USAGE_DETAILS,
        oneLineSummary = QueryIndexMeta.USAGE_SUMMARY,
        programGroup = IndexProgramGroup.class)

public final class QueryIndexMeta extends CMDProgram {
    static final String USAGE_SUMMARY = "Query header, meta or sequence names stored in the VarNote index files (\".vanno.vi\"). \n ";
    static final String USAGE_DETAILS =
            "\nUsage example:" +
            "\n" +
            "java -jar " + GlobalParameter.PRO_NAME + ".jar IndexMeta -" +  Arguments.INDEX_INPUT_SHORT + " /path/test.gz -"
                    + Arguments.INDEX_PRINT_HEADER_SHORT + " true -" + Arguments.INDEX_PRINT_META_SHORT + " true " + Arguments.INDEX_LITS_CHROM_SHORT + " true" +
            "\n\n" ;

    @Argument(
            fullName = Arguments.INDEX_INPUT_LONG, shortName = Arguments.INDEX_INPUT_SHORT,
            doc = "The indexed database(or annotation) file, indexed by VarNote."
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
        if(indexArguments.replaceHeader != null) config.replaceHeader(indexArguments.replaceHeader);

        return -1;
    }
}
