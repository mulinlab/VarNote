package org.mulinlab.varnote.config.param.query;

import org.mulinlab.varnote.utils.VannoUtils;

public final class QueryRegionParam extends QueryParam {
    private String region;

    public QueryRegionParam(String region) {
        this.region = region;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public void printLog() {
        logger.info(VannoUtils.parseIndexFileFormat("QUERY"));
        logger.info(String.format("Query Region: %s", region));
    }
}
