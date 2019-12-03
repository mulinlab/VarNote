package org.mulinlab.varnote.config.param.query;

import htsjdk.samtools.util.IOUtil;
import org.mulinlab.varnote.config.anno.databse.HeaderFormatReader;
import org.mulinlab.varnote.config.param.FilterParam;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.operations.readers.itf.QueryReaderItf;
import org.mulinlab.varnote.operations.readers.itf.thread.GZIPThreadReader;
import org.mulinlab.varnote.operations.readers.itf.thread.SpiderReader;
import org.mulinlab.varnote.operations.readers.query.AbstractFileReader;
import org.mulinlab.varnote.operations.readers.query.VCFFileReader;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.enumset.FormatType;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.stream.BZIP2InputStream;
import org.mulinlab.varnote.utils.stream.GZInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class QueryFileParam extends QueryParam {

    private final String path;
    private final FileType fileType;
    private final Format queryFormat;
    private final String fileName;

    private final boolean isFull;
    private FilterParam filterParam;

    protected List<AbstractFileReader> threadReaders;


    public QueryFileParam(final String path, final boolean isFull) {
        this(path, null, isFull, null);
    }

    public QueryFileParam(final String path, Format queryFormat, final boolean isFull) {
        this(path, queryFormat, isFull, null);
    }

    public QueryFileParam(final String path, Format queryFormat, final boolean isFull, final FilterParam filterParam) {
        if(path == null || path.trim().equals("")) throw new InvalidArgumentException("Query file is required.");

        IOUtil.assertInputIsValid(path);

        this.path = VannoUtils.getAbsolutePath(path);
        this.fileName = new File(this.path).getName();
        this.fileType = VannoUtils.checkFileType(path);
        this.isFull = isFull;
        this.filterParam = filterParam;

        if(queryFormat == null) queryFormat = Format.defaultFormat(path, true);
        if(queryFormat.getHeaderPart() == null) {
            queryFormat = HeaderFormatReader.readHeader(queryFormat, path, fileType);
        }

        this.queryFormat = queryFormat;
    }

    public void splitFile(int thread) {
        if(threadReaders == null) threadReaders = new ArrayList<>();

        try {
            if (fileType == FileType.BGZ || fileType == FileType.TXT) {
                BZIP2InputStream bz2_text = new BZIP2InputStream(path, thread);
                bz2_text.adjustPos();
                bz2_text.creatSpider();

                if (bz2_text.getThreadNum() != thread) {
                    thread = bz2_text.getThreadNum();
                }

                for (int i = 0; i < thread; i++) {
                    threadReaders.add(getReader(new SpiderReader(bz2_text.spider[i])));
                }
            } else {
                GZInputStream in = new GZInputStream(path, thread);
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

    public AbstractFileReader getReader(final QueryReaderItf itf) throws FileNotFoundException {
        AbstractFileReader r = VannoUtils.getReader(itf, this.queryFormat);

        if(queryFormat.type == FormatType.VCF) {
            if(filterParam != null) {
                ((VCFFileReader)r).setVariantFilters(filterParam);
            }
        }
        r.setDecodeFull(isFull);
        return r;
    }

    public Format getQueryFormat() {
        return queryFormat;
    }
    public String getQueryName() {
        return fileName;
    }
    public String getQueryPath() {
        return path;
    }

    public void printLog() {
        logger.info(VannoUtils.printLogHeader("QUERY"));
        logger.info(String.format("Query File: %s", getQueryName()));
        logger.info(String.format("Query Format: %s", queryFormat.logFormat()));

        if(!queryFormat.getCommentIndicator().equals("##")) logger.info(String.format("Comment indicator of query is: %s", queryFormat.getCommentIndicator()));
        if(queryFormat.isHasHeader()) logger.info(String.format("QueryRegion header is: %s", queryFormat.getHeaderPartStr()));
    }


    public FileType getFileType() {
        return fileType;
    }

    public FilterParam getFilterParam() {
        return filterParam;
    }
    public void setFilterParam(FilterParam filterParam) {
        this.filterParam = filterParam;
    }
}
