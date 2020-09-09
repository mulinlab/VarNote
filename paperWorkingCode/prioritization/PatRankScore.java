package benchmark.final_release;

import benchmark.reg.RankSort;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.operations.decode.LocCodec;
import org.mulinlab.varnote.operations.decode.TABLocCodec;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.FormatType;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PatRankScore {
    private String caseFile;
    private String controlFile;
    private int[] funs;

    public PatRankScore(final String caseFile, final String controlFile) {
        this.caseFile = caseFile;
        this.controlFile = controlFile;
    }

    public List<LocFeature> readFile(String file, FormatType formatType) {
        List<LocFeature> nodes = new ArrayList<>();

        LocCodec codec = null;

        if(formatType == FormatType.COORDALLELE) {
            codec = new TABLocCodec(VannoUtils.getCoordAlleleFormat(), true);
        } else if(formatType == FormatType.VCFLIKE) {
            codec = new TABLocCodec(VannoUtils.getVCFLikeFormat(), true);
        }

        LocFeature locFeature;
        NoFilterIterator iterator = new NoFilterIterator(file);
        while(iterator.hasNext()) {
            locFeature = codec.decode(iterator.next());
            for (int i = 0; i < locFeature.parts.length; i++) {
                if(locFeature.parts[i].equals(".")) {
                    locFeature.parts[i] = "-1";
                }
            }
            nodes.add(locFeature.clone());
        }
        iterator.close();

        return nodes;
    }

    public void rank() throws IOException {
        List<String> header = Arrays.asList("pat\tpat_PHRED\tCADD\tCADD_PHRED\tDANN\tDANN_PHRED\tFATHMM-MKL\tFATHMM-MKL_PHRED\tFunSeq2\tFunSeq2_PHRED\tEigen\tEigen_PHRED\tEigen_PC\tEigen_PC_PHRED\tGenoCanyon\tGenoCanyon_PHRED\tFIRE\tFIRE_PHRED\tReMM\tReMM_PHRED\tLINSIGHT\tLINSIGHT_PHRED\tfitCons\tfitCons_PHRED\tFATHMM-XF\tFATHMM-XF_PHRED\tCScape\tCScape_PHRED\tCDTS\tCDTS_PHRED\tDVAR\tDVAR_PHRED\tFitCons2\tFitCons2_PHRED\tncER\tncER_PHRED\tOrion\tOrion_PHRED\tPAFA\tPAFA_PHRED".split("\t"));
        String[] extract = "CADD\tEigen\tFATHMM-MKL\tFATHMM-XF\tGenoCanyon\tLINSIGHT\tReMM".split("\t");

        funs = new int[extract.length];
        for (int i = 0; i < extract.length; i++) {
            funs[i] = 10 + header.indexOf(extract[i]);
        }

        List<LocFeature> caseNodes =  readFile( caseFile, FormatType.COORDALLELE);
        List<LocFeature> controlNodes =  readFile( controlFile, FormatType.VCFLIKE);

        for (LocFeature locFeature:caseNodes) {
            locFeature.origStr = "case";
            controlNodes.add(locFeature.clone());
        }

        for (int i = 10; i <= 48 ; i=i+2) {
            Collections.sort(controlNodes, new RankSort(i, false));

            for (int j = 0; j < controlNodes.size(); j++) {
                controlNodes.get(j).parts[i+1] = String.valueOf(j + 1) ;
            }
        }

        for (int j = 0; j < controlNodes.size(); j++) {
            controlNodes.get(j).parts[9] = String.valueOf(getProduct(controlNodes.get(j)));
        }

        String outFolder = "~/pat/rank/";
        BufferedWriter writer =  new BufferedWriter(new FileWriter(new File(outFolder + new File(controlFile).getName().replace(".filter.vcf", ".rank.txt"))));
        Collections.sort(controlNodes, new RankSort(9, true));

        for (LocFeature locFeature:controlNodes) {
            if(locFeature.origStr.equals("case")) {
                for (int i = 0; i < extract.length; i++) {
                    writer.write(extract[i] + "\t" + Double.parseDouble(locFeature.parts[funs[i] + 1])/controlNodes.size() + "\t" + locFeature.parts[funs[i] + 1] + "\t" + locFeature.posTabStr() + "\n");
                }
                writer.write( "Comb\t" + (double)(controlNodes.indexOf(locFeature) + 1)/controlNodes.size() + "\t" + (controlNodes.indexOf(locFeature) + 1) + "\t" + locFeature.posTabStr() + "\n");
            }
        }
        writer.close();
    }

    public double getProduct(final LocFeature locFeature) {
        double rank = 1;
        for (int i = 0; i < funs.length; i++) {
            rank = rank * Integer.parseInt(locFeature.parts[funs[i] + 1]);
        }
        return Math.pow(rank, 1.0/(double)funs.length);
    }

    public static void main(final String[] args) throws IOException {
        final String folder = "~/pat/data/";
        int i =0;
        for (final File f : new File(folder).listFiles()) {
            if(f.getName().endsWith(".filter.vcf")) {
                i++;
                System.out.println(f.getName());
                PatRankScore rankProduct = new PatRankScore(folder + "insert_features.txt", f.getAbsolutePath());
                rankProduct.rank();
            }
        }
    }
}
