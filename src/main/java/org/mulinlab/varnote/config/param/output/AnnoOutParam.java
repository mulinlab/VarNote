package org.mulinlab.varnote.config.param.output;

import htsjdk.samtools.util.IOUtil;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.AnnoOutFormat;
import org.mulinlab.varnote.utils.format.Format;

import java.text.Normalizer;

public final class AnnoOutParam extends OutParam {

    private static String ANNO_RESULT_SUFFIX = GlobalParameter.ANNO_RESULT_SUFFIX;
    private final static String ANNO_RESULT_SUFFIX_GZ = GlobalParameter.ANNO_RESULT_SUFFIX_GZ;

    private AnnoOutFormat annoOutFormat ;


    public AnnoOutParam() { super(); }
    public AnnoOutParam(final String outputPath) {
        super(outputPath);
    }

    @Override
    public void checkParam() {

    }

    public void init() {

    }

    @Override
    public void setDefalutOutPath(final String queryPath) {
        if(outputPath == null) {
            if(isGzip) {
                ANNO_RESULT_SUFFIX = ANNO_RESULT_SUFFIX_GZ;
            }
            this.outputPath = queryPath + ANNO_RESULT_SUFFIX ;
        }
//        log.printKVKCYNSystem("IntersetOutput path is not defined, redirect output folder to", outputFolder);
    }



    public AnnoOutFormat getAnnoOutFormat() {
        return annoOutFormat;
    }

    public void setAnnoOutFormat(AnnoOutFormat annoOutFormat) {
        this.annoOutFormat = annoOutFormat;
    }
    public void setAnnoOutFormat(final String _annoOutFormat) {
        setAnnoOutFormat(VannoUtils.checkFileFormat(_annoOutFormat));
    }



    public void setDefalutOutFormat(final Format queryFormat) {
        if(annoOutFormat == null) {
            if (queryFormat.getFlags() == Format.VCF_FLAGS)
                this.annoOutFormat = AnnoOutFormat.VCF;
            else this.annoOutFormat = AnnoOutFormat.BED;
        }
    }

}
