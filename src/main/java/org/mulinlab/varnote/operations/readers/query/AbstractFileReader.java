package org.mulinlab.varnote.operations.readers.query;

import htsjdk.samtools.util.IOUtil;
import htsjdk.tribble.AsciiFeatureCodec;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.filters.query.LocFeatureFilter;
import org.mulinlab.varnote.filters.query.line.LineFilter;
import org.mulinlab.varnote.operations.readers.itf.*;
import org.mulinlab.varnote.filters.iterator.LineFilterIterator;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.format.Format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFileReader {

    protected Object header;

    protected List<LineFilter> lineFilters;
    protected List<LocFeatureFilter> locFilters;

    protected String filePath;
    protected FileType fileType;
    protected QueryReaderItf reader;
    protected Format format;

    protected AsciiFeatureCodec codec;
    protected LineFilterIterator iterator;

    protected boolean isDecodeLoc = true;

    public AbstractFileReader(final QueryReaderItf itf, final Format format) {
        this.reader = itf;
        this.format = format;

        this.lineFilters = new ArrayList<>();
        this.initLineFilters();
    }

    public AbstractFileReader(final String path, final Format format) {
        this(path, VannoUtils.checkFileType(path), format);
    }

    public AbstractFileReader(final String path, final FileType fileType, final Format format) {
        this(getReader(path, fileType), format);
        this.filePath = filePath;
        this.fileType = fileType;
    }

    public abstract void checkFormat();

    public void readHeader() {
        if(format.getHeaderPath() != null) {
            format = readDefaultHeader(format.getHeaderPath(), VannoUtils.checkFileType(format.getHeaderPath()), format, true);
        } else {
            if(filePath == null) filePath = reader.getFilePath();
            if(fileType == null) fileType = VannoUtils.checkFileType(filePath);

            format = readDefaultHeader(filePath, fileType, format, format.isHasHeader());
        }
    }

    public void setCodec(final AsciiFeatureCodec codec, final Object header) {
        this.codec = codec;
        this.header = header;
    }

    protected abstract void initLineFilters();
    public abstract LineFilterIterator getFilterIterator();

    public void addLineFilters(final LineFilter lineFilter) {
        lineFilters.add(lineFilter);
    }

    public void setHeader(Object header) {
        this.header = header;
    }

    public Object getHeader() {
        return header;
    }

    public Format getFormat() {
        return format;
    }

    public static QueryReaderItf getReader(final String path) {
        return getReader(path, VannoUtils.checkFileType(path));
    }

    public static QueryReaderItf getReader(final String path, final FileType fileType) {
        IOUtil.assertInputIsValid(path);
        try {
            if (fileType == FileType.BGZ) {
                return new BGZReader(path);
            } else if (fileType == FileType.GZ) {
                return new GZIPReader(path);
            } else {
                return new LongLineReader(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Format readDefaultHeader(final String path, final FileType fileType, final Format format, final boolean hasHeader) {
        try {
            QueryReaderItf reader = getReader(path, fileType);
            readDefaultHeader(reader, format, hasHeader);
            reader.closeReader();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return format;
    }

    public static Format readDefaultHeader(QueryReaderItf reader, final Format format, final boolean hasHeader) {
        System.out.println("read header " + reader.getFilePath());

        NoFilterIterator lineIterator = new NoFilterIterator(reader);
        String line;
        String[] parts = null;

        while (lineIterator.hasNext()) {
            line = lineIterator.next();

            if(line.startsWith(format.getCommentIndicator()) || line.equals("")) {
            } else if(line.startsWith(GlobalParameter.VCF_HEADER_INDICATOR) || hasHeader){
                format.setHeader(line);

                if(line.startsWith(GlobalParameter.VCF_HEADER_INDICATOR)) line = line.substring(1);
                parts = VannoUtils.parserHeader(line, GlobalParameter.TAB);
                format.setHeaderPart(parts, true);
                break;
            } else {
                format.setDataStr(line);

                parts = VannoUtils.setDefaultCol(VannoUtils.parserHeader(line, GlobalParameter.TAB));
                format.setHeaderPart(parts, false);
                break;
            }
        }


        return format;
    }
}
