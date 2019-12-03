package org.mulinlab.varnote.operations.readers.query;


import org.mulinlab.varnote.filters.iterator.LocFilterIterator;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.filters.query.line.TABHeaderLineFilter;
import org.mulinlab.varnote.operations.decode.TABLocCodec;
import org.mulinlab.varnote.filters.iterator.LineFilterIterator;
import org.mulinlab.varnote.operations.readers.itf.QueryReaderItf;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.format.Format;


public final class TABFileReader extends AbstractFileReader {

    public TABFileReader(QueryReaderItf itf, Format format) {
        super(itf, format);
    }

    public TABFileReader(final String path, final Format format) {
        super(path, format);
    }

    public TABFileReader(final String path, final FileType fileType, final Format format) {
        super(path, fileType, format);
    }

    @Override
    protected void initLineFilters() {
        lineFilters.add(new TABHeaderLineFilter(format));
    }

    @Override
    public LineFilterIterator getFilterIterator() {
        if(iterator == null) {
            iterator = new LocFilterIterator(new NoFilterIterator(reader), lineFilters, new TABLocCodec(format, decodeFull));
        }
        return iterator;
    }
}
