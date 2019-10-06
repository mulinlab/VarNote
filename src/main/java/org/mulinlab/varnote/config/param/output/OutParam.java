package org.mulinlab.varnote.config.param.output;

import htsjdk.samtools.util.IOUtil;
import org.mulinlab.varnote.config.param.Param;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.OutMode;
import org.mulinlab.varnote.utils.format.Format;

import java.io.File;

public abstract class OutParam extends Param {

    protected String outputName;
    protected String outputPath;
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

    public abstract void setDefalutOutPath(final String queryPath);

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
}
