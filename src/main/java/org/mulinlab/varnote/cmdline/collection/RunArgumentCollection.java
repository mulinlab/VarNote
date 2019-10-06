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

    public Integer getThreads() {
        return threads;
    }
}
