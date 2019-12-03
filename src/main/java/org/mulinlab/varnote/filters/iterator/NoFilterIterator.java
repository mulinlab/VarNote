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
    protected long pos;

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
                getString();

                iterating = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return next != null;
    }

    public void getString() {
        try {
            pos = reader.getPosition();
            next = reader.readLine();
            if(next != null && next.length() > 1 && next.charAt(next.length() - 1) == '\r') {
                next = next.substring(0, next.length() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        String ret = next;
        try {
            getString();
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
        return pos;
    }
}
