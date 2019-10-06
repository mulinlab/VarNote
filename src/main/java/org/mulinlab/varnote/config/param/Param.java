package org.mulinlab.varnote.config.param;

import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.utils.LoggingUtils;

public abstract class Param {

    protected final Logger logger = LoggingUtils.logger;;

    public abstract void checkParam();
}
