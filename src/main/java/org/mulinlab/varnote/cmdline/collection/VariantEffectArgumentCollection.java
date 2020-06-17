package org.mulinlab.varnote.cmdline.collection;

import org.broadinstitute.barclay.argparser.Argument;
import org.mulinlab.varnote.utils.JannovarUtils;
import org.mulinlab.varnote.utils.enumset.GenomeAssembly;
import org.mulinlab.varnote.utils.enumset.TranscriptSource;
import java.io.File;


public final class VariantEffectArgumentCollection {
    private static final long serialVersionUID = 1L;

    @Argument( shortName = "G", fullName = "Genome", doc = "Genome reference assembly version.")
    public GenomeAssembly genomeAssembly = null;

    @Argument( shortName = "TS", fullName = "TSource", doc = "The supported transcript sources.")
    public TranscriptSource transcriptSource = TranscriptSource.ensembl;

    public JannovarUtils getJannovarUtils(final String serPath) {
        if((new File(serPath).exists()) && genomeAssembly != null && transcriptSource != null) {
            return new JannovarUtils(genomeAssembly, transcriptSource, serPath);
        } else {
            return null;
        }
    }
}
