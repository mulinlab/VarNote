package org.mulinlab.varnote.config.param.query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.utils.LoggingUtils;

public abstract class QueryParam {
    protected final Logger logger = LoggingUtils.logger;
    public QueryParam() {
        super();
    }
    public abstract void printLog();
}
