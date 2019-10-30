package org.mulinlab.varnote.config.parser;

import htsjdk.tribble.util.ParsingUtils;
import org.mulinlab.varnote.config.io.temp.ThreadPrintter;
import org.mulinlab.varnote.config.run.CEPIPRunConfig;
import org.mulinlab.varnote.utils.node.LocFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegParser implements ResultParser {
    private Map<String, CellMark> cellMarkMap;
    private int count;
    private final ThreadPrintter printter;
    private final String[] token = new String[2];

    public RegParser(final ThreadPrintter printter) {
        setDefaultCellMark();
        this.count = 0;
        this.printter = printter;
    }


//    @Override
//    public String processNode(LocFeature query, Map<String, LocFeature[]> dbNodeMap) {
//        try {
//            LocFeature[] features = dbNodeMap.get(CEPIPRunConfig.ROAD_MAP_LABEL);
//
//            count++;
//            String begin = "";
//            String[] token = null;
//            if (features != null) {
//
//                Map<String, List<CellMark>> cellMap = new HashMap<>();
//                List<CellMark> marks = null;
//                for (LocFeature feature : features) {
//
//                    token = feature.parts[10].split("-");
//
//
//                    if (cellMap.get(token[0]) == null) {
//                        marks = new ArrayList<>();
//                    } else {
//                        marks = cellMap.get(token[0]);
//                    }
//                    marks.add(new CellMark(token[1], 1, Integer.parseInt(feature.parts[4]),
//                            Math.abs(Integer.parseInt(feature.parts[9]) - (query.beg - Integer.parseInt(feature.parts[1])))));
//                    cellMap.put(token[0], marks);
//                }
//
//                begin = query.origStr;
//                List<CellMark> E116 = cellMap.get("E116");
//                if (E116 != null) {
//                    for (CellMark cellMark : E116) {
//                        if (cellMark.hit == 1) {
//                            printter.print(String.format("%s\t%d\t%s\t%s\t%f", query.origStr, count, "E116", cellMark.name, compute(E116)));
//                        }
//                    }
//                }
//
//                begin = query.origStr;
//                List<CellMark> E124 = cellMap.get("E124");
//                if (E124 != null) {
//                    for (CellMark cellMark : E124) {
//                        if (cellMark.hit == 1) {
//                            printter.print(String.format("%s\t%d\t%s\t%s\t%f", query.origStr, count, "E124", cellMark.name, compute(E124)));
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    @Override
    public String processNode(LocFeature query, Map<String, LocFeature[]> dbNodeMap) {
		LocFeature[] roadmapFeatures = dbNodeMap.get(CEPIPRunConfig.ROAD_MAP_LABEL);
        LocFeature[] regbaseFeature = dbNodeMap.get(CEPIPRunConfig.REGBASE_MAP_LABEL);

        double combined_p = 1, prior_p = 0.5;
        if(regbaseFeature != null) {
            for (LocFeature locFeature: regbaseFeature) {
                if(locFeature.parts[3].equals(query.ref) && locFeature.parts[4].equals(query.alt) && !locFeature.parts[5].equals(".")) {
                    combined_p = Double.parseDouble(locFeature.parts[5]);
                }
            }
        }

		if(roadmapFeatures != null) {
            try {
                Map<String, List<CellMark>> cellMap = new HashMap<>();
                List<CellMark> marks;
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

                double score;
                for (String cell: cellMap.keySet()) {
                    score = compute(cellMap.get(cell));
                    printter.print(String.format("%s\t%s\t%f", query.origStr, cell, (score * combined_p)/prior_p));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
		}
		return null;
	}

    @Override
    public void printLog() { }

    private double compute(final List<CellMark> marks) {
        setDefaultCellMark();

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
                        0.0003089830 * H3K4me3.score + 0.0043517819 * H3K4me3.score + -0.0001497833 * H3K36me3.centrality))
        );
        if ( prior < 0.3696304 ) {
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
