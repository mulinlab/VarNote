package org.mulinlab.varnote.filters.iterator;

import htsjdk.tribble.readers.LineIterator;
import org.mulinlab.varnote.operations.readers.itf.QueryReaderItf;
import org.mulinlab.varnote.operations.readers.query.AbstractFileReader;
import org.mulinlab.varnote.utils.enumset.FileType;

import java.io.IOException;
import java.util.NoSuchElementException;

public final class NoFilterIterator implements LineIterator {
    private QueryReaderItf reader;
    private String next;
    private boolean iterating = false;

    public NoFilterIterator(final QueryReaderItf reader) {
        this.reader = reader;
    }
    public NoFilterIterator(final String path, final FileType fileType) {
        this.reader = AbstractFileReader.getReader(path, fileType);
    }

    @Override
    public boolean hasNext() {
        try {
            if (!iterating) {
                next = reader.readLine();
                iterating = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return next != null;
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        String ret = next;

        try {
            next = reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public String peek() {
        return next;
    }

    public void close() {
        try {
            reader.closeReader();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getPosition() {
        return reader.getPosition();
    }

}
