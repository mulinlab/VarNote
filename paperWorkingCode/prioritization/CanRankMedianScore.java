package benchmark.final_release;

import benchmark.can.FeatureSort;
import javafx.util.Pair;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.operations.decode.LocCodec;
import org.mulinlab.varnote.operations.decode.TABLocCodec;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CanRankMedianScore {
    public static final int NO_SCORE = -999;

    private String intervalFile;
    private String simulatedFile;
    private Map<String, List<LocFeature>> geneMap;
    private int intervalScoreIndex = 8;
    private int simulatedScoreIndex = 8;

    private double defaultScore = -1;
    private String outFloder;
    private String outName;
    private List<String> variantExclude;

    private int VFIndex = 8;
    private int BENCHVFIndex = 10;

    public CanRankMedianScore(String intervalFile, String simulatedFile, String outFloder, String outName, int intervalScoreIndex, int simulatedScoreIndex, double defaultScore) {
        this.intervalFile = intervalFile;
        this.intervalScoreIndex = intervalScoreIndex;
        this.simulatedFile = simulatedFile;
        this.outName = outName;
        this.outFloder = outFloder;
        this.simulatedScoreIndex = simulatedScoreIndex;
        this.defaultScore = defaultScore;

        //variant type to exclude
        variantExclude = new ArrayList<>();
        variantExclude.add("transcript_variant");
        variantExclude.add("exon_variant");
        variantExclude.add("gene_variant");
        variantExclude.add("coding_sequence_variant");
        variantExclude.add("splicing_variant");
        variantExclude.add("coding_transcript_variant");
        variantExclude.add("stop_retained_variant");
        variantExclude.add("synonymous_variant");
        variantExclude.add("initiator_codon_variant");
        variantExclude.add("splice_region_variant");
        variantExclude.add("disruptive_inframe_deletion");
        variantExclude.add("disruptive_inframe_insertion");
        variantExclude.add("inframe_deletion");
        variantExclude.add("missense_variant");
        variantExclude.add("inframe_insertion");
        variantExclude.add("frameshift_elongation");
        variantExclude.add("exon_loss_variant");
        variantExclude.add("splice_donor_variant");
        variantExclude.add("transcript_amplification");
        variantExclude.add("frameshift_variant");
        variantExclude.add("stop_gained");
        variantExclude.add("splice_acceptor_variant");
        variantExclude.add("transcript_ablation");
        variantExclude.add("stop_lost");
        variantExclude.add("rare_amino_acid_variant");
        variantExclude.add("frameshift_truncation");
        variantExclude.add("start_lost");

        readGeneList();
    }

    public void rank() throws IOException {
        //get median pathogenic score for each method
        List<Node> medianScoreList = getIntervalMedianScore();
        LocCodec codec = new TABLocCodec(VannoUtils.getVCFLikeFormat(), true);

        List<Node> simulatedList = new ArrayList<>();
        File file = new File(simulatedFile);
        NoFilterIterator iterator = new NoFilterIterator(simulatedFile);
        LocFeature locFeature;

        String line;
        while(iterator.hasNext()) {
            line = iterator.next();
            if(!line.startsWith("#")) {
                locFeature = codec.decode(line);
                if(!isExclude(locFeature) && isOverlapGene(locFeature) != null) {
                    simulatedList.add(new Node(locFeature.clone(), simulatedScoreIndex));
                }
            }
        }

        // get rank of the median pathogenic score
        if(simulatedList.size() > 0) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outFloder + file.getName().substring(0, file.getName().indexOf(".")) + "." + outName + ".tsv")));
            for (Node node:medianScoreList) {
                simulatedList.add(node);
                Collections.sort(simulatedList, new RegpSort());
                writer.write(String.format("%s\t%.3f\n", node.toString(), ((double)simulatedList.indexOf(node)/simulatedList.size())));
                simulatedList.remove(node);
            }
            writer.close();
        }
    }

    public boolean isExclude(LocFeature locFeature) {
        for (String ex:variantExclude) {
            if(ex.equalsIgnoreCase(locFeature.parts[BENCHVFIndex])) {
                return true;
            }
        }

        return false;
    }

    public String isOverlapGene(LocFeature loc) {
        List<LocFeature> list = geneMap.get(loc.chr);
        for (LocFeature g:list) {
            if(isOverlap(g, loc)) {
                return g.toString().replace('\t', '_');
            }
        }

        return null;
    }

    public boolean isOverlap(LocFeature region, LocFeature loc) {
        if((loc.beg + 1) > region.end || loc.end < (region.beg + 1)) {
            return false;
        } else {
            return true;
        }
    }

    public List<Node> getIntervalMedianScore() throws IOException {
        LocCodec codec = new TABLocCodec(VannoUtils.getCoordAlleleFormat(), true);

        List<Node> features = new ArrayList<>();
        NoFilterIterator iterator = new NoFilterIterator(intervalFile);
        LocFeature locFeature;
        String group = "";
        List<LocFeature> list = null;

        iterator.next();
        while(iterator.hasNext()) {
            locFeature = codec.decode(iterator.next());

            if(!locFeature.parts[6].equals(group)) {
                if(list != null) {
                    LocFeature locFeature1 = getMedian(list);
                    if(locFeature1 != null) {
                        features.add(new Node(locFeature1, intervalScoreIndex));
                    }
                }
                group = locFeature.parts[6];
                list = new ArrayList<>();
            }

            if(!isExclude(locFeature) && isOverlapGene(locFeature) != null) {
                list.add(locFeature.clone());
            }
        }


        if(list != null) {
            LocFeature locFeature1 = getMedian(list);
            if(locFeature1 != null) {
                features.add(new Node(locFeature1, intervalScoreIndex));
            }
        }

        iterator.close();
        return features;
    }

    public LocFeature getMedian(List<LocFeature> list)  {
        if (list.size() > 0){
            Collections.sort(list, new FeatureSort(intervalScoreIndex));

            int size = list.size();
            if(size > 1) {
                if(size % 2 == 0) {
                    return list.get((int)size/2 - 1);
                } else {
                    return list.get((int)size/2 );
                }
            } else {
                return list.get(0);
            }
        } else {
            return null;
        }
    }

    class RegpSort implements Comparator<Node> {
        public int compare(Node a, Node b)
        {
            return (a.score > b.score ? -1 : (a.score == b.score ? 0 : 1));
        }
    }

    public void readGeneList() {
        geneMap = new HashMap<>();
        NoFilterIterator iterator = new NoFilterIterator("～/can/ref/gencode.v32lift37.annotation.gene.sort.gtf.gz");

        Format format = Format.newTAB();
        format.sequenceColumn = 1;
        format.startPositionColumn = 4;
        format.endPositionColumn = 5;

        TABLocCodec locCodec = new TABLocCodec(format, false);

        LocFeature locFeature;
        List<LocFeature> list;
        while(iterator.hasNext()) {
            locFeature = locCodec.decode(iterator.next());

            locFeature.beg -= 10000;
            locFeature.end += 10000;
            locFeature.origStr = "";

            locFeature.chr = locFeature.chr.replace("chr", "");
            if(geneMap.get(locFeature.chr) == null) {
                list = new ArrayList<>();
            } else {
                list = geneMap.get(locFeature.chr);
            }
            list.add(locFeature.clone());

            geneMap.put(locFeature.chr, list);
        }
        iterator.close();
    }

    class Node {
        private LocFeature feature;
        private double score;

        public Node(LocFeature feature, int scoreIndex) {
            this.feature = feature;
            this.score = Double.parseDouble(feature.parts[scoreIndex]);
            if((int)(this.score) == NO_SCORE || (int)(this.score) == NO_SCORE) {
                this.score = defaultScore;
            }
        }

        public String toString() {
            return feature.chr + "\t" + (feature.beg + 1) + "\t" + feature.ref + "\t" + feature.alt  + "\t" + score;
        }
    }

    public static void main(final String[] args) throws IOException {
        Map<String, Pair<Integer, Double>> keyMap = new HashMap<>();

        keyMap.put("CADD", new Pair<>(11, 6.5945)); keyMap.put("DANN", new Pair<>(13, 11.3128));
        keyMap.put("FATHMM-MKL", new Pair<>(15, 7.9202)); keyMap.put("FunSeq2", new Pair<>(17, 3.4168));
        keyMap.put("Eigen", new Pair<>(19, 10.6865)); keyMap.put("Eigen_PC", new Pair<>(21, 6.6139));
        keyMap.put("GenoCanyon", new Pair<>(23, 2.9673)); keyMap.put("FIRE", new Pair<>(25, 3.1409));
        keyMap.put("ReMM", new Pair<>(27, 12.2018)); keyMap.put("LINSIGHT", new Pair<>(29, 4.1429));
        keyMap.put("fitCons", new Pair<>(31, 4.353)); keyMap.put("FATHMM-XF", new Pair<>(33, 10.9984));
        keyMap.put("CScape", new Pair<>(35, 14.8384)); keyMap.put("CDTS", new Pair<>(37, 2.9269));

        String simulatedFolder = "~/can/data/collaborary/";
        String outFolder = "~/can/data/rank/";
        String intervalFile = "~/can/data/interval.score.can.txt";
        String benchFile = "~/can/data/interval.score.other.methods.txt";

        for (final File f : new File(simulatedFolder).listFiles()) {
            if (f.getName().endsWith(".can")) {
                CanRankMedianScore getMedian = new CanRankMedianScore(intervalFile, f.getAbsolutePath(), outFolder,"can", 13, 14, 15.9);
                getMedian.rank();
            }
        }

        Pair<Integer, Double> val;
        for (final File f : new File(simulatedFolder).listFiles()) {
            if(f.getName().endsWith(".bench")) {
                for (String key:keyMap.keySet()) {
                    val = keyMap.get(key);
                    CanRankMedianScore getMedian = new CanRankMedianScore(benchFile, f.getAbsolutePath(), outFolder,key, val.getKey()+3, val.getKey()+4, val.getValue());
                    getMedian.rank();
                }
            }
        }
    }
}
