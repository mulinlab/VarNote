package org.mulinlab.varnote.config.parser.output;

import org.mulinlab.varnote.utils.node.LocFeature;

public final class AnnoOut extends ADOutFeature {

    private String result;

    public AnnoOut(LocFeature queryLoc) {
        super(queryLoc);
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
