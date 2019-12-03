package org.mulinlab.varnote.utils;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import org.mulinlab.varnote.utils.enumset.GenomeAssembly;
import org.mulinlab.varnote.utils.enumset.TranscriptSource;
import org.mulinlab.varnote.utils.jannovar.ChromosomalRegionIndex;
import org.mulinlab.varnote.utils.jannovar.JannovarVariantAnnotator;
import org.mulinlab.varnote.utils.jannovar.VariantAnnotation;
import org.mulinlab.varnote.utils.node.LocFeature;

public final class JannovarUtils {

    private JannovarVariantAnnotator annotator;
    private final GenomeAssembly genome;
    private final TranscriptSource source;
    private final String dbPath;

    public JannovarUtils(final String dbPath) {
        this.genome = GenomeAssembly.defaultVal();
        this.source = TranscriptSource.defaultVal();
        this.dbPath = dbPath;
    }

    public JannovarUtils(final GenomeAssembly genome, final TranscriptSource source, final String dbPath) {
        this.genome = genome;
        this.source = source;
        this.dbPath = dbPath;
    }

    public void setJannovarData() {
        try {
            JannovarData data = new JannovarDataSerializer(dbPath + genome + "_" + source + ".ser").load();
            annotator = new JannovarVariantAnnotator(GenomeAssembly.hg38, data, ChromosomalRegionIndex
                    .empty());
        } catch (SerializationException e) {
            e.printStackTrace();
        }
    }

    public VariantAnnotation annotate(final LocFeature locFeature) {
        return annotator.annotate(locFeature.chr, locFeature.beg, locFeature.ref, locFeature.alt);
    }
}
