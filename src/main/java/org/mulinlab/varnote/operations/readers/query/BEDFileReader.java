package org.mulinlab.varnote.operations.readers.query;

import htsjdk.tribble.bed.BEDCodec;
import org.mulinlab.varnote.filters.iterator.LocFilterIterator;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.filters.query.line.BEDHeaderLineFilter;
import org.mulinlab.varnote.filters.iterator.LineFilterIterator;
import org.mulinlab.varnote.operations.decode.BEDLocCodec;
import org.mulinlab.varnote.operations.readers.itf.QueryReaderItf;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.format.Format;

public final class BEDFileReader extends AbstractFileReader {

    public BEDFileReader(QueryReaderItf itf, Format format) {
        super(itf, format);
    }

    public BEDFileReader(final String path) {
        super(path, Format.BED);
    }

    public BEDFileReader(final String path, final Format format) {
        super(path, format);
    }

    public BEDFileReader(final String path, final FileType fileType, final Format format) {
        super(path, fileType, format);
    }

    public void defaultCodec() {
        if (codec == null) {
            System.out.println("create codec");
            codec = new BEDCodec(BEDCodec.StartOffset.ZERO);
        }
    }

    public void checkFormat() {
    }

    @Override
    protected void initLineFilters() {
        lineFilters.add(new BEDHeaderLineFilter(format));
    }

    @Override
    public Object getHeader() {
        return null;
    }


    @Override
    public LineFilterIterator getFilterIterator() {
        if(iterator == null) {
            iterator = new LocFilterIterator(new NoFilterIterator(reader), lineFilters, new BEDLocCodec(format));
        }
        return iterator;
    }
}
