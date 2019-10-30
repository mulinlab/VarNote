package org.mulinlab.varnote.utils.pedigree;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.pedigree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;


public class PedigreeConverter {

    private static final Logger logger = LoggerFactory.getLogger(PedigreeConverter.class);

    private PedigreeConverter() {
    }

    public static de.charite.compbio.jannovar.pedigree.Pedigree convertToJannovarPedigree(Pedigree pedigree) {
        logger.debug("Converting pedigree");
        ImmutableList<PedPerson> people = pedigree.getIndividuals()
                .stream()
                .map(toPedPerson())
                .collect(ImmutableList.toImmutableList());

        return buildJannovarPedigree(people);
    }

    private static Function<Pedigree.Individual, PedPerson> toPedPerson() {
        return individual -> {
            logger.debug("Converting individual {}", individual);
            Sex sex = mapSex(individual.getSex());
            Disease disease = mapDisease(individual.getStatus());
            return new PedPerson(individual.getFamilyId(), individual.getId(), mapParentId(individual.getFatherId()), mapParentId(individual.getMotherId()), sex, disease);
        };
    }

    private static String mapParentId(String parentId) {
        return parentId.isEmpty() ? "0" : parentId;
    }

    private static Sex mapSex(Pedigree.Individual.Sex sex) {
        switch (sex) {
            case MALE:
                return Sex.MALE;
            case FEMALE:
                return Sex.FEMALE;
            default:
                return Sex.UNKNOWN;
        }
    }

    private static Disease mapDisease(Pedigree.Individual.Status status) {
        switch (status) {
            case AFFECTED:
                return Disease.AFFECTED;
            case UNAFFECTED:
                return Disease.UNAFFECTED;
            default:
                return Disease.UNKNOWN;
        }
    }

    private static de.charite.compbio.jannovar.pedigree.Pedigree buildJannovarPedigree(ImmutableList<PedPerson> people) {
        PedFileContents pedFileContents = new PedFileContents(ImmutableList.of(), people);

        final String name = pedFileContents.getIndividuals().get(0).getPedigree();
        try {
            logger.debug("Building pedigree for family {}", name);
            return new de.charite.compbio.jannovar.pedigree.Pedigree(name, new PedigreeExtractor(name, pedFileContents).run());
        } catch (PedParseException e) {
            String message = "Problem converting pedigree.";
            logger.error(message);
            throw new PedigreeConversionException(message, e);
        }
    }

    private static class PedigreeConversionException extends RuntimeException {
        private PedigreeConversionException(String message, Exception e) {
            super(message, e);
        }
    }
}
