package org.mulinlab.varnote.cmdline.collection;

import org.broadinstitute.barclay.argparser.Argument;
import org.mulinlab.varnote.utils.JannovarUtils;
import org.mulinlab.varnote.utils.enumset.GenomeAssembly;
import org.mulinlab.varnote.utils.enumset.TranscriptSource;


public final class VariantEffectArgumentCollection {
    private static final long serialVersionUID = 1L;

    @Argument( shortName = "SP", fullName = "SERPath", optional = true, doc = "Path of annotation databases(.ser) for variant effects.")
    public String serPath;

    @Argument( shortName = "G", fullName = "Genome", optional = true, doc = "Genome reference assembly version.")
    public GenomeAssembly genomeAssembly = null;

    @Argument( shortName = "TS", fullName = "TSource", optional = true, doc = "The supported transcript sources.")
    public TranscriptSource transcriptSource = null;

    public JannovarUtils getJannovarUtils() {
        if(serPath != null && genomeAssembly != null && transcriptSource != null) {
            return new JannovarUtils(genomeAssembly, transcriptSource, serPath);
        } else {
            return null;
        }
    }
}
