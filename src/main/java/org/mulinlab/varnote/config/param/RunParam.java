package org.mulinlab.varnote.config.param;

import htsjdk.samtools.util.IOUtil;
import org.mulinlab.varnote.config.param.Param;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.IndexType;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import org.mulinlab.varnote.utils.enumset.Mode;

import java.io.File;

public final class RunParam extends Param {

    private Mode mode = GlobalParameter.DEFAULT_MODE;
    private int thread = GlobalParameter.DEFAULT_THREAD;

    public RunParam() {
    }

    public RunParam(final int thread) {
        setThread(thread);
    }

    public void checkThreadNum(final int spiderSize) {
        if(spiderSize != thread) {
            thread = spiderSize;
        }
    }

    public int getThread() {
        return thread;
    }

    public void setThread(int thread) {
        if(thread < 1)  {
            if(thread == -1) setAutoThread();
            else throw new InvalidArgumentException("Thread should be -1(automatically get thread number by available processors) or a number greater than zero, but we get " + thread);
        }
        this.thread = thread;
    }

    public void checkAutoThread() {
        if(thread == -1) setAutoThread();
    }

    public void setAutoThread() {
        thread = Runtime.getRuntime().availableProcessors();
        if(thread < 1) thread = 1;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public void checkParam() {

    }
}
