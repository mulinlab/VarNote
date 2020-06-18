package org.mulinlab.varnote.cmdline.collection;

import org.broadinstitute.barclay.argparser.Argument;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.config.param.output.AnnoOutParam;
import org.mulinlab.varnote.config.param.output.OutParam;
import org.mulinlab.varnote.config.run.AnnoRunConfig;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.enumset.AnnoOutFormat;

import java.io.File;

public final class AnnoConfigArgumentCollection {
    private static final long serialVersionUID = 1L;

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

    public AnnoRunConfig setConfig(final AnnoRunConfig annoRunConfig) {
        annoRunConfig.setForceOverlap(forceOverlap);
        if(annoConfig != null) annoRunConfig.setAnnoConfig(annoConfig.getAbsolutePath());
        if(vcfHeader != null) annoRunConfig.setVcfHeaderPathForBED(vcfHeader.getAbsolutePath());

        return annoRunConfig;
    }
}
