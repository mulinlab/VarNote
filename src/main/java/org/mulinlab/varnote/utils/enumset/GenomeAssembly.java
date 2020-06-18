package org.mulinlab.varnote.utils.enumset;

import java.util.Objects;


public enum GenomeAssembly {

    hg19("hg19"), hg38("hg38");

    private final String value;

    GenomeAssembly(String value) {
        this.value = value;
    }

    public static GenomeAssembly defaultBuild() {
        return GenomeAssembly.hg19;
    }

    public static GenomeAssembly fromValue(String value) {
        Objects.requireNonNull(value, "Genome build cannot be null");
        switch (value.toLowerCase()) {
            case "hg19":
            case "hg37":
            case "grch37":
                return hg19;
            case "hg38":
            case "grch38":
                return hg38;
            default:
                throw new InvalidGenomeAssemblyException(String.format("'%s' is not a valid/supported genome assembly.", value));
        }
    }

    public static GenomeAssembly defaultVal() {
        return GenomeAssembly.hg19;
    }

    @Override
    public String toString() {
        return value;
    }

    public static class InvalidGenomeAssemblyException extends RuntimeException {

        public InvalidGenomeAssemblyException() {
        }

        public InvalidGenomeAssemblyException(String message) {
            super(message);
        }

        public InvalidGenomeAssemblyException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidGenomeAssemblyException(Throwable cause) {
            super(cause);
        }

        public InvalidGenomeAssemblyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}