package org.mulinlab.varnote.config.parser;

import org.mulinlab.varnote.utils.node.LocFeature;

import java.util.Map;

public interface ResultParser {
    public String processNode(final LocFeature query, final Map<String, LocFeature[]> dbNodeMap);
    public void printLog();
}
