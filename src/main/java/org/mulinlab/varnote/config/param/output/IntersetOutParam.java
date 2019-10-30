package org.mulinlab.varnote.config.param.output;


import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.OutMode;


public final class IntersetOutParam extends OutParam {

    private boolean removeCommemt = GlobalParameter.DEFAULT_REMOVE_COMMENT;

    public IntersetOutParam() {
        super();
    }

    public IntersetOutParam(final String outputPath) {
        super(outputPath);
    }


    public void init() {
    }

    public OutMode getOutputMode() {
        return outputMode;
    }

    @Override
    public void checkParam() {

    }

    public boolean isRemoveCommemt() {
        return removeCommemt;
    }

    public void setRemoveCommemt(boolean removeCommemt) {
        this.removeCommemt = removeCommemt;
    }
}
