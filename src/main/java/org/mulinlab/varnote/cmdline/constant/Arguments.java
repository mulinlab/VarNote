package org.mulinlab.varnote.cmdline.constant;

import org.mulinlab.varnote.utils.enumset.FormatType;

public final class Arguments {

    public static final String LOG_LONG = "log";

    //index
    public static final String USE_JDK_SHORT = "UJD";
    public static final String USE_JDK_LONG = "use-jdk-deflater";

    public static final String INPUT_SHORT = "I";
    public static final String INPUT_LONG = "input";

    public static final String INDEX_OUTPUT_SHORT = "O";
    public static final String INDEX_OUTPUT_LONG = "out-folder";

    public static final String INDEX_PRINT_HEADER_SHORT = "PH";
    public static final String INDEX_PRINT_HEADER_LONG = "print-header";

    public static final String INDEX_PRINT_META_SHORT = "PM";
    public static final String INDEX_PRINT_META_LONG = "print-meta-data";

    public static final String INDEX_PRINT_FORMAT_SHORT = "PF";
    public static final String INDEX_PRINT_FORMAT_LONG = "print-format";

    public static final String INDEX_LITS_CHROM_SHORT = "LC";
    public static final String INDEX_LIST_CHROM_LONG = "list-chroms";

    public static final String INDEX_REPLACE_HEADER_SHORT = "RH";
    public static final String INDEX_REPLACE_HEADER_LONG = "reheader";


    //query
    public static final String USE_JDKI_SHORT = "UJI";
    public static final String USE_JDKI_LONG = "use-jdk-inflater";

    public static final String QUERY_INPUT_SHORT = "Q";
    public static final String QUERY_INPUT_LONG = "q-region";

    public static final String QUERY_LABEL_SHORT = "L";
    public static final String QUERY_LABEL_LONG = "is-label";

    //intersect
    public static final String INTERSECT_INPUT_SHORT = "Q";
    public static final String INTERSECT_INPUT_LONG = "query-file";

    public static final String INTERSECT_THREAD_SHORT = "T";
    public static final String INTERSECT_THREAD_LONG = "thread";

    public static final String INTERSECT_COUNT_SHORT = "C";
    public static final String INTERSECT_COUNT_LONG = "is-count";

    public static final String INTERSECT_ZIP_SHORT = "Z";
    public static final String INTERSECT_ZIP_LONG = "is-zip";

    public static final String INTERSECT_OUT_SHORT = "O";
    public static final String INTERSECT_OUT_LONG = "out-file";

    public static final String INTERSECT_OUT_MODE_SHORT = "OM";
    public static final String INTERSECT_OUT_MODE_LONG = "out-mode";

    public static final String INTERSECT_LOJ_SHORT = "loj";
    public static final String INTERSECT_LOJ_LONG = "is-loj";

    public static final String INTERSECT_RC_SHORT = "RC";
    public static final String INTERSECT_RC_LONG = "is-remove-comment";

    //DB
    public static final String DB_INPUT_SHORT = "D";
    public static final String DB_INPUT_LONG = "d-files";


    //format
    public static final String FORMAT_SHORT = "F";
    public static final String FORMAT_LONG = "format";

    public static final String FORMAT_CHROM_SHORT = "C";
    public static final String FORMAT_CHROM_LONG = "chrom";

    public static final String FORMAT_BEGIN_SHORT = "B";
    public static final String FORMAT_BEGIN_LONG = "begin";

    public static final String FORMAT_END_SHORT = "E";
    public static final String FORMAT_END_LONG = "end";

    public static final String FORMAT_REF_LONG = "ref";
    public static final String FORMAT_ALT_LONG = "alt";

    public static final String FORMAT_ZERO_SHORT = "0";
    public static final String FORMAT_ZERO_LONG = "is-zero-based";

    public static final String FORMAT_HEADER_SHORT = "header";
    public static final String FORMAT_HEADER_LONG = "has-header";

    public static final String FORMAT_HEADER_PATH_SHORT = "HP";
    public static final String FORMAT_HEADER_PATH_LONG = "header-path";

    public static final String FORMAT_COMMENT_SHORT = "CI";
    public static final String FORMAT_COMMENT_LONG = "comment-indicator";

    public static final String FORMAT_SKIP_SHORT = "S";
    public static final String FORMAT_SKIP_LONG = "skip";


    //anno
    public static final String ANNO_CONFIG_SHORT = "A";
    public static final String ANNO_CONFIG_LONG = "anno-config";

    public static final String ANNO_FO_SHORT = "FO";
    public static final String ANNO_FO_LONG = "force-overlap";

    public static final String ANNO_VH_SHORT = "VH";
    public static final String ANNO_VH_LONG = "vcf-header-for-bed";

    public static final String ANNO_OF_SHORT = "OF";
    public static final String ANNO_OF_LONG = "out-format";


    public static final String DB_DOC =
            "\nPossible attributes: {index, mode, tag}.\n" +
            "index - The index type that should be used to retrieve data. Default value is \"VarNote\". Possible values: {VarNote, TBI}, optional \n" +
            "mode - Mode of Intersection. default value is \"0\". Possible values: {0, 1, 2}, optional.\n" +
            "\t   0: Intersect mode, perform common interaction operation\n\t      according to query and database formats;\n"  +
            "\t   1: Exact match mode, force the program only to consider\n\t      the chromosome position of database records that exactly match\n\t      the corresponding chromosome position of query;\n"  +
            "\t   2: Full close mode, force the program to report database\n\t      records that overlap both endpoints of query interval regardless\n\t      of original query and database formats.\n"  +
            "tag - A label to rename the database in the output file, optional. By default, the program will use original file name as tag for the database.\n\n";


    public static final String QUERY_DOC = "Path of query file (support plain text or gzip compressed file).\n\n" +
            "Possible Tags: {vcf, vcfLike, bed, bedAllele, coordOnly, coordAllele, tab} \n\n" +

            "Possible attributes for all tags: {sep, ci}\n" +
            "Possible attributes for \"tab\" tag: {c, b, e, ref, alt, 0}\n" +
            "c: column of sequence name (1-based)\n" +
            "b: column of start chromosomal position (1-based)\n" +
            "e: column of end chromosomal position (1-based)\n" +
            "ref: column of reference allele\n" +
            "alt: column of alternative allele\n" +
            "0: specify the position in the data file is 0-based rather than 1-based\n" +
            "sep: specifies the character that separates fields in file, possible values are: {TAB, COMMA}\n" +
            "ci: comment indicator\n";


    public static final String INDEX_DOC = "The path of the TAB-delimited genome position file compressed by bgzip program. The file must be position-sorted (first by sequence name and then by leftmost coordinate). \n\n" +
            "Possible Tags: {vcf, vcfLike, bed, bedAllele, coordOnly, coordAllele, tab} \n\n" +
            "Possible attributes for all tags: {ci}\n" +
            "Possible attributes for \"tab\" tag: {c, b, e, ref, alt, 0}\n" +
            "c: column of sequence name (1-based)\n" +
            "b: column of start chromosomal position (1-based)\n" +
            "e: column of end chromosomal position (1-based)\n" +
            "ref: column of reference allele\n" +
            "alt: column of alternative allele\n" +
            "0: specify the position in the data file is 0-based rather than 1-based\n" +
            "ci: comment indicator\n";

    public static final String FILE_TYPE_DOC = "VCF format is a vcf file with header, VCFLike format   ";
}
