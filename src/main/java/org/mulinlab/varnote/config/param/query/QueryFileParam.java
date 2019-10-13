package org.mulinlab.varnote.config.param.query;

import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.StringUtil;
import htsjdk.tribble.AsciiFeatureCodec;
import htsjdk.variant.vcf.VCFCodec;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.filters.query.LocFeatureFilter;
import org.mulinlab.varnote.filters.query.VCFContextFilter;
import org.mulinlab.varnote.operations.readers.itf.LongLineReader;
import org.mulinlab.varnote.operations.readers.itf.QueryReaderItf;
import org.mulinlab.varnote.operations.readers.itf.thread.GZIPThreadReader;
import org.mulinlab.varnote.operations.readers.itf.thread.SpiderReader;
import org.mulinlab.varnote.operations.readers.query.AbstractFileReader;
import org.mulinlab.varnote.operations.readers.query.VCFFileReader;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.enumset.FormatType;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.headerparser.BEDHeaderParser;
import org.mulinlab.varnote.utils.stream.BZIP2InputStream;
import org.mulinlab.varnote.utils.stream.GZInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class QueryFileParam extends QueryParam {

    private String queryPath;
    private String queryName;

    private Format queryFormat;
    private FileType fileType;

    private AsciiFeatureCodec codec;
    private Object header;

    protected List<AbstractFileReader> threadReaders;


    public QueryFileParam(final String path) {
        this(path, null);
    }

    public QueryFileParam(final String path, final Format queryFormat) {
//        ThreadReader.readHeader = false;
        if(path == null || path.trim().equals("")) throw new InvalidArgumentException("Query file is required.");

        this.queryPath = VannoUtils.getAbsolutePath(path);
        IOUtil.assertInputIsValid(this.queryPath);

        this.fileType = VannoUtils.checkFileType(path);
        queryName = new File(this.queryPath).getName();

        if(queryFormat == null) this.queryFormat = Format.defaultFormat(path, true);
        else this.queryFormat = queryFormat;
    }


    public void splitFile(int thread) {
        if(threadReaders == null) threadReaders = new ArrayList<>();

        try {
            if (fileType == FileType.BGZ || fileType == FileType.TXT) {
                BZIP2InputStream bz2_text = new BZIP2InputStream(queryPath, thread);
                bz2_text.adjustPos();
                bz2_text.creatSpider();

                if (bz2_text.getThreadNum() != thread) {
                    thread = bz2_text.getThreadNum();
                }

                for (int i = 0; i < thread; i++) {
                    threadReaders.add(getReader(new SpiderReader(bz2_text.spider[i])));
                }
            } else {
                GZInputStream in = new GZInputStream(queryPath, thread);
                for (int i = 0; i < thread; i++) {
                    threadReaders.add(getReader((new GZIPThreadReader(in.getReader(i)))));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getThreadSize() {
        return threadReaders.size();
    }

    public AbstractFileReader getThreadReader(final int index) {
        if((index < 0) || (index >= threadReaders.size()))
            throw new InvalidArgumentException(String.format("Min thread number is 0 and max thread number is %d.", threadReaders.size()));
        return threadReaders.get(index);
    }

    private void getVCFCodec() throws FileNotFoundException {
        if(codec == null) {
            codec = new VCFCodec();
            header = codec.readActualHeader(new NoFilterIterator(new LongLineReader(queryPath)));
        }
    }

    public AbstractFileReader getReader(final QueryReaderItf itf) throws FileNotFoundException {
        AbstractFileReader reader = VannoUtils.getReader(itf, queryFormat);
        if(queryFormat.getHeaderPart() == null) {
            reader.readHeader();
            queryFormat = reader.getFormat();
        }

        if(queryFormat.getType() == FormatType.VCF)  {

            List<LocFeatureFilter> locFilters = new ArrayList<>();
            locFilters.add(new VCFContextFilter());

            getVCFCodec();
            reader.setCodec(codec, header);

            ((VCFFileReader)reader).setLocFilters(locFilters);
        }
        reader.checkFormat();
        return reader;
    }

    public Format getQueryFormat() {
        return queryFormat;
    }

    public String getQueryName() {
        return queryName;
    }

    public String getQueryPath() {
        return queryPath;
    }

    public void setQueryFormat(Format queryFormat) {
        this.queryFormat = queryFormat;
    }

    public FileType getFileType() {
        return fileType;
    }


    public void printLog() {
        List<String> colNmaes = queryFormat.getOriginalField();

        logger.info(VannoUtils.printLogHeader("QUERY"));
        logger.info(String.format("Query File: %s", getQueryName()));
        logger.info(String.format("Query Format: %s", queryFormat.logFormat()));

        if(!queryFormat.getCommentIndicator().equals("##")) logger.info(String.format("Comment indicator of query is: %s", queryFormat.getCommentIndicator()));
        if(colNmaes != null && queryFormat.isHasHeader()) logger.info(String.format("QueryRegion header is: %s", StringUtil.join(BEDHeaderParser.COMMA, colNmaes)));
    }
}
