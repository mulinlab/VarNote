package benchmark.final_release;

import benchmark.reg.RankSort;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.operations.decode.LocCodec;
import org.mulinlab.varnote.operations.decode.TABLocCodec;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class RegRankScore {
    public static final int SCORE_START_INDEX = 10;
    public static final int SCORE_COMBINE_INDEX = 21;

    private String scoreFile;
    private String groupFile;
    private String[] headers;

    private List<String> excludeList;
    private List<String> diseaseList;
    private HashMap<String, List<LocFeature>> ldMap;

    public RegRankScore(final String scoreFile, final String groupFile) {
        this.scoreFile = scoreFile;
        this.groupFile = groupFile;

        //variant type to exclude
        this.excludeList = new ArrayList<>();
        this.excludeList.add("missense");
        this.excludeList.add("nearsplice");
        this.excludeList.add("frameshift");
        this.excludeList.add("nonsense");

        //diseases to include
        this.diseaseList = new ArrayList<>();
        this.diseaseList.add("C_reactive_protein");
        this.diseaseList.add("Vitiligo");
        this.diseaseList.add("Asthma");
        this.diseaseList.add("Atopic_dermatitis");
        this.diseaseList.add("Allergy");
        this.diseaseList.add("Rheumatoid_arthritis");
        this.diseaseList.add("Juvenile_idiopathic_arthritis");
        this.diseaseList.add("Type_1_diabetes");
        this.diseaseList.add("Autoimmune_thyroiditis");
        this.diseaseList.add("Behcets_disease");
        this.diseaseList.add("Primary_sclerosing_cholangitis");
        this.diseaseList.add("Primary_biliary_cirrhosis");
        this.diseaseList.add("Multiple_sclerosis");
        this.diseaseList.add("Celiac_disease");
        this.diseaseList.add("Systemic_lupus_erythematosus");
        this.diseaseList.add("Systemic_sclerosis");
        this.diseaseList.add("Ankylosing_spondylitis");
        this.diseaseList.add("Alopecia_areata");
        this.diseaseList.add("Psoriasis");
        this.diseaseList.add("Crohns_disease");
        this.diseaseList.add("Ulcerative_colitis");
        this.diseaseList.add("Kawasaki_disease");
    }

    public void rank() throws IOException {
        getLDMap();

        BufferedWriter output =  new BufferedWriter(new FileWriter(new File(scoreFile.replace(".gz", ".srank.txt"))));
        List<List<Node>> groups = getGroup();

        Map<String, LocFeature> dataMap;
        for (List<Node> group:groups) {
            dataMap = new HashMap<>();
            for (Node d : group) {
                List<LocFeature> ldData = ldMap.get(d.rsid);
                if (ldData != null && ldData.size() >= 5) {
                    dataMap = addData(dataMap, ldData, d);

                    int rank = 0;
                    if (ldData.size() >= 5) {
                        Collections.sort(ldData, new RankSort(SCORE_COMBINE_INDEX, true));
                        for (LocFeature loc : ldData) {
                            if (loc.parts[0].equals("Query")) {
                                rank = ldData.indexOf(loc) + 1;

                                boolean filter = false;
                                for (int i = SCORE_START_INDEX + 1; i <= SCORE_START_INDEX + 5; i++) {
                                    if (Double.parseDouble(loc.parts[i]) <= 0.001) {
                                        filter = true;
                                        break;
                                    }
                                }

                                if (!filter) {
                                    for (int i = SCORE_START_INDEX + 1; i <= SCORE_START_INDEX + 5; i++) {
                                        output.write(headers[i] + "\t" + loc.parts[i] + "\t" + (Double.parseDouble(loc.parts[i + 5])) + "\t" + (Double.parseDouble(loc.parts[i + 5])) / ldData.size() + "\t" + d.prob + "\t" + d.group + "\t" + loc.parts[0] + "\t" + d.rsid + "\n");
                                    }
                                    output.write(headers[SCORE_COMBINE_INDEX] + "\t" + loc.parts[SCORE_COMBINE_INDEX] + "\t" + rank + "\t" + (double) rank / ldData.size() + "\t" + d.prob + "\t" + d.group + "\t" + loc.parts[0] + "\t" + d.rsid + "\n");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public List<LocFeature> rankify(List<LocFeature> list, int index, boolean asc)
    {
        int n = list.size();
        for (int i = 0; i < n; i++) {
            int r = 1, s = 1;

            for (int j = 0; j < n; j++)
            {
                if(asc) {
                    if (j != i && Double.parseDouble(list.get(j).parts[index]) < Double.parseDouble(list.get(i).parts[index]))
                        r += 1;
                } else {
                    if (j != i && Double.parseDouble(list.get(j).parts[index]) > Double.parseDouble(list.get(i).parts[index]))
                        r += 1;
                }

                if (j != i && Double.parseDouble(list.get(j).parts[index]) == Double.parseDouble(list.get(i).parts[index]))
                    s += 1;
            }
            if(index < 20) {
                list.get(i).parts[index + 5] = String.valueOf(r + (float)(s - 1) / (float) 2);
            } else {
                list.get(i).parts[6] = String.valueOf(r + (float)(s - 1) / (float) 2);
            }
        }
        return list;
    }

    public double countLDNum(List<LocFeature> groupData) {
        int ld = 0, query = 0;

        for (LocFeature loc:groupData) {
            if(loc.parts[0].equals("LD")) {
                ld++;
            } else if(loc.parts[0].equals("Query")) {
                query++;
            }
        }

        if(query > 0) {
            return (double)ld/query;
        } else {
            return -1;
        }
    }

    public void getLDMap() {
        ldMap = new HashMap<>();

        Format format = Format.newTAB();
        format.sequenceColumn = 2;
        format.startPositionColumn = 3;
        format.endPositionColumn = 3;
        format.refPositionColumn = 4;
        format.altPositionColumn = 5;

        LocCodec codec = new TABLocCodec(format, true);

        NoFilterIterator iterator = new NoFilterIterator(scoreFile);
        LocFeature locFeature;
        LocFeature query = null;
        List<LocFeature> ldList = new ArrayList<>();

        headers = iterator.next().split("\t");

        while(iterator.hasNext()) {
            locFeature = codec.decode(iterator.next());
            if(locFeature.parts[0].equals("Query")) {
                if(ldList.size() > 0) {
                    ldMap.put(query.parts[9], ldList);
                }
                query = locFeature.clone();
                ldList = new ArrayList<>();
            }
            ldList.add(locFeature.clone());
        }
        if(ldList.size() > 0) {
            ldMap.put(query.parts[9], ldList);
        }
        iterator.close();
    }

    public Map<String, LocFeature> addData(Map<String, LocFeature> dataMap, List<LocFeature> ldData, Node d) {
        if(ldData != null) {
            for (LocFeature f:ldData) {
                if(dataMap.get(f.posTabStr()) == null) {
                    f.parts[5] = String.valueOf(d.prob);
                    f.parts[7] = String.valueOf(d.group);
                    dataMap.put(f.posTabStr(), f);
                }
            }
        }

        return dataMap;
    }

    public List<List<Node>> getGroup() {
        String  preGroup = "";

        List<List<Node>> allGroups = new ArrayList<>();
        List<Node> group = new ArrayList<>();

        NoFilterIterator iterator = new NoFilterIterator(this.groupFile);
        Node node;

        while(iterator.hasNext()) {
            node = new Node(iterator.next().split("\t"));

            if(!node.group.equals(preGroup)) {
                if(group.size() > 0) {
                    group = filterGroup(group);
                    if(group != null && group.size() > 0) allGroups.add(group);
                }
                preGroup = node.group;
                group = new ArrayList<>();
            }

            if(diseaseList.contains(node.category.trim())) {
                group.add(node);
            }
        }

        iterator.close();
        return allGroups;
    }

    public List<Node> filterGroup(List<Node> groupItems) {
        Node max = null;

        for (Node n:groupItems) {
            if(max == null || n.prob > max.prob) {
                max = n;
            }
        }

        String anno = max.anno.trim();
        if(!excludeList.contains(anno)) {
            List<Node> filterGroup = new ArrayList<>();
            for (Node n:filterGroup) {
                if(!excludeList.contains(n.anno.trim())) {
                    filterGroup.add(n);
                }
            }
            return filterGroup;
        }

        return null;
    }

    class Node {
        private String rsid;
        private String category;
        private String group;
        private double prob;
        private String anno;

        public Node(final String[] token) {
            rsid = token[0];
            category = token[1];
            group = token[2];
            prob = Double.parseDouble(token[3]);
            anno = token[4];
        }

        @Override
        public String toString() {
            return rsid + "\t"  + group + "\t" + prob + "\t" + anno;
        }
    }

    public static void main(final String[] args) throws IOException {
        String folder = "~/reg/data/";

        for (final File f : new File(folder).listFiles()) {
            if(f.getName().endsWith(".gz")) {
                RegRankScore regRank = new RegRankScore(f.getAbsolutePath(), folder + "group.txt");
                regRank.rank();
            }
        }
    }
}
