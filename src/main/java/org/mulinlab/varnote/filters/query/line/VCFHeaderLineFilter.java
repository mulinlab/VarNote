package org.mulinlab.varnote.filters.query.line;


public final class VCFHeaderLineFilter extends HeaderLineFilter {
    public VCFHeaderLineFilter() {
    }


    @Override
    public boolean isMetaLine(final String line) {
        return line.startsWith("##");
    }

    @Override
    public boolean isHeaderLine(String line) {
        System.out.println("is header" + line);
        return line.startsWith("#");
    }
}
