package org.mulinlab.varnote.utils.enumset;

import java.util.Arrays;
import java.util.Objects;

public enum TranscriptSource {
    ensembl("ensembl"),
    refseq("refseq"),
    UCSC("ucsc");

    private final String value;

    TranscriptSource(String value) {
        this.value = value;
    }

    public static TranscriptSource parseValue(String value) {
        Objects.requireNonNull(value, "Transcript source cannot be null");
        switch (value.toLowerCase()) {
            case "ensembl":
                return ensembl;
            case "refseq":
                return refseq;
            case "ucsc":
                return UCSC;
            default:
                String message = String.format("'%s' is not a valid/supported transcript source. Valid sources are: %s", value, Arrays
                        .asList(TranscriptSource.values()));
                throw new InvalidTranscriptSourceException(message);
        }
    }

    public static TranscriptSource defaultVal() {
        return TranscriptSource.refseq;
    }

    @Override
    public String toString() {
        return value;
    }

    private static class InvalidTranscriptSourceException extends RuntimeException {
        private InvalidTranscriptSourceException(String message) {
            super(message);
        }

    }
}
