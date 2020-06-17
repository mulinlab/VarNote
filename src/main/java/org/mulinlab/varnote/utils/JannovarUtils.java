package org.mulinlab.varnote.utils;


import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.mulinlab.varnote.utils.enumset.GenomeAssembly;
import org.mulinlab.varnote.utils.enumset.TranscriptSource;
import org.mulinlab.varnote.utils.jannovar.*;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public final class JannovarUtils {

    private JannovarVariantAnnotator annotator;
    private final String path;
    private GenomeAssembly genome;
    private JannovarData data;

    public JannovarUtils(String path) {
        this.path = path;
    }
    public JannovarUtils(final GenomeAssembly genome, final TranscriptSource source, final String serPath) {
        this.genome = genome;
        this.path = serPath + genome + "_" + source + ".ser";
    }

    public void setJannovarData() {
        try {
            if(data == null) {
                data = new JannovarDataSerializer(path).load();
                annotator = new JannovarVariantAnnotator(genome, data, ChromosomalRegionIndex.empty());
            }
        } catch (SerializationException e) {
            e.printStackTrace();
        }
    }

    public VariantAnnotation annotate(final LocFeature locFeature) {
        return annotator.annotate(locFeature.chr, locFeature.beg + 1, locFeature.ref, locFeature.alt);
    }

    public VariantAnnotation annotate(final String chr, final Integer pos, final String ref, final String alt) {
        return annotator.annotate(chr, pos, ref, alt);
    }

    public static String getSerPath(final String dataPath) {
        return dataPath + File.separator + "ser" + File.separator;
    }

    public JannovarData getData() {
        return data;
    }

    public List<Gene> getGeneList(final List<String> list) {
        List<Gene> genes = new ArrayList<>(list.size());
        ImmutableMultimap<String, TranscriptModel> set = data.getTmByGeneSymbol();

        ImmutableCollection<TranscriptModel> models;
        GeneBuilder geneBuilder;
        for (String geneName:list) {
            models = set.get(geneName);

            geneBuilder = new GeneBuilder(data.getRefDict(), geneName);
            for (TranscriptModel model:models) {
                geneBuilder.addTranscriptModel(model);
            }

            genes.add(geneBuilder.build());
        }

        return genes;
    }

    public GenomeAssembly getGenome() {
        return genome;
    }

    public void setGenome(GenomeAssembly genome) {
        this.genome = genome;
    }
}
