package org.mulinlab.varnote.cmdline.txtreader.abs;

import com.intel.gkl.compression.IntelInflaterFactory;
import htsjdk.samtools.util.BlockGunzipper;
import htsjdk.samtools.util.Log;
import htsjdk.samtools.util.zip.InflaterFactory;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.config.param.output.OutParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.format.Format;

import java.util.ArrayList;
import java.util.List;

public abstract class QueryReader<T> extends AbstractQueryReader<T> {

    public static final String DB_BEGIN = "[db]";
    public static final String QUERY = "query_file";
    public static final String QUERY_FORMAT = "query_format";
    public static final String QUERY_CHROM = "chrom";
    public static final String QUERY_BEGIN = "begin";
    public static final String QUERY_END = "end";
    public static final String QUERY_REF = "ref";
    public static final String QUERY_ALT = "alt";
    public static final String ZERO_BASED = "zero_based";
    public static final String HAS_HEADER = "has_header";
    public static final String HEADER = "header";
    public static final String HEADER_PATH = "header_path";
    public static final String COMMNT_INDICATOR = "comment_indicator";

    public static final String DB_PATH = "db_path";
    public static final String DB_INDEX_TYPE = "db_index_type";
    public static final String DB_LABEL= "db_tag";
    public static final String DB_MODE = "db_mode";

    public static final String OUTPUT_PATH = "out_file";
    public static final String OUTPUT_MODE = "out_mode";
    public static final String LOJ = "is_loj";
    public static final String ISZIP = "is_zip";
    public static final String REMOVE_COMMENT = "remove_comment";

    public static final String VERBOSE = "is_log";
    public static final String USE_JDK_INFLATER = "use_jdk_inflater";
    public static final String MODE = "mode";
    public static final String THREAD = "thread";
    public static final String ALLOW_LARGE_VARIANTS = "allowLargeVariants";
    public static final String MAX_VARIANT_LENGTH = "maxVariantLength";


    protected List<DBParam> dbParams;

    @Override
    public T read(final String filePath) {
        T obj = super.read(filePath);

        boolean useJDK = GlobalParameter.DEFAULT_USE_JDK, islog = GlobalParameter.DEFAULT_LOG;
        if (valueHash.get(USE_JDK_INFLATER) != null) {
            useJDK = VannoUtils.strToBool(valueHash.get(USE_JDK_INFLATER));
        }

        if(useJDK) {
            BlockGunzipper.setDefaultInflaterFactory(new InflaterFactory());
        } else {
            BlockGunzipper.setDefaultInflaterFactory(new IntelInflaterFactory());
        }

        if (valueHash.get(VERBOSE) != null) {
            islog = VannoUtils.strToBool(valueHash.get(VERBOSE));
        }
        if(!islog)  {
            LoggingUtils.setLoggingLevel(Log.LogLevel.ERROR);
        } else {
            LoggingUtils.setLoggingLevel(Log.LogLevel.INFO);
        }
        return obj;
    }

    @Override
    public void doInit() {
        super.doInit();
        dbParams = new ArrayList<>();
    }

    @Override
    public boolean filterLine(String line) {
        boolean flag = super.filterLine(line);
        if(flag) {
            return true;
        } else if(line.trim().equalsIgnoreCase(DB_BEGIN)) {
            if(valueHash.get(DB_PATH) != null) {
                setDB();
            }

            return true;
        } else return false;
    }

    @Override
    public void processLine(String line) {
        super.processLine(line);
    }

    protected void setDB() {
        DBParam dbParam = new DBParam(valueHash.get(DB_PATH));

        if(valueHash.get(DB_MODE) != null) {
            dbParam.setIntersect(valueHash.get(DB_MODE));
        }
        if(valueHash.get(DB_INDEX_TYPE) != null) {
            dbParam.setIndexType(valueHash.get(DB_INDEX_TYPE));
        }
        if(valueHash.get(DB_LABEL) != null) {
            dbParam.setOutName(valueHash.get(DB_LABEL));
        }
        dbParams.add(dbParam);
    }

    protected OutParam setOutParam(OutParam outParam) {
        if(valueHash.get(OUTPUT_PATH) != null) outParam.setOutputPath(valueHash.get(OUTPUT_PATH));
        if(valueHash.get(LOJ) != null) outParam.setLoj(VannoUtils.strToBool(valueHash.get(LOJ)));
        if(valueHash.get(ISZIP) != null) outParam.setGzip(VannoUtils.strToBool(valueHash.get(ISZIP)));

        return outParam;
    }

    protected Format setformatHeader(Format format) {
        if(valueHash.get(HEADER_PATH) != null) format.setHeaderPath(valueHash.get(HEADER_PATH));
        if(valueHash.get(HAS_HEADER) != null) format.setHasHeaderInFile(VannoUtils.strToBool(valueHash.get(HAS_HEADER)));
        if(valueHash.get(HEADER) != null) format.setHeaderPart(VannoUtils.parserHeader(valueHash.get(HEADER), format.getDelimStr()));
        return format;
    }

    protected Format setformat(Format format) {
        if(valueHash.get(COMMNT_INDICATOR) != null) format.setCommentIndicator(valueHash.get(COMMNT_INDICATOR));

        format = setformatHeader(format);
        if(valueHash.get(ZERO_BASED) != null && VannoUtils.strToBool(valueHash.get(ZERO_BASED))) format.setZeroBased();
        if(valueHash.get(QUERY_CHROM) != null) format.sequenceColumn = Integer.parseInt(valueHash.get(QUERY_CHROM));
        if(valueHash.get(QUERY_BEGIN) != null) format.startPositionColumn = Integer.parseInt(valueHash.get(QUERY_BEGIN));
        if(valueHash.get(QUERY_END) != null) format.endPositionColumn = Integer.parseInt(valueHash.get(QUERY_END));
        if(valueHash.get(QUERY_REF) != null) format.refPositionColumn = Integer.parseInt(valueHash.get(QUERY_REF));
        if(valueHash.get(QUERY_ALT) != null) format.altPositionColumn = Integer.parseInt(valueHash.get(QUERY_ALT));

        if(valueHash.get(ALLOW_LARGE_VARIANTS) != null) format.setAllowLargeVariants(VannoUtils.strToBool(valueHash.get(ALLOW_LARGE_VARIANTS)));
        if(valueHash.get(MAX_VARIANT_LENGTH) != null) format.setMaxVariantLength(Integer.parseInt(valueHash.get(MAX_VARIANT_LENGTH)));
        return format;
    }

    protected void checkDB() {
        if(dbParams.size() == 0) throw new InvalidArgumentException("No database found, database is indicated by the " + DB_BEGIN + " flag.");
    }

    @Override
    public T doEnd() {
        if(valueHash.get(DB_PATH) != null) setDB();
        return null;
    }
}
