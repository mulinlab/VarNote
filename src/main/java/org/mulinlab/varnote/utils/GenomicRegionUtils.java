package org.mulinlab.varnote.utils;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.operations.decode.BEDLocCodec;
import org.mulinlab.varnote.utils.enumset.Delimiter;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.jannovar.Gene;
import org.mulinlab.varnote.utils.node.LocFeature;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GenomicRegionUtils {
    final static Logger logger = LoggingUtils.logger;

    public static List<String> toLines(final File file) {
        final NoFilterIterator reader = new NoFilterIterator(file.getAbsolutePath(), VannoUtils.checkFileType(file.getAbsolutePath()));
        List<String> list = new ArrayList<>();

        while(reader.hasNext()) {
            list.add(reader.peek());
            reader.next();
        }
        reader.close();

        return list;
    }

    public static List<LocFeature> genesToRegion(final List<Gene> genes, final int shift) {
        List<LocFeature> regions = new ArrayList<>(genes.size());
        LocFeature region;
        GenomeInterval interval;

        for (Gene gene:genes) {
            interval = gene.getRegion();
            if(interval != null) {
                region = new LocFeature(interval.getBeginPos() - shift, interval.getEndPos() + shift, gene.getRefDict().getContigIDToName().get(gene.getRegion().getChr()));
                regions.add(region);
                logger.info(String.format("Convert gene  %s to region %s:%d-%d.", gene.getName(), region.chr, region.beg, region.end));
            } else {
                logger.info(String.format("Convert gene %s to region failed.", gene.getName()));
            }
        }

        return regions;
    }

    public static List<LocFeature> strToRegion(final List<String> lines) {
        List<LocFeature> regions = new ArrayList<>(lines.size());

        if(lines.size() > 0) {
            for (String line:lines) {
                regions.add(VannoUtils.regionToNode(line));
            }
        }

        return regions;
    }

    public static Delimiter checkFormat(String line, Format format) {
        Delimiter delimiter = Delimiter.TAB;
		String[] strings = line.split(delimiter.getCStr());

		if (strings.length < 2) {
            delimiter = Delimiter.COMMA;
        }

        strings = line.split(delimiter.getCStr());
        if (strings.length < 2) {
            throw new InvalidArgumentException(String.format("Columns of region line '%s' should be delimited by TAB or COMMA.", line));
        }

        try {
            Integer.parseInt(strings[format.startPositionColumn - 1]);
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException(String.format("Begin column should be a number, but we get ", strings[format.startPositionColumn - 1]));
        }

        try {
            Integer.parseInt(strings[format.endPositionColumn - 1]);
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException(String.format("End column should be a number, but we get ", strings[format.endPositionColumn - 1]));
        }

        return delimiter;
    }
}
