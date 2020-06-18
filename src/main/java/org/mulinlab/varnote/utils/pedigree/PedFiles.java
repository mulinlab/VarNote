package org.mulinlab.varnote.utils.pedigree;

import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.utils.LoggingUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.mulinlab.varnote.utils.pedigree.Pedigree.Individual;

public class PedFiles {

    final static Logger logger = LoggingUtils.logger;

    private PedFiles() {
    }

    public static Pedigree readPedigree(Path pedFile) {
        try (Stream<String> lines = Files.lines(pedFile)) {
            return parsePedigree(lines);
        } catch (Exception e) {
            logger.error("Error reading PED file {}", pedFile, e);
            throw new PedFilesException(String.format("Error reading PED file %s", pedFile), e);
        }
    }

    public static Pedigree parsePedigree(Stream<String> lines) {
        Set<Individual> individuals = lines
                .filter(line -> !line.startsWith("#"))
                .map(toIndividual())
                .collect(ImmutableSet.toImmutableSet());
        return Pedigree.of(individuals);
    }

    private static Function<String, Individual> toIndividual() {
        //    #Family_ID	Individual_ID	Paternal_ID	Maternal_ID	sex	Phenotype
        //    1	Eva	0	0	2	1
        //    1	Adam	0	0	1	1
        //    1	Seth	Adam	Eva	1	2
        //Strictly this should be a tab delimited format, but there have been instances where users input spaces.
        return line -> {
            String[] tokens = line.split("\\s+");
            if (tokens.length < 6) {
                logger.error("PED file must have at least 6 fields - found {} in line '{}'", tokens.length, line);
                throw new PedFilesParseException(String.format("PED file must have at least 6 fields - found %d in line '%s'", tokens.length, line));
            }
            String familyId = tokens[0];
            String id = parseId(tokens[1]);
            String fatherId = parseParentId(tokens[2]);
            String motherId = parseParentId(tokens[3]);
            Individual.Sex sex = parseSex(tokens[4]);
            Individual.Status status = parseStatus(tokens[5]);
            return Individual.builder()
                    .familyId(familyId)
                    .id(id)
                    .fatherId(fatherId)
                    .motherId(motherId)
                    .sex(sex)
                    .status(status)
                    .build();
        };
    }

    private static String parseId(String token) {
        if (token.isEmpty()) {
            logger.error("Individual id cannot be empty");
            throw new PedFilesParseException("Individual id cannot be empty");
        }
        return token;
    }

    private static String parseParentId(String token) {
        if (token.isEmpty()) {
            logger.error("Parent id cannot be empty");
            throw new PedFilesParseException("Parent id cannot be empty");
        }
        if ("0".equals(token)) {
            // despite empty not being a legal token in the PED file, it's ok for the us.
            return "";
        }
        return token;
    }

    private static Individual.Sex parseSex(String token) {
        switch (token) {
            case "1":
                return Individual.Sex.MALE;
            case "2":
                return Individual.Sex.FEMALE;
            default:
                return Individual.Sex.UNKNOWN;
        }
    }

    private static Individual.Status parseStatus(String token) {
        switch (token) {
            case "-9":
            case "0":
                return Individual.Status.UNKNOWN;
            case "1":
                return Individual.Status.UNAFFECTED;
            case "2":
                return Individual.Status.AFFECTED;
            default:
                logger.error("Individual status must be one of -9 0, 1, 2. Found '{}'", token);
                throw new PedFilesParseException(String.format("Individual status must be one of -9 0, 1, 2. Found '%s'", token));
        }
    }


    private static class PedFilesException extends RuntimeException {

        private PedFilesException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class PedFilesParseException extends RuntimeException {

        private PedFilesParseException(String message) {
            super(message);
        }
    }
}
