package org.mulinlab.varnote.config.param.output;

import org.mulinlab.varnote.config.param.Param;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.OutMode;
import java.io.File;

public class OutParam extends Param {

    protected String outputName;
    protected String outputPath;
    protected String outFileSuffix = GlobalParameter.OVERLAP_RESULT_SUFFIX;

    protected boolean loj = GlobalParameter.DEFAULT_LOJ;
    protected boolean isGzip = GlobalParameter.DEFAULT_IS_GZIP;
    protected OutMode outputMode = GlobalParameter.DEFALT_OUT_MODE;

    public OutParam() {
        super();
    }

    public OutParam(final String outputPath) {
       setOutputPath(outputPath);
    }

    public void printLog() {
        logger.info(VannoUtils.printLogHeader("OUTPUT"));
        logger.info(String.format("Output Path: %s", outputPath));
        logger.info(String.format("Is LOJ: %s", loj));
        logger.info(String.format("Is GZIP: %s", isGzip));
        logger.info(String.format("Output Mode: %s", outputMode));
    }

    public void setDefalutOutPath(final String queryPath) {
        if(outputPath == null) {
            if(isGzip) {
                setOutputPath(queryPath + outFileSuffix + ".gz");
            } else {
                setOutputPath(queryPath + outFileSuffix);
            }
        }
    }

    public boolean isLoj() {
        return loj;
    }

    public void setLoj(boolean loj) {
        this.loj = loj;
    }

    public boolean isGzip() {
        return isGzip;
    }

    public void setGzip(boolean gzip) {
        isGzip = gzip;
    }

    public String getOutputPath() {
        if(isGzip && !VannoUtils.hasExtension(VannoUtils.FileExt.GZ, outputPath)) {
            outputPath = outputPath + ".gz";
            outputName = outputName + ".gz";
        }
        return outputPath;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
        this.outputName = new File(outputPath).getName();

        if(VannoUtils.hasExtension(VannoUtils.FileExt.GZ, outputPath)) {
            isGzip = true;
        }
    }

    public void setOutputMode(OutMode outputMode) {
        this.outputMode = outputMode;
    }

    public void setOutputMode(int outMode) {
        this.outputMode = VannoUtils.checkOutMode(outMode);
    }

    public void setOutFileSuffix(String outFileSuffix) {
        this.outFileSuffix = outFileSuffix;
    }

    @Override
    public void checkParam() {

    }
}
