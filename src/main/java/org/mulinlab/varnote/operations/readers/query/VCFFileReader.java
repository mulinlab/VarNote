package org.mulinlab.varnote.operations.readers.query;

import htsjdk.variant.vcf.VCFCodec;
import org.mulinlab.varnote.filters.query.LocFeatureFilter;
import org.mulinlab.varnote.filters.query.line.LineFilter;
import org.mulinlab.varnote.filters.query.line.VCFHeaderLineFilter;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.operations.readers.itf.QueryReaderItf;
import org.mulinlab.varnote.operations.decode.VCFLocCodec;
import org.mulinlab.varnote.filters.iterator.LineFilterIterator;
import org.mulinlab.varnote.filters.iterator.LocFilterIterator;
import org.mulinlab.varnote.filters.iterator.VCFFilterIterator;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.format.Format;

import java.util.List;

public final class VCFFileReader extends AbstractFileReader {

    public VCFFileReader(QueryReaderItf itf, Format format) {
        super(itf, format);
    }

    public VCFFileReader(final String path) {
        super(path, Format.VCF);
    }

    public VCFFileReader(final String path, final Format format) {
        super(path, format);
    }

    public VCFFileReader(final String path, final FileType fileType, final Format format) {
        super(path, fileType, format);
    }

    @Override
    public void checkFormat() {
    }

    @Override
    protected void initLineFilters() {
        lineFilters.add(new VCFHeaderLineFilter());
    }

    @Override
    public Object getHeader() {
        if(header == null) {
            NoFilterIterator iterator = new NoFilterIterator(getReader(filePath, fileType));
            header = codec.readActualHeader(iterator);
            iterator.close();
        }
        return header;
    }

    public void defaultCodec() {
        if(codec == null) {
            System.out.println("create codec");
            codec = new VCFCodec();
            getHeader();
        }
    }

    public void setLocFilters(final List<LocFeatureFilter> locFilters) {
        this.isDecodeLoc = false;
        this.locFilters = locFilters;
    }

    @Override
    public LineFilterIterator getFilterIterator() {
        if(iterator == null) {
            if(isDecodeLoc) {
                iterator = new LocFilterIterator(new NoFilterIterator(reader), lineFilters, new VCFLocCodec(format));
            } else {
                defaultCodec();
                iterator = new VCFFilterIterator(new NoFilterIterator(reader), lineFilters, locFilters, (VCFCodec)codec);
            }
        }
        return iterator;
    }
}
