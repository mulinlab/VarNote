package org.mulinlab.varnote.config.param.output;


import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.OutMode;


public final class IntersetOutParam extends OutParam {

    private static String OVERLAP_RESULT_SUFFIX = GlobalParameter.OVERLAP_RESULT_SUFFIX;
    private final static String OVERLAP_RESULT_SUFFIX_GZ = GlobalParameter.OVERLAP_RESULT_SUFFIX_GZ;


    private boolean removeCommemt = GlobalParameter.DEFAULT_REMOVE_COMMENT;

    public IntersetOutParam() {
        super();
    }

    public IntersetOutParam(final String outputPath) {
        super(outputPath);
    }

    @Override
    public void setDefalutOutPath(final String queryPath) {
        if(outputPath == null) {
            if(isGzip) OVERLAP_RESULT_SUFFIX = OVERLAP_RESULT_SUFFIX_GZ;

            setOutputPath(queryPath + OVERLAP_RESULT_SUFFIX);
        }
//        log.printKVKCYNSystem("IntersetOutput path is not defined, redirect output folder to", outputFolder);
    }

    public void init() {

//        log.printKVKCYNSystem("Intersect output file path is", outputPath);
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
