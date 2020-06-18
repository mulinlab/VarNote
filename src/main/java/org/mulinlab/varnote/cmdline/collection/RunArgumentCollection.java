package org.mulinlab.varnote.cmdline.collection;

import org.broadinstitute.barclay.argparser.Argument;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.constants.GlobalParameter;

public final class RunArgumentCollection {
    private static final long serialVersionUID = 1L;

    @Argument( shortName = Arguments.INTERSECT_THREAD_SHORT, fullName = Arguments.INTERSECT_THREAD_LONG, optional = true,
            doc = "Number of used threads. Sets thread to -1 to get thread number by available processors automatically."
    )
    private Integer threads = GlobalParameter.DEFAULT_THREAD;

    @Argument( shortName = "ALV", fullName = "allowLargeVariants", optional = true,
            doc = "Indicator to allow large variants or not."
    )
    private Boolean allowLargeVariants = GlobalParameter.DEFAULT_ALLOW_MAX_VIRANTS;

    @Argument( shortName = "MVL", fullName = "maxVariantLength", optional = true,
            doc = "Indicator of the max length of variant."
    )
    private int maxVariantLength = GlobalParameter.DEFAULT_MAX_LENGTH;

    public Integer getThreads() {
        return threads;
    }

    public Boolean getAllowLargeVariants() {
        return allowLargeVariants;
    }

    public int getMaxVariantLength() {
        return maxVariantLength;
    }
}
