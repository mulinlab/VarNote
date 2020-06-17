package org.mulinlab.varnote.operations.readers.query;

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
        super(path, Format.newBED());
    }

    public BEDFileReader(final String path, final Format format) {
        super(path, format);
    }

    public BEDFileReader(final String path, final FileType fileType, final Format format) {
        super(path, fileType, format);
    }


    @Override
    protected void initLineFilters() {
        lineFilters.add(new BEDHeaderLineFilter(format));
    }

    @Override
    public LineFilterIterator getFilterIterator() {
        if(iterator == null) {
            iterator = new LocFilterIterator(new NoFilterIterator(reader), lineFilters, new BEDLocCodec(format, decodeFull));
            setLocFilter();
        }
        return iterator;
    }
}
