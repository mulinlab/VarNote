package org.mulinlab.varnote.filters.query.line;

import org.mulinlab.varnote.utils.format.Format;

public final class TABHeaderLineFilter extends HeaderLineFilter {
    private final Format format;

    public TABHeaderLineFilter(final Format format) {
        this.format = format;
    }

    @Override
    public boolean isMetaLine(final String line) {
        return line.startsWith(format.getCommentIndicator());
    }

    @Override
    public boolean isHeaderLine(String line) {
        if(line.startsWith("#")) return true;
        else if(format.getHeaderStart() == null) return false;
        else return line.startsWith(format.getHeaderStart()) && line.startsWith(format.getHeaderStr());
    }
}
