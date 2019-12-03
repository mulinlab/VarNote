package org.mulinlab.varnote.config.parser;

import htsjdk.tribble.util.ParsingUtils;
import org.mulinlab.varnote.config.io.temp.ThreadPrintter;
import org.mulinlab.varnote.config.run.AdvanceToolRunConfig;
import org.mulinlab.varnote.utils.JannovarUtils;
import org.mulinlab.varnote.utils.enumset.CellType;
import org.mulinlab.varnote.utils.jannovar.VariantAnnotation;
import org.mulinlab.varnote.utils.node.LocFeature;

import javax.xml.transform.sax.SAXSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class REGParser extends AbstractParser {
    private Map<String, CellMark> cellMarkMap;
    private int count;

    private final String[] token = new String[2];

    private final ThreadPrintter printter;
    private final List<CellType> cellTypes;
    private JannovarUtils jannovarUtils = null;

    public REGParser(final ThreadPrintter printter, final List<CellType> cellTypes, final JannovarUtils jannovarUtils) {
        setDefaultCellMark();
        this.count = 0;
        this.printter = printter;
        this.cellTypes = cellTypes;
        this.jannovarUtils = jannovarUtils;
    }

    @Override
    public String processNode(LocFeature query, Map<String, LocFeature[]> dbNodeMap) {
		LocFeature[] roadmapFeatures = dbNodeMap.get(AdvanceToolRunConfig.ROAD_MAP_LABEL);
        VariantAnnotation annotation = null;

        if(query.chr.equals("1")) {
            System.out.println();
        }

        double combined_p = 0.1, reg_p = -1, prior_p = 0.5, a = 0.5;


        LocFeature regBase = getRegBase(query, dbNodeMap.get(AdvanceToolRunConfig.REGBASE_MAP_LABEL));
        if(regBase != null) {
            combined_p = Double.parseDouble(regBase.parts[5]);
            reg_p = Double.parseDouble(regBase.parts[6]);
        }

        try {
            Map<String, List<CellMark>> cellMap = new HashMap<>();
            List<CellMark> marks;
            if(roadmapFeatures != null) {
                for (LocFeature feature: roadmapFeatures) {
                    ParsingUtils.split(feature.parts[10], token, '-', true);

                    if(cellMap.get(token[0]) == null) {
                        marks = new ArrayList<>();
                    } else {
                        marks = cellMap.get(token[0]);
                    }

                    marks.add(new CellMark(token[1], 1, Integer.parseInt(feature.parts[4]),
                            Math.abs( Integer.parseInt(feature.parts[9]) - ( query.beg - Integer.parseInt(feature.parts[1])))));
                    cellMap.put(token[0], marks);
                }
            }


            if(jannovarUtils != null) {
                annotation = jannovarUtils.annotate(query);
                query.origStr += "\t" + annotation.getVariantEffect() + "\t" + annotation.getGeneId() + "\t" + annotation.getGeneSymbol();
            }

            double score;
            for (CellType cell: cellTypes) {
                score = compute(cellMap.get(cell.toString()));
                printter.print(String.format("%s\t%s\t%f\t%f\t%f\t%f", query.origStr, cell, score, combined_p, reg_p, a*(score * combined_p)/prior_p));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.count++;
		return null;
	}

    @Override
    public void printLog() { }

    private double compute(final List<CellMark> marks) {
        setDefaultCellMark();

        if(marks != null)
            for (CellMark cellMark: marks) {
                cellMarkMap.put(cellMark.name, cellMark);
            }

        CellMark H3K4me1 = cellMarkMap.get("H3K4me1");
        CellMark DNase = cellMarkMap.get("DNase");
        CellMark H3K36me3 = cellMarkMap.get("H3K36me3");
        CellMark H3K79me2 = cellMarkMap.get("H3K79me2");
        CellMark H3K4me2 = cellMarkMap.get("H3K4me2");
        CellMark H3K9me3 = cellMarkMap.get("H3K9me3");
        CellMark H3K27me3 = cellMarkMap.get("H3K27me3");
        CellMark H3K4me3 = cellMarkMap.get("H3K4me3");

        double prior = 1 / (
                1 + Math.exp( -( -0.5339527052 + 1.0513562209 * H3K4me1.hit + 1.5659681399 * H3K36me3.hit + 1.2131942069 * DNase.hit + 0.9750312605 * H3K79me2.hit
                        + -0.4843821400 * H3K9me3.hit + 1.5150317212 * H3K27me3.hit + 0.0008691201 * H3K4me2.score +
                        0.0003089830 * H3K4me3.score + 0.0043517819 * H3K36me3.score + -0.0001497833 * H3K79me2.centrality))
        );
        if (prior < 0.3696304) {
            prior = 0.3696304;
        }
        return prior;
    }

    private void setDefaultCellMark() {
        if(cellMarkMap == null) {
            String[] marks = new String[]{"DNase", "H3K27ac", "H3K27me3", "H3K36me3", "H3K4me1", "H3K4me2", "H3K4me3", "H3K79me2", "H3K9me3"};
            cellMarkMap = new HashMap<>();
            for (String mark: marks) {
                cellMarkMap.put(mark, new CellMark(mark));
            }
        } else {
            for (CellMark mark: cellMarkMap.values()) {
                mark.clear();
            }
        }
    }

    private final class CellMark {
        String name;
        int hit = 0;
        int score = 0;
        int centrality = -1;

        public CellMark(final String name) { this.name = name; }

        public CellMark(String name, int hit, int score, int centrality) {
            this.name = name;
            this.hit = hit;
            this.score = score;
            this.centrality = centrality;
        }

        public void clear() {
            hit = 0;
            score = 0;
            centrality = -1;
        }
    }
}
