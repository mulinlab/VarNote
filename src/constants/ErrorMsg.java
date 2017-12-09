package constants;


public final class ErrorMsg {
   
	//VannoSearch.java
    public static final String ARG_ERROR = "The first argument should be overlap, anno, count or index. you can use 'overlap --help', 'anno --help', 'count --help' or 'index --help' to see how to use it.";
    public static final String MISS_ERROR = "Missing required arguments: ";
    
    //index file cmd
    public static final String INDEX_FILE_PATH_CMD = "The path of file for indexing (only support bgz file now)";
    public static final String INDEX_FILE_PATH_DESC = "Path of file to be indexed";
    
    public static final String INDEX_FILE_FORMAT_CMD = "[bed, vcf, tab] represent for BED, VCF and Tab-delimited format respectively.**Format tab should be used with --columns option together. Ignore this option if your bgz file has a tabix index in the same folder. The format will be extract automatically from thr tabix index.";
    public static final String INDEX_FILE_FORMAT_DESC = "Format of file to be indexed";
    
    public static final String INDEX_FILE_COLUMN_CMD = "The column name is required in case of file format is tab. It must include CHROM,POS(or, alternatively, FROM and TO), and optionally REF and ALT. Column name should be separated by comma(***no space).";
    public static final String INDEX_FILE_COLUMN_DESC = "Comma-separated list of Column Name";
    
    public static final String INDEX_FILE_TYPE_CMD = "-t plus means write plus file for bgz, -t pindex means write plus index, -t all means write plus first and write plus index file then.";
    public static final String INDEX_FILE_TYPE_DESC = "Type";
    
    public static final String INDEX_SKIP_CMD = "Skip first INT lines in the data file";
    public static final String INDEX_COMMENT_CMD = "Skip lines started with character CHAR";
    public static final String INDEX_OUTPUT_CMD = "Output file location";
    public static final String PRINT_HELP = "Print help information";
    public static final String PRINT_VERSION = "Print program version";
    
    public static final String VANNO_RUN_CONFIG = "You can run the program with a configuration file.";
    public static final String VANNO_RUN_CONFIG_DESC = "Config file path";
    
    public static final String VANNO_RUN_THREAD = "The threads used to make process";
    
    public static final String VANNO_RUN_QUERY = "The query file (we support bed, vcf and tab-delimited file, the format can be detect automatically if file end with vcf, bed, vcf.gz, bed.gz or else you should define the option --q-format.)";
    public static final String VANNO_RUN_QUERY_FORMAT = "[bed, vcf, tab] represent for BED, VCF and Tab-delimited format respectively. **Format tab should be used with --columns option together. Files end with vcf, vcf.gz, bed and bed.gz can be detect automatically without this option.";
    
    public static final String VANNO_RUN_OL = "Path of overlap file.";
    public static final String VANNO_RUN_ANNO = "Annotation config file path";
    public static final String VANNO_RUN_EXACT = "[true, false] Exact mode means return database records that has position exactly matches query (query’s start equals database’s start and query’s end equals database’s end).";
    public static final String VANNO_RUN_OUT_MODE = "0: output query only, 1: output database only, 2: output both."; 
    public static final String VANNO_RUN_MODE = "[0, 1, 2] represent for \"tabix search\", \"mix search\" and \"sweep search\" respectively. We recommend using mode 1."; 
    public static final String VANNO_RUN_DB = "The database files. The index file(end with tbi or plus) of database should be put in the same folder with database file."; 
    public static final String VANNO_RUN_DB_INDEX = "[tbi, plus] Which index file to be used? Default is to use plus index file. If plus index file is not exist, try to find and use tbi index file in the same folder."; 
    public static final String VANNO_RUN_DB_LABEL = "Labels for database files, which is separated by comma. Each file has one label.";
    public static final String VANNO_RUN_DB_LABEL_DESC = "Labels for database files";
    public static final String VANNO_RUN_DB_OUT = "Map file location: Folder";
    public static final String VANNO_RUN_DB_LOJ = "loj Perform a 'left outer join'. That is, for each feature in A report each overlap with B. If no overlaps are found, report '.\t.\t.' feature for B if each row in B contains 3 columns.";
    public static final String VANNO_RUN_DB_FC = "Full Closed";
    
    
    //index file error
    public static final String INDEX_FILE_FORMAT_ERR = "We support bed, vcf or tab(requires columns option to be set). but we get: ";
    public static final String NO_FORMAT_ERR = "You should set -f as index file format or you should have a tabix index for the index file.";
    public static final String INDEX_TYPE_ERR = "We only support index type equals plus, pindex and all. but we get: ";

    //FormatWithRef.java
    
    public static final String  MISS_POS = "POS(or, alternatively, FROM and TO) is required.";
    public static final String  MISS = " Columns should be seperated by comma(***no space), Please check.";
    public static final String  MISS_CHR = "CHROM column is required.";
    public static final String  MISS_REF = "REF column is required.";
    public static final String  MISS_ALT = "ALT column is required.";
    
    
    //VannoUtils.java
    public static final String VU_FORMAT_ERR = "We only support tbi(tabix index format) and plus(index by vanno), but we get: ";
    public static final String VU_OUT_MODE_ERR = "We only support outMode equals 0, 1 or 2. 0 means output query only, 1 means output db only and 2 means output both.";
    public static final String VU_STEP_ERR = "We only support step equals to count, overlap, anno and both.";
    public static final String VU_LINE_TYPE_ERR = "We only support type equals 'integer', 'float', 'string', 'character' or 'flag'.";
    public static final String VU_NUM_ERR = "We only support number equals 'integer', '.', 'r', 'g' or 'a'.";
    public static final String VU_MODE_ERR = "Mode can only be 0, 1 or 2. mode=0 means random access search, mode=1 means mix search, mode=2 means sweep search.";
    public static final String VU_DT_ERR = "Cann't determine query format from query name automaticly, you should set q-format or query_file_format in configuration file.";
    public static final String VU_BOOL_ERR = "Parameter expect true or false but get: ";
//    public static final String VU_REST = "Reset columns accoring to: ";
//    public static final String VU_MODE_ERR
}
