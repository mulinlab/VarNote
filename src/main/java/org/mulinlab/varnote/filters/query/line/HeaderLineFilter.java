package org.mulinlab.varnote.filters.query.line;


import org.mulinlab.varnote.utils.VannoUtils;

public abstract class HeaderLineFilter implements LineFilter {

    private boolean isReadHeader = false;

    public HeaderLineFilter() {
    }

    @Override
    public boolean isFilterLine(String line) {
        if(isReadHeader) {
            return false;
        } else if(isMetaLine(line) || line.trim().equals("")) {
//            System.out.println("meta line" + line);
            return true;
        } else if(isHeaderLine(line)) {
//            System.out.println("header line" + line);
            isReadHeader = true;
            return true;
        } else {
//            System.out.println("data line" + line);
            isReadHeader = true;
            return false;
        }
    }

    public abstract boolean isMetaLine(final String line);
    public abstract boolean isHeaderLine(final String line);
}
